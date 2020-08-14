package com.azoft.energosbyt.exception;

import com.azoft.energosbyt.dto.QiwiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApplicationExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<QiwiResponse> handleApiException(ApiException exception) {
    QiwiResponse response = new QiwiResponse();
    if (exception.getErrorCode() != null) {
      response.setResult(exception.getErrorCode().getNumericCode());
    }

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_XML)
        .body(response);
  }
}
