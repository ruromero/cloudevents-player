package com.redhat.syseng.tools.cloudevents.resources;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.redhat.syseng.tools.cloudevents.model.Message;
import com.redhat.syseng.tools.cloudevents.service.MessageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cloudevents.v03.CloudEventImpl;

@Path("/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageResource.class);

    @Inject
    MessageService msgService;

    @Inject
    Validator validator;

    @GET
    public CompletionStage<List<Message>> list(@QueryParam("page") @DefaultValue("0") Integer page,
            @QueryParam("size") @DefaultValue("10") Integer size) {
        return CompletableFuture.supplyAsync(() -> msgService.list(page, size));
    }

    @POST
    public CompletionStage<Response> sendEvent(CloudEventImpl<JsonObject> object) {
        return CompletableFuture.supplyAsync(() -> {
            Set<ConstraintViolation<CloudEventImpl<JsonObject>>> violations = validator.validate(object);
            if(!violations.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity(violations).build();
            }
            LOGGER.debug("New event to send: {} - {}", object.getAttributes().getId(), object.getData().get());
            msgService.send(object);
            return Response.accepted().build();
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
