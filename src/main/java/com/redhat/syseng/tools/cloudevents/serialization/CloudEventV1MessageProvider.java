package com.redhat.syseng.tools.cloudevents.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.CDI;
import javax.json.Json;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import io.cloudevents.CloudEvent;
import io.cloudevents.format.Wire;
import io.cloudevents.v1.AttributesImpl;
import io.cloudevents.v1.CloudEventImpl;
import io.cloudevents.v1.http.Marshallers;
import io.cloudevents.v1.http.Unmarshallers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CloudEventV1MessageProvider implements MessageBodyReader<CloudEvent<AttributesImpl, Object>>,
                                                    MessageBodyWriter<CloudEvent<AttributesImpl, Object>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudEventV1MessageProvider.class);

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return CloudEvent.class.equals(type);
    }

    @Override
    public CloudEvent<AttributesImpl, Object> readFrom(Class<CloudEvent<AttributesImpl, Object>> type,
                                                       Type genericType,
                                                       Annotation[] annotations,
                                                       MediaType mediaType,
                                                       MultivaluedMap<String, String> httpHeaders,
                                                       InputStream entityStream) throws WebApplicationException {
        Validator validator = null;
        if (CDI.current().select(Validator.class).isResolvable()) {
            LOGGER.debug("Using existing validator instance");
            validator = CDI.current().select(Validator.class).get();
        }
        return Unmarshallers.binary(Object.class, validator)
            .withHeaders(() -> httpHeaders.entrySet().stream().collect(Collectors.toMap(Entry::getKey, this::getValue)))
            .withPayload(() -> Json.createReader(entityStream).readObject().toString())
            .unmarshal();
    }

    private String getValue(Entry<String, List<String>> entry) {
        if (entry.getValue() == null || entry.getValue().isEmpty()) {
            return null;
        }
        return entry.getValue().get(0);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return CloudEventImpl.class.equals(type);
    }

    @Override
    public void writeTo(CloudEvent<AttributesImpl, Object> cloudEvent, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws WebApplicationException, IOException {
        Wire<String, String, String> wire = Marshallers.binary().withEvent(() -> cloudEvent).marshal();
        wire.getHeaders().forEach((k, v) -> httpHeaders.add(k, v));
        if (wire.getPayload().isPresent()) {
            entityStream.write(wire.getPayload().get().getBytes());
        }
    }
}
