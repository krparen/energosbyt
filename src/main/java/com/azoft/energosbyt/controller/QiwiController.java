package com.azoft.energosbyt.controller;

import com.azoft.energosbyt.dto.QiwiRequest;
import com.azoft.energosbyt.dto.QiwiResponse;
import com.azoft.energosbyt.service.RabbitRequestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class QiwiController {

  private final RabbitRequestService rabbitRequestService;

  @RequestMapping(value = "/api/checkOrPay")
  public ResponseEntity<QiwiResponse> getOrPay(QiwiRequest request) throws JsonProcessingException {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("application/xml;charset=UTF-8"));

    QiwiResponse qiwiResponse = rabbitRequestService.sendRequestToQueue(request);
    ResponseEntity<QiwiResponse> result = ResponseEntity
            .status(HttpStatus.OK)
            .headers(headers)
            .body(qiwiResponse);

    return result;
  }

}
