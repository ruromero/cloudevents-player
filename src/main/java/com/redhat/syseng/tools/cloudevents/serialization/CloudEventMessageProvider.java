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

import io.cloudevents.Attributes;
import io.cloudevents.CloudEvent;
import io.cloudevents.format.Wire;
import io.cloudevents.format.builder.HeadersStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CloudEventMessageProvider implements MessageBodyReader<CloudEvent<? extends Attributes, Object>>,
                                                  MessageBodyWriter<CloudEvent<? extends Attributes, Object>> {

    private static final String SPECVERSION_HEADER = "ce-specversion";
    private static final String V1 = "1.0";
    private static final String V03 = "0.3";
    private static final String V02 = "0.2";
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudEventMessageProvider.class);

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return CloudEvent.class.equals(type);
    }

    @Override
    public CloudEvent<? extends Attributes, Object> readFrom(Class<CloudEvent<? extends Attributes, Object>> type,
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
        List<String> specversionHeader = httpHeaders.get(SPECVERSION_HEADER);
        if (specversionHeader.isEmpty()) {
            throw new IllegalArgumentException("Missing Specversion header");
        }
        String specversion = specversionHeader.get(0);
        HeadersStep<? extends Attributes, Object, String> headersStep;
        switch (specversion) {
            case V1:
                headersStep = io.cloudevents.v1.http.Unmarshallers.binary(Object.class, validator);
                break;
            case V03:
                headersStep = io.cloudevents.v03.http.Unmarshallers.binary(Object.class, validator);
                break;
            case V02:
                headersStep = io.cloudevents.v02.http.Unmarshallers.binary(Object.class, validator);
                break;
            default:
                throw new IllegalArgumentException("Unsupported specversion: " + specversion);
        }

        return headersStep.withHeaders(() -> httpHeaders.entrySet().stream().collect(Collectors.toMap(Entry::getKey, this::getValue)))
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
        return io.cloudevents.v1.CloudEventImpl.class.equals(type)
            || io.cloudevents.v03.CloudEventImpl.class.equals(type)
            || io.cloudevents.v02.CloudEventImpl.class.equals(type);
    }

    @Override
    public void writeTo(CloudEvent<? extends Attributes, Object> cloudEvent, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws WebApplicationException, IOException {
        Wire<String, String, String> wire = null;
        if (cloudEvent.getAttributes() instanceof io.cloudevents.v1.AttributesImpl) {
            wire = io.cloudevents.v1.http.Marshallers.binary().withEvent(() -> (CloudEvent<io.cloudevents.v1.AttributesImpl, Object>) cloudEvent).marshal();
        } else if (cloudEvent.getAttributes() instanceof io.cloudevents.v03.AttributesImpl) {
            wire = io.cloudevents.v03.http.Marshallers.binary().withEvent(() -> (CloudEvent<io.cloudevents.v03.AttributesImpl, Object>) cloudEvent).marshal();
        } else if (cloudEvent.getAttributes() instanceof io.cloudevents.v02.AttributesImpl) {
            wire = io.cloudevents.v02.http.Marshallers.binary().withEvent(() -> (CloudEvent<io.cloudevents.v02.AttributesImpl, Object>) cloudEvent).marshal();
        }
        wire.getHeaders().forEach(httpHeaders::add);
        if (wire.getPayload().isPresent()) {
            entityStream.write(wire.getPayload().get().getBytes());
        }
    }
}
