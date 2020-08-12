package com.azoft.energosbyt.dto;

import com.azoft.energosbyt.common.Field;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "response")
public class QiwiResponse {
  private String osmp_txn_id;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String prv_txn;
  private Integer result;
  private String comment;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BigDecimal sum;

  @JacksonXmlElementWrapper(localName = "fields")
  @JacksonXmlProperty(localName = "field")
  private List<Field> fields;

}
