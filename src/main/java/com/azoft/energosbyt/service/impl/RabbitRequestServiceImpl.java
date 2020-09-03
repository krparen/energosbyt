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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class RabbitRequestServiceImpl implements RabbitRequestService {

    private static final String TXN_RECORD_WITH_SAME_ID_EXISTS =
            "Транзакция с id = %s уже в обработке или завершена";

    private final AmqpTemplate template;
    private final AmqpAdmin rabbitAdmin;
    private final ObjectMapper mapper;
    private final QiwiTxnRepository qiwiTxnRepository;

    @Value("${energosbyt.rabbit.request.queue-name}")
    private String requestQueueName;
    @Value("${energosbyt.rabbit.request.timeout-in-ms}")
    private Long requestTimeout;

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

            if (qiwiRequest.getCommand() == Command.pay) {
                QiwiTxnEntity txnWithSameId = qiwiTxnRepository.findByTxnId(qiwiRequest.getTxn_id());
                if (txnWithSameId == null) {
                    createTxnRecord(qiwiRequest);
                } else {
                    return txnRecordExistsResponse(qiwiRequest);
                }
            }


            replyQueueName = declareReplyQueue();
            MessageProperties messageProperties = createMessageProperties(replyQueueName, qiwiRequest);
            byte[] body = createMessageBody(qiwiRequest);
            Message requestMessage = new Message(body, messageProperties);

            template.send(requestQueueName, requestMessage);
            BasePerson rabbitResponse = receiveResponse(replyQueueName);

            return getQiwiResponse(qiwiRequest, rabbitResponse);
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

    private byte[] createMessageBody(QiwiRequest request) {
        return toRabbitRequestBodyAsString(request)
                .getBytes(StandardCharsets.UTF_8);
    }

    private String toRabbitRequestBodyAsString(QiwiRequest request) {
        String bodyAsString = null;
        try {
            bodyAsString = mapper.writeValueAsString(createRabbitRequest(request));
        } catch (JsonProcessingException e) {
            String message = "Rabbit request serialization failed";
            log.error(message, e);
            throw new ApiException(message, e, QiwiResultCode.OTHER_PROVIDER_ERROR);
        }
        log.info("body as String: {}", bodyAsString);
        return bodyAsString;
    }

    private MessageProperties createMessageProperties(String replyQueueName, QiwiRequest qiwiRequest) {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("type", qiwiRequest.getCommand().getRabbitType());
        messageProperties.setHeader("m_guid", "08.06.2020");
        messageProperties.setHeader("reply-to", replyQueueName);
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

    private BasePerson createRabbitRequest(QiwiRequest qiwiRequest) {
        BasePerson rabbitRequest = new BasePerson();
        rabbitRequest.setSystem_id("1010");

        BasePerson.Srch search = new BasePerson.Srch();
        search.setAccount_number(qiwiRequest.getAccount());
        search.setDept("SESB");
        rabbitRequest.setSrch(search);
        return rabbitRequest;
    }

    private QiwiResponse getQiwiResponse(QiwiRequest qiwiRequest, BasePerson rabbitResponse) {

        if (qiwiRequest.getCommand() == Command.check) {
            return prepareCheckResponse(qiwiRequest, rabbitResponse);
        } else if (qiwiRequest.getCommand() == Command.pay) {
            return preparePayResponse();
        }

        return new QiwiResponse();
    }

    private QiwiResponse preparePayResponse() {
        QiwiResponse response = new QiwiResponse();
        response.setComment("Тестовый фиксированный ответ");
        response.setOsmp_txn_id("13513416");
        response.setResult(0);
        response.setPrv_txn("49472744");
        BigDecimal sum = BigDecimal.TEN;
        sum = sum.setScale(2);
        response.setSum(sum);
        return response;
    }

    private QiwiResponse prepareCheckResponse(QiwiRequest qiwiRequest, BasePerson rabbitResponse) {

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

        response.setFields(List.of(userId, fio));
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
}
