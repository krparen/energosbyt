package com.azoft.energosbyt.controller;

import com.azoft.energosbyt.common.Command;
import com.azoft.energosbyt.common.Field;
import com.azoft.energosbyt.dto.QiwiRequest;
import com.azoft.energosbyt.dto.QiwiResponse;
import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class QiwiController {

  @RequestMapping(value = "/api/checkOrPay", produces = MediaType.APPLICATION_XML_VALUE)
  public QiwiResponse getOrPay(QiwiRequest request) {

    QiwiResponse response = new QiwiResponse();
    response.setComment("Тестовый фиксированный ответ");
    response.setOsmp_txn_id("13513416");
    response.setResult(0);
    if (request.getCommand() == Command.pay) {
      response.setPrv_txn("49472744");
      BigDecimal sum = BigDecimal.TEN;
      sum = sum.setScale(2);
      response.setSum(sum);
    }

    Field field1 = new Field();
    field1.setName("name1");
    field1.setType("type1");
    field1.setValue("value1");

    Field field2 = new Field();
    field2.setName("name2");
    field2.setType("type2");
    field2.setValue("value2");

    response.setFields(List.of(field1, field2));

    return response;
  }
}
