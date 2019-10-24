package com.redhat.syseng.tools.cloudevents.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import io.cloudevents.v03.CloudEventBuilder;
import io.cloudevents.v03.CloudEventImpl;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class CloudEventV3MessageProvider implements MessageBodyReader<CloudEventImpl<JsonObject>>,
                                                    MessageBodyWriter<CloudEventImpl<JsonObject>> {

    private static final String EVENT_SOURCE_HEADER = "ce-source";
    private static final String EVENT_ID_HEADER = "ce-id";
    private static final String EVENT_TYPE_HEADER = "ce-type";
    private static final String EVENT_SPECVERSION_HEADER = "ce-specversion";

    @Inject
    Validator validator;

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == CloudEventImpl.class;
    }

    @Override
    public CloudEventImpl<JsonObject> readFrom(Class<CloudEventImpl<JsonObject>> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws WebApplicationException {
        CloudEventBuilder<JsonObject> builder = CloudEventBuilder.builder(validator);
        httpHeaders.forEach((header, values) -> setEventValue(header, values, builder));
        builder.withData(Json.createReader(entityStream).readObject());
        CloudEventImpl<JsonObject> event = builder.build();
        return event;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == CloudEventImpl.class;
    }

    @Override
    public void writeTo(CloudEventImpl<JsonObject> cloudEvent, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        httpHeaders.add(EVENT_ID_HEADER, cloudEvent.getAttributes().getId());
        httpHeaders.add(EVENT_TYPE_HEADER, cloudEvent.getAttributes().getType());
        httpHeaders.add(EVENT_SOURCE_HEADER, cloudEvent.getAttributes().getSource());
        httpHeaders.add(EVENT_SPECVERSION_HEADER, CloudEventBuilder.SPEC_VERSION);
        if (cloudEvent.getData().isPresent()) {
            entityStream.write(cloudEvent.getData().get().toString().getBytes());
        }
    }

    private CloudEventBuilder<JsonObject> setEventValue(String header, List<String> values, CloudEventBuilder<JsonObject> builder) {
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
