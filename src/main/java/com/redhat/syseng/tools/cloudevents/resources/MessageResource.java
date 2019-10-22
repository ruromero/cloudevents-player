package com.redhat.syseng.tools.cloudevents.resources;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.redhat.syseng.tools.cloudevents.service.MessageService;

import io.cloudevents.v03.CloudEventImpl;

@Path("/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageResource {

    @Inject
    MessageService msgService;

    @GET
    @Path("/{id}")
    public CompletionStage<CloudEventImpl<JsonObject>> get(@PathParam("id") String id) {
        return CompletableFuture.supplyAsync(() -> msgService.get(id));
    }

    @GET
    public CompletionStage<List<CloudEventImpl<JsonObject>>> list(@QueryParam("page") @DefaultValue("0") Integer page,
                                                                  @QueryParam("size") @DefaultValue("10") Integer size) {
        return CompletableFuture.supplyAsync(() -> msgService.list(page, size));
    }

    @DELETE
    public CompletionStage<Response> clear() {
        return CompletableFuture.supplyAsync(() -> {
            msgService.clear();
            return Response.accepted().build();
        });
    }
}
