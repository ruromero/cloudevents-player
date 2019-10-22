package com.redhat.syseng.tools.cloudevents.serialization;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import io.cloudevents.v03.CloudEventBuilder;
import io.cloudevents.v03.CloudEventImpl;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class CloudEventV3MessageBodyReader implements MessageBodyReader<CloudEventImpl<JsonObject>>,
                                                      CloudEventV3Serializable {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == CloudEventImpl.class;
    }

    @Override
    public CloudEventImpl<JsonObject> readFrom(Class<CloudEventImpl<JsonObject>> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws WebApplicationException {
        CloudEventBuilder<JsonObject> builder = CloudEventBuilder.builder();
        httpHeaders.forEach((header, values) -> setEventValue(header, values, builder));
        builder.withData(Json.createReader(entityStream).readObject());
        CloudEventImpl<JsonObject> event = builder.build();
        return event;
    }

    private CloudEventBuilder setEventValue(String header, List<String> values, CloudEventBuilder builder) {
        header = header.toLowerCase();
        if (values == null || values.size() != 1) {
            return builder;
        }
        String value = values.get(0);
        switch (header) {
            case EVENT_ID_HEADER:
                return builder.withId(value);
            case EVENT_SPECVERSION_HEADER:
                if (!CloudEventBuilder.SPEC_VERSION.equals(value)) {
                    throw new IllegalArgumentException("SpecVersion must be " + CloudEventBuilder.SPEC_VERSION);
                }
                return builder;
            case EVENT_SOURCE_HEADER:
                return builder.withSource(URI.create(value));
            case EVENT_TYPE_HEADER:
                return builder.withType(value);
            default:
                return builder;
        }
    }
}
