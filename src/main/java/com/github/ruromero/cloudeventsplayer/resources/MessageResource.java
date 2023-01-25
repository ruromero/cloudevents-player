package com.github.ruromero.cloudeventsplayer.resources;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.ResponseStatus;
import org.jboss.resteasy.reactive.RestHeader;
import org.jboss.resteasy.reactive.RestQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ruromero.cloudeventsplayer.model.Message;
import com.github.ruromero.cloudeventsplayer.service.MessageService;

import io.cloudevents.CloudEvent;
import io.cloudevents.jackson.JsonFormat;
import io.quarkus.runtime.annotations.RegisterForReflection;

@Path("/messages")
@Consumes({MediaType.APPLICATION_JSON, JsonFormat.CONTENT_TYPE})
@Produces(MediaType.APPLICATION_JSON)
@RegisterForReflection
public class MessageResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageResource.class);

    @Inject
    MessageService msgService;

    @Inject
    Validator validator;
    
    @Inject
    ObjectMapper mapper;

    @GET
    public List<Message> list(@RestQuery @DefaultValue("0") Integer page,
                                               @RestQuery @DefaultValue("10") Integer size) {
        return msgService.list(page, size);
    }

    @POST
    @ResponseStatus(202)
    public void sendEvent(CloudEvent object, @RestHeader String contentType) {
        Set<ConstraintViolation<CloudEvent>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            LOGGER.debug("Validation error {}", violations);
            try {
                throw new BadRequestException(mapper.writeValueAsString(violations));
            } catch (JsonProcessingException e) {
                throw new InternalServerErrorException(e);
            }
        }
        LOGGER.debug("New event to send: {}", object);
        msgService.send(object, JsonFormat.CONTENT_TYPE.equalsIgnoreCase(contentType));
    }
    

    @DELETE
    @ResponseStatus(202)
    public void clear() {
        msgService.clear();
    }
}
