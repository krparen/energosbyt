package com.azoft.energosbyt.service.impl;

import com.azoft.energosbyt.QiwiTxnRepository;
import com.azoft.energosbyt.dto.*;
import com.azoft.energosbyt.entity.QiwiTxnEntity;
import com.azoft.energosbyt.exception.ApiException;
import com.azoft.energosbyt.exception.QiwiResultCode;
import com.azoft.energosbyt.service.RabbitRequestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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


    public RabbitRequestServiceImpl(AmqpTemplate template, AmqpAdmin rabbitAdmin, ObjectMapper mapper, QiwiTxnRepository qiwiTxnRepository) {
        this.template = template;
        this.rabbitAdmin = rabbitAdmin;
        this.mapper = mapper;
        this.qiwiTxnRepository = qiwiTxnRepository;
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
                replyQueueName = declareReplyQueue();
                MessageProperties messageProperties = createCheckMessageProperties(replyQueueName, qiwiRequest);
                byte[] body = createCheckMessageBody(qiwiRequest);
                Message requestMessage = new Message(body, messageProperties);

                template.send(checkRequestQueueName, requestMessage);
                BasePerson rabbitResponse = receiveResponse(replyQueueName);

                return getCheckQiwiResponse(qiwiRequest, rabbitResponse);
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

    private byte[] createCheckMessageBody(QiwiRequest request) {

        String bodyAsString = null;
        try {
            bodyAsString = mapper.writeValueAsString(createCheckRabbitRequest(request));
        } catch (JsonProcessingException e) {
            String message = "Rabbit request serialization failed";
            log.error(message, e);
            throw new ApiException(message, e, QiwiResultCode.OTHER_PROVIDER_ERROR);
        }
        log.info("body as String: {}", bodyAsString);

        return bodyAsString.getBytes(StandardCharsets.UTF_8);
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

    private MessageProperties createCheckMessageProperties(String replyQueueName, QiwiRequest qiwiRequest) {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("type", qiwiRequest.getCommand().getRabbitType());
        messageProperties.setHeader("m_guid", "08.06.2020");
        messageProperties.setHeader("reply-to", replyQueueName);
        messageProperties.setContentEncoding(StandardCharsets.UTF_8.name());
        return messageProperties;
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

    private BasePerson receiveResponse(String replyQueueName) throws UnsupportedEncodingException {
        Message responseMessage = safelyReceiveResponse(replyQueueName);
        String responseAsString = new String(responseMessage.getBody(), StandardCharsets.UTF_8.name());
        return safelyDeserializeFromResponse(responseAsString);
    }

    private String declareReplyQueue() {
        String replyQueueName = UUID.randomUUID().toString();
        Queue newQueue = new Queue(replyQueueName, false, false, true);

        try {
            return rabbitAdmin.declareQueue(newQueue);
        } catch (AmqpException e) {
            String message = "Queue declaration failed";
            log.error(message, e);
            throw new ApiException(message, e, QiwiResultCode.TRY_AGAIN_LATER);
        }

    }

    private BasePerson createCheckRabbitRequest(QiwiRequest qiwiRequest) {
        BasePerson rabbitRequest = new BasePerson();
        rabbitRequest.setSystem_id(thisSystemId);

        BasePerson.Srch search = new BasePerson.Srch();
        search.setAccount_number(qiwiRequest.getAccount());
        search.setDept("SESB");
        rabbitRequest.setSrch(search);
        return rabbitRequest;
    }

    private QiwiResponse getCheckQiwiResponse(QiwiRequest qiwiRequest, BasePerson rabbitResponse) {

        QiwiResponse response = new QiwiResponse();

        if (rabbitResponse.getSrch_res().getRes().isEmpty()) {
            response.setResult(QiwiResultCode.ABONENT_ID_NOT_FOUND.getNumericCode());
            return response;
        }

        response.setResult(QiwiResultCode.OK.getNumericCode());
        response.setOsmp_txn_id(qiwiRequest.getTxn_id());

        Field userId = new Field();
        userId.setName("id");
        userId.setType(FieldType.INFO.getStringValue());
        userId.setValue(rabbitResponse.getSrch_res().getRes().get(0).getId());

        Field fio = new Field();
        fio.setName("fio");
        fio.setType(FieldType.INFO.getStringValue());
        fio.setValue(rabbitResponse.getSrch_res().getRes().get(0).getFio());

        List<Field> fields = new ArrayList<>();
        fields.add(userId);
        fields.add(fio);

        response.setFields(fields);
        return response;
    }

    private Message safelyReceiveResponse(String replyQueueName) {
        Message responseMessage = null;
        try {
            responseMessage = template.receive(replyQueueName, requestTimeout);
        } catch (AmqpException e) {
            String message = "Getting a rabbit response from queue " + replyQueueName + " failed";
            log.error(message, e);
            throw new ApiException(message, e, QiwiResultCode.TRY_AGAIN_LATER);
        }

        if (responseMessage == null) {
            String message = "Rabbit response from queue " + replyQueueName + " failed timeout";
            log.error(message, replyQueueName);
            throw new ApiException(message, QiwiResultCode.TRY_AGAIN_LATER);
        }
        return responseMessage;
    }

    private BasePerson safelyDeserializeFromResponse(String responseAsString) {
        BasePerson response = null;
        try {
            response = mapper.readValue(responseAsString, BasePerson.class);
        } catch (JsonProcessingException e) {
            String message = "Rabbit response deserialization failed";
            log.error(message, e);
            throw new ApiException(message, e, QiwiResultCode.TRY_AGAIN_LATER);
        }

        log.info("response from rabbit: {}", response);
        return response;
    }

    private Date dateFromLocalDateTime(LocalDateTime dateToConvert) {
        return java.util.Date
                .from(dateToConvert.atZone(ZoneId.systemDefault()).toInstant());
    }
}
