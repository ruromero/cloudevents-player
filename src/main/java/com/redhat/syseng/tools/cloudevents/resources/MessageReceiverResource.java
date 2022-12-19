package com.redhat.syseng.tools.cloudevents.resources;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.ResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.syseng.tools.cloudevents.service.MessageService;

import io.cloudevents.CloudEvent;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageReceiverResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageReceiverResource.class);

    @Inject
    MessageService msgService;

    @Inject
    Validator validator;

    @Inject
    ObjectMapper mapper;

    @POST
    @ResponseStatus(202)
    public CompletionStage<Void> receive(CloudEvent object) {
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.debug("Received event: {}", object.getId());
            Set<ConstraintViolation<CloudEvent>> violations = validator.validate(object);
            if (!violations.isEmpty()) {
                try {
                    throw new BadRequestException(mapper.writeValueAsString(violations));
                } catch (JsonProcessingException e) {
                    throw new InternalServerErrorException(e);
                }
            }
            msgService.receive(object);
            return null;
        });
    }

}
