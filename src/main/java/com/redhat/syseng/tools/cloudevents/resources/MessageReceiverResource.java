package com.redhat.syseng.tools.cloudevents.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
import javax.json.JsonObject;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageResource.class);

    @Inject
    MessageService msgService;

    @POST
    public CompletionStage<Response> hello(CloudEventImpl<JsonObject> object) {
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.info("Received: {}, {}, {}", object.getAttributes().getId(), object.getAttributes().getType(), object.getAttributes().getSource());
            msgService.add(object);
            return Response.accepted().build();
        });
    }
}
