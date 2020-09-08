package com.azoft.energosbyt.service.impl;

import com.azoft.energosbyt.repository.QiwiTxnRepository;
import com.azoft.energosbyt.dto.*;
import com.azoft.energosbyt.entity.QiwiTxnEntity;
import com.azoft.energosbyt.exception.ApiException;
import com.azoft.energosbyt.exception.QiwiResultCode;
import com.azoft.energosbyt.service.RabbitRequestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class RabbitRequestServiceImpl implements RabbitRequestService {

    private static final String TXN_RECORD_WITH_SAME_ID_EXISTS =
            "Транзакция с id = %s уже в обработке или завершена";

    /**
     * Используется для форматирования даты при отправке сообщения в очередь pay
     */
    private static final DateTimeFormatter payDateTimeFormatter =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final AmqpTemplate template;
    private final AmqpAdmin rabbitAdmin;
    private final ObjectMapper mapper;
    private final QiwiTxnRepository qiwiTxnRepository;
    private final CheckRequestService checkRequestService;

    @Value("${energosbyt.rabbit.request.check.queue-name}")
    private String checkRequestQueueName;
    @Value("${energosbyt.rabbit.request.pay.queue-name}")
    private String payRequestQueueName;
    @Value("${energosbyt.rabbit.request.timeout-in-ms}")
    private Long requestTimeout;
    @Value("${energosbyt.application.this-system-id}")
    private String thisSystemId;
    @Value("${energosbyt.application.qiwi-system-id}")
    private String qiwiSystemId;


    public RabbitRequestServiceImpl(AmqpTemplate template, AmqpAdmin rabbitAdmin, ObjectMapper mapper,
                                    QiwiTxnRepository qiwiTxnRepository, CheckRequestService checkRequestService) {
        this.template = template;
        this.rabbitAdmin = rabbitAdmin;
        this.mapper = mapper;
        this.qiwiTxnRepository = qiwiTxnRepository;
        this.checkRequestService = checkRequestService;
    }

    @Override
    @Transactional
    public QiwiResponse sendRequestToQueue(QiwiRequest qiwiRequest) {

        String replyQueueName = null;

        try {
            QiwiTxnEntity txnWithSameId = qiwiTxnRepository.findByTxnId(qiwiRequest.getTxn_id());
            if (txnWithSameId == null) {
                if (qiwiRequest.getCommand() == Command.pay) {
                    createTxnRecord(qiwiRequest);
                }
            } else {
                return txnRecordExistsResponse(qiwiRequest);
            }


            if (qiwiRequest.getCommand() == Command.check) {
                return checkRequestService.process(qiwiRequest);
            } else {
                MessageProperties messageProperties = createPayMessageProperties();
                byte[] body = createPayMessageBody(qiwiRequest);
                Message requestMessage = new Message(body, messageProperties);
                template.send(payRequestQueueName, requestMessage);
                return QiwiResponse.ok();
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            String message = "Unknown exception happened";
            log.error(message, e);
            throw new ApiException(message, e, QiwiResultCode.OTHER_PROVIDER_ERROR);
        } finally {
            if (replyQueueName != null) {
                rabbitAdmin.deleteQueue(replyQueueName);
            }
        }
    }

    private QiwiResponse txnRecordExistsResponse(QiwiRequest qiwiRequest) {
        QiwiResponse qiwiResponse = new QiwiResponse();
        qiwiResponse.setResult(QiwiResultCode.OTHER_PROVIDER_ERROR.getNumericCode());

        String comment = String.format(TXN_RECORD_WITH_SAME_ID_EXISTS, qiwiRequest.getTxn_id());
        qiwiResponse.setComment(comment);

        return qiwiResponse;
    }

    private void createTxnRecord(QiwiRequest qiwiRequest) {
        QiwiTxnEntity newTxnRecord = new QiwiTxnEntity();
        newTxnRecord.setTxnId(qiwiRequest.getTxn_id());
        newTxnRecord.setTxnDate(qiwiRequest.getTxn_date());
        newTxnRecord.setCommand(qiwiRequest.getCommand());
        newTxnRecord.setAccount(qiwiRequest.getAccount());
        newTxnRecord.setSum(qiwiRequest.getSum());
        qiwiTxnRepository.save(newTxnRecord);
    }

    private byte[] createPayMessageBody(QiwiRequest qiwiRequest) {

        String bodyAsString = null;
        try {
            bodyAsString = mapper.writeValueAsString(createPayRabbitRequest(qiwiRequest));
        } catch (JsonProcessingException e) {
            String message = "Rabbit request serialization failed";
            log.error(message, e);
            throw new ApiException(message, e, QiwiResultCode.OTHER_PROVIDER_ERROR);
        }
        log.info("body as String: {}", bodyAsString);

        return bodyAsString.getBytes(StandardCharsets.UTF_8);
    }

    private BasePayCashLkk createPayRabbitRequest(QiwiRequest qiwiRequest) {
        BasePayCashLkk cash = new BasePayCashLkk();
        cash.setSystem_id(qiwiSystemId);
        cash.setAccount_id(qiwiRequest.getAccount());
        cash.setAmmount(qiwiRequest.getSum().floatValue());
        cash.setTrx_id(qiwiRequest.getTxn_id());
        cash.setPayDate(dateFromLocalDateTime(qiwiRequest.getTxn_date()));
        return cash;
    }

    private MessageProperties createPayMessageProperties() {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("system_id", qiwiSystemId);
        messageProperties.setHeader("m_guid", UUID.randomUUID().toString());
        messageProperties.setHeader("type", Command.pay.getRabbitType());
        messageProperties.setHeader("m_date",
                LocalDateTime.now().format(payDateTimeFormatter));
        messageProperties.setContentEncoding(StandardCharsets.UTF_8.name());
        return messageProperties;
    }



    private Date dateFromLocalDateTime(LocalDateTime dateToConvert) {
        return java.util.Date
                .from(dateToConvert.atZone(ZoneId.systemDefault()).toInstant());
    }
}
