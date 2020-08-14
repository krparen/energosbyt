package com.azoft.energosbyt.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiException extends RuntimeException {

  private QiwiResultCode errorCode;

  public ApiException(String s, QiwiResultCode errorCode) {
    super(s);
    this.errorCode = errorCode;
  }

  public ApiException(String s, Throwable throwable, QiwiResultCode errorCode) {
    super(s, throwable);
    this.errorCode = errorCode;
  }
}
