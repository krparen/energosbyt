package com.azoft.energosbyt.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class QiwiRequest {

  private static final String dateTimeFormat = "yyyyMMddHHmmss";

  private Command command;
  private String txn_id;
  private String account;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateTimeFormat, timezone = "Europe/Moscow") // отвечает за сериализацию
  @DateTimeFormat(pattern = dateTimeFormat) // отвечает за десериализацию
  private LocalDateTime txn_date;
  private BigDecimal sum;
}
