package com.redhat.syseng.tools.cloudevents.resources;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.reactive.ResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.syseng.tools.cloudevents.model.Message;
import com.redhat.syseng.tools.cloudevents.service.MessageService;

import io.cloudevents.CloudEvent;

@Path("/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageResource.class);

    @Inject
    MessageService msgService;

    @Inject
    Validator validator;
    
    @Inject
    ObjectMapper mapper;

    @GET
    public CompletionStage<List<Message>> list(@QueryParam("page") @DefaultValue("0") Integer page,
                                               @QueryParam("size") @DefaultValue("10") Integer size) {
        return CompletableFuture.supplyAsync(() -> msgService.list(page, size));
    }

    @POST
    @ResponseStatus(202)
    public CompletableFuture<Void> sendEvent(CloudEvent object) {
        return CompletableFuture.supplyAsync(() -> {
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
            msgService.send(object);
            return null;
        });
    }

    @DELETE
    public CompletionStage<Response> clear() {
        return CompletableFuture.supplyAsync(() -> {
            msgService.clear();
            return Response.accepted().build();
        });
    }
}
