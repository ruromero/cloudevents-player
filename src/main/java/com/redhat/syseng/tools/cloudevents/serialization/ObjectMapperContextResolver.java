package com.redhat.syseng.tools.cloudevents.serialization;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr353.JSR353Module;

@Provider
public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

  private final ObjectMapper mapper;

  public ObjectMapperContextResolver() {
    mapper = new ObjectMapper();
    mapper.registerModule(new Jdk8Module()); // Serialize Optional
    mapper.registerModule(new JSR353Module()); // Serialize JsonObject
    mapper.registerModule(new JavaTimeModule()); // Serialize DateTime
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  @Override
  public ObjectMapper getContext(Class<?> type) {
    return mapper;
  }

}