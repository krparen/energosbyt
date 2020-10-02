package com.azoft.energosbyt.service.impl;

import com.azoft.energosbyt.dto.*;
import com.azoft.energosbyt.exception.ApiException;
import com.azoft.energosbyt.exception.QiwiResultCode;
import com.azoft.energosbyt.service.QiwiRequestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CheckRequestService implements QiwiRequestService {

    @Value("${energosbyt.rabbit.request.check.queue-name}")
    private String checkRequestQueueName;
    @Value("${energosbyt.rabbit.request.timeout-in-ms}")
    private Long requestTimeout;
    @Value("${energosbyt.application.this-system-id}")
    private String thisSystemId;

    private final AmqpTemplate template;
    private final AmqpAdmin rabbitAdmin;
    private final ObjectMapper mapper;

    public CheckRequestService(AmqpTemplate template, AmqpAdmin rabbitAdmin, ObjectMapper mapper) {
        this.template = template;
        this.rabbitAdmin = rabbitAdmin;
        this.mapper = mapper;
    }

    @Override
    public QiwiResponse process(QiwiRequest qiwiRequest) {

        String personReplyQueueName = null;
        String metersReplyQueueName = null;

        try {
            personReplyQueueName = declareReplyQueueWithUuidName();
            MessageProperties personMessageProperties = createPersonMessageProperties(personReplyQueueName, qiwiRequest);
            byte[] personMessageBody = createPersonMessageBody(qiwiRequest);
            Message personRequestMessage = new Message(personMessageBody, personMessageProperties);

            template.send(checkRequestQueueName, personRequestMessage);
            BasePerson personRabbitResponse = receivePersonResponse(personReplyQueueName);

            if (personRabbitResponse.getSrch_res().getRes().isEmpty()) {
                String message = "No person found for account id = " + qiwiRequest.getAccount();
                log.error(message);
                throw new ApiException(message, QiwiResultCode.ABONENT_ID_NOT_FOUND, true);
            }

            String personId = personRabbitResponse.getSrch_res().getRes().get(0).getId();

            metersReplyQueueName = declareReplyQueueWithUuidName();
            MessageProperties metersMessageProperties = createMetersMessageProperties(metersReplyQueueName);
            byte[] metersMessageBody = createMetersMessageBody(personId);
            Message metersRequestMessage = new Message(metersMessageBody, metersMessageProperties);

            template.send(checkRequestQueueName, metersRequestMessage);

            BaseMeter metersRabbitResponse = receiveMetersResponse(metersReplyQueueName);
            log.info("User with id {} has meters {}", personId, metersRabbitResponse.getSrch_res().getServ());


            return getCheckQiwiResponse(qiwiRequest, personRabbitResponse);
        } finally {
            if (personReplyQueueName != null) {
                rabbitAdmin.deleteQueue(personReplyQueueName);
            }
            if (metersReplyQueueName != null) {
                rabbitAdmin.deleteQueue(metersReplyQueueName);
            }
        }
    }

    private BaseMeter receiveMetersResponse(String replyQueueName) {
        Message responseMessage = safelyReceiveResponse(replyQueueName);
        String responseAsString = getMessageBodyAsString(responseMessage);
        return safelyDeserializeMeterFromResponse(responseAsString);
    }

    private BaseMeter safelyDeserializeMeterFromResponse(String responseAsString) {
        BaseMeter response = null;
        try {
            response = mapper.readValue(responseAsString, BaseMeter.class);
        } catch (JsonProcessingException e) {
            String message = "Rabbit response deserialization failed";
            log.error(message, e);
            throw new ApiException(message, e, QiwiResultCode.TRY_AGAIN_LATER);
        }

        log.info("response from rabbit: {}", response);
        return response;
    }

    private MessageProperties createMetersMessageProperties(String metersReplyQueueName) {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("type", "searchMeter");
        messageProperties.setHeader("m_guid", "08.06.2020"); // легаси заголовок, должен присутствовать, а что в нём - не важно
        messageProperties.setHeader("reply-to", metersReplyQueueName);
        messageProperties.setContentEncoding(StandardCharsets.UTF_8.name());
        return messageProperties;
    }

    private byte[] createMetersMessageBody(String personId) {
        String bodyAsString = null;
        try {
            bodyAsString = mapper.writeValueAsString(createMetersRabbitRequest(personId));
        } catch (JsonProcessingException e) {
            String message = "Rabbit request serialization failed";
            log.error(message, e);
            throw new ApiException(message, e, QiwiResultCode.OTHER_PROVIDER_ERROR);
        }
        log.info("body as String: {}", bodyAsString);

        return bodyAsString.getBytes(StandardCharsets.UTF_8);
    }

    private BaseMeter createMetersRabbitRequest(String personId) {
        BaseMeter rabbitRequest = new BaseMeter();
        rabbitRequest.setSystem_id(thisSystemId);

        BaseMeter.Srch search = new BaseMeter.Srch();
        search.setPerson_Id(personId);
        rabbitRequest.setSrch(search);
        return rabbitRequest;
    }

    private String declareReplyQueueWithUuidName() {
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

    private MessageProperties createPersonMessageProperties(String replyQueueName, QiwiRequest qiwiRequest) {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("type", qiwiRequest.getCommand().getRabbitType());
        messageProperties.setHeader("m_guid", "08.06.2020"); // легаси заголовок, должен присутствовать, а что в нём - не важно
        messageProperties.setHeader("reply-to", replyQueueName);
        messageProperties.setContentEncoding(StandardCharsets.UTF_8.name());
        return messageProperties;
    }

    private byte[] createPersonMessageBody(QiwiRequest request) {

        String bodyAsString = null;
        try {
            bodyAsString = mapper.writeValueAsString(createPersonRabbitRequest(request));
        } catch (JsonProcessingException e) {
            String message = "Rabbit request serialization failed";
            log.error(message, e);
            throw new ApiException(message, e, QiwiResultCode.OTHER_PROVIDER_ERROR);
        }
        log.info("body as String: {}", bodyAsString);

        return bodyAsString.getBytes(StandardCharsets.UTF_8);
    }

    private BasePerson createPersonRabbitRequest(QiwiRequest qiwiRequest) {
        BasePerson rabbitRequest = new BasePerson();
        rabbitRequest.setSystem_id(thisSystemId);

        BasePerson.Srch search = new BasePerson.Srch();
        search.setAccount_number(qiwiRequest.getAccount());
        search.setDept("ORESB");
        rabbitRequest.setSrch(search);
        return rabbitRequest;
    }

    private BasePerson receivePersonResponse(String replyQueueName) {
        Message responseMessage = safelyReceiveResponse(replyQueueName);
        String responseAsString = getMessageBodyAsString(responseMessage);
        return safelyDeserializePersonFromResponse(responseAsString);
    }

    private String getMessageBodyAsString(Message responseMessage) {
        String responseAsString = null;
        try {
            responseAsString = new String(responseMessage.getBody(), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            String message = "Unsupported encoding for incoming rabbit message";
            log.error(message + "; rabbit message: {}", responseMessage);
            throw new ApiException(message, QiwiResultCode.OTHER_PROVIDER_ERROR);
        }
        return responseAsString;
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

    private BasePerson safelyDeserializePersonFromResponse(String responseAsString) {
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

    private QiwiResponse getCheckQiwiResponse(QiwiRequest qiwiRequest, BasePerson rabbitResponse) {

        QiwiResponse response = new QiwiResponse();

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

}
