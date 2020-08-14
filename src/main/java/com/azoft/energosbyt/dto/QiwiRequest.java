package com.azoft.energosbyt.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class QiwiRequest {
  private Command command;
  private String txn_id;
  private String account;
  private BigDecimal sum;
}
