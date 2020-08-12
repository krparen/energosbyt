package com.azoft.energosbyt.serializer;

import com.azoft.energosbyt.common.Field;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import java.io.IOException;

public class FieldXmlSerializer extends StdSerializer<Field> {

  public FieldXmlSerializer() {
    this(null);
  }

  public FieldXmlSerializer(Class<Field> t) {
    super(t);
  }

  @Override
  public void serialize(Field field, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

    if (jsonGenerator instanceof ToXmlGenerator) {
      final ToXmlGenerator xmlGenerator = (ToXmlGenerator) jsonGenerator;
      xmlGenerator.writeStartObject();
      xmlGenerator.writeEndObject();
    } else {

      jsonGenerator.writeStartObject();
      jsonGenerator.writeStringField("name", field.getName());
      jsonGenerator.writeStringField("type", field.getType());
      jsonGenerator.writeStringField("value", field.getValue());
      jsonGenerator.writeEndObject();
    }

  }
}
