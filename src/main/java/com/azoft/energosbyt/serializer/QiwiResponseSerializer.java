package com.azoft.energosbyt.serializer;

import com.azoft.energosbyt.dto.Field;
import com.azoft.energosbyt.dto.QiwiResponse;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import java.io.IOException;
import java.util.List;

public class QiwiResponseSerializer extends StdSerializer<QiwiResponse> {

    public QiwiResponseSerializer() {
        this(null);
    }

    public QiwiResponseSerializer(Class<QiwiResponse> t) {
        super(t);
    }

    @Override
    public void serialize(QiwiResponse qiwiResponse, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (jsonGenerator instanceof ToXmlGenerator) {
            final ToXmlGenerator xmlGenerator = (ToXmlGenerator) jsonGenerator;
            xmlGenerator.writeStartObject();

            xmlGenerator.writeStringField("osmp_txn_id", qiwiResponse.getOsmp_txn_id());

            if (qiwiResponse.getPrv_txn() != null) {
                xmlGenerator.writeFieldName("prv_txn");
                xmlGenerator.writeNumber(qiwiResponse.getPrv_txn());
            }

            xmlGenerator.writeFieldName("result");
            xmlGenerator.writeNumber(qiwiResponse.getResult());

            xmlGenerator.writeStringField("comment", qiwiResponse.getComment());
            if (qiwiResponse.getSum() != null) {
                xmlGenerator.writeStringField("sum", qiwiResponse.getSum().toString());
            }



            writeFields(xmlGenerator, qiwiResponse.getFields());


            xmlGenerator.writeEndObject();
        }
    }

    private void writeFields(ToXmlGenerator xmlGenerator, List<Field> fields) throws IOException {

        if (fields == null || fields.isEmpty()) {
            return;
        }

        xmlGenerator.writeObjectFieldStart("fields");

        for (int i = 0; i < fields.size(); i++) {
            xmlGenerator.writeFieldName("field" + (i + 1));
            xmlGenerator.writeStartObject();
            xmlGenerator.setNextIsAttribute(true);
            xmlGenerator.writeStringField("name", fields.get(i).getName());
            xmlGenerator.writeStringField("type", fields.get(i).getType());
            xmlGenerator.setNextIsAttribute(false);
            xmlGenerator.writeRaw(fields.get(i).getValue());
            xmlGenerator.writeEndObject();
        }

        xmlGenerator.writeEndObject();
    }

}
