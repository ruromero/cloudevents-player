package com.redhat.syseng.tools.cloudevents.resources;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.redhat.syseng.tools.cloudevents.service.MessageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cloudevents.v03.CloudEventImpl;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageReceiverResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageReceiverResource.class);

    @Inject
    MessageService msgService;

    @Inject
    Validator validator;

    @POST
    public CompletionStage<Response> receive(CloudEventImpl<JsonObject> object) {
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.debug("Received event: {}", object.getAttributes().getId());
            Set<ConstraintViolation<CloudEventImpl<JsonObject>>> violations = validator.validate(object);
            if(!violations.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity(violations).build();
            }
            msgService.receive(object);
            return Response.accepted().build();
        });
    }
}
