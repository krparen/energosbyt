package com.azoft.energosbyt.service.impl;

import com.azoft.energosbyt.common.Command;
import com.azoft.energosbyt.common.Field;
import com.azoft.energosbyt.dto.BasePayment;
import com.azoft.energosbyt.dto.QiwiRequest;
import com.azoft.energosbyt.dto.QiwiResponse;
import com.azoft.energosbyt.service.RabbitRequestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitRequestServiceImpl implements RabbitRequestService {

  private final AmqpTemplate template;
  private final AmqpAdmin rabbitAdmin;
  private final ObjectMapper mapper;

  @Value("${energosbyt.rabbit.request.queue-name}")
  private String requestQueueName;
  @Value("${energosbyt.rabbit.request.timeout-in-ms}")
  private Long requestTimeout;

  @Override
  public QiwiResponse sendRequestToQueue(QiwiRequest qiwiRequest) throws JsonProcessingException {

    String replyQueueName = declareReplyQueue();

    MessageProperties messageProperties = createMessageProperties(replyQueueName);
    byte[] body = createMessageBody(qiwiRequest);
    Message requestMessage = new Message(body, messageProperties);
    template.send(requestQueueName, requestMessage);

    BasePayment rabbitResponse = receiveResponse(replyQueueName);

    rabbitAdmin.deleteQueue(replyQueueName);

    return getMockQiwiResponse(qiwiRequest);
  }

  private byte[] createMessageBody(QiwiRequest request) throws JsonProcessingException {
    String bodyAsString = mapper.writeValueAsString(createRabbitRequest(request));
    log.info("body as String: {}", bodyAsString);

    return bodyAsString.getBytes(StandardCharsets.UTF_8);
  }

  private MessageProperties createMessageProperties(String replyQueueName) {
    MessageProperties messageProperties = new MessageProperties();
    messageProperties.setReplyTo(replyQueueName);
    messageProperties.setContentEncoding(StandardCharsets.UTF_8.name());
    return messageProperties;
  }

  private BasePayment receiveResponse(String replyQueueName) throws JsonProcessingException {
    Message responseMessage = template.receive(replyQueueName, requestTimeout);
    String responseAsString = new String(responseMessage.getBody());
    BasePayment response = mapper.readValue(responseAsString, BasePayment.class);
    log.info("response from rabbit: {}", response);
    return response;
  }

  private String declareReplyQueue() {
    String replyQueueName = UUID.randomUUID().toString();
    Queue newQueue = new Queue(replyQueueName);
    return rabbitAdmin.declareQueue(newQueue);
  }

  private BasePayment createRabbitRequest(QiwiRequest qiwiRequest) {
    BasePayment rabbitRequest = new BasePayment();
    BasePayment.Srch search = new BasePayment.Srch();
    search.setAccount_id(qiwiRequest.getAccount());
    search.setLimit("SomeLimit");
    rabbitRequest.setSrch(search);
    return rabbitRequest;
  }

  private QiwiResponse getMockQiwiResponse(QiwiRequest request) {
    QiwiResponse response = new QiwiResponse();
    response.setComment("Тестовый фиксированный ответ");
    response.setOsmp_txn_id("13513416");
    response.setResult(0);

    if (request.getCommand() == Command.check) {
      Field field1 = new Field();
      field1.setName("name1");
      field1.setType("type1");
      field1.setValue("value1");

      Field field2 = new Field();
      field2.setName("name2");
      field2.setType("type2");
      field2.setValue("value2");

      response.setFields(List.of(field1, field2));
    }

    if (request.getCommand() == Command.pay) {
      response.setPrv_txn("49472744");
      BigDecimal sum = BigDecimal.TEN;
      sum = sum.setScale(2);
      response.setSum(sum);
    }

    return response;
  }
}
