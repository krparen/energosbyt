package com.azoft.energosbyt.serializer;


import com.azoft.energosbyt.common.Field;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FieldListXmlSerializer /*extends StdSerializer<List<Field>>*/ {

//  public FieldListXmlSerializer() {
//    this(null);
//  }
//
//  public FieldListXmlSerializer(Class<List<Field>> t) {
//    super(t);
//  }
//
//  @Override
//  public void serialize(List<Field> fields, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
//    if (jsonGenerator instanceof ToXmlGenerator) {
//      final ToXmlGenerator xmlGenerator = (ToXmlGenerator) jsonGenerator;
//      jsonGenerator.writeStartObject();
//      for (int i = 0; i < fields.size(); i++) {
//        Field item = fields.get(i);
//        jsonGenerator.writeStartObject(item.getValue());
//        xmlGenerator.setNextIsAttribute(true);
//
//        jsonGenerator.writeFieldName("field" + i);
//        jsonGenerator.writeString(item.getValue());
//      }
//      jsonGenerator.writeEndObject();
//    } else {
//      jsonGenerator.writeStartObject();
//      for (int i = 0; i < fields.size(); i++) {
//        Field item = fields.get(i);
//        jsonGenerator.writeStartObject(item.getValue());
//
//        jsonGenerator.writeFieldName("field" + i);
//        jsonGenerator.writeString(item.getValue());
//      }
//    }
//  }
//
//  @Override
//  public Class<List<Field>> handledType() {
//    Class<List<Field>> typeClass = (Class<List<Field>>)(Class<?>)List.class;
//    return typeClass;
//  }
}
