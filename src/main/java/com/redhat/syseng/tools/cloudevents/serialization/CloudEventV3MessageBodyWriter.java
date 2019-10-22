package com.redhat.syseng.tools.cloudevents.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.json.JsonObject;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import io.cloudevents.v03.CloudEventBuilder;
import io.cloudevents.v03.CloudEventImpl;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class CloudEventV3MessageBodyWriter implements MessageBodyWriter<CloudEventImpl<JsonObject>>,
                                                      CloudEventV3Serializable {

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
}
