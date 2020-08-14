package com.azoft.energosbyt.controller;

import com.azoft.energosbyt.dto.QiwiRequest;
import com.azoft.energosbyt.dto.QiwiResponse;
import com.azoft.energosbyt.service.RabbitRequestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class QiwiController {

  private final RabbitRequestService rabbitRequestService;

  @RequestMapping(value = "/api/checkOrPay", produces = MediaType.APPLICATION_XML_VALUE)
  public QiwiResponse getOrPay(QiwiRequest request) throws JsonProcessingException {

    return rabbitRequestService.sendRequestToQueue(request);
  }

}
