package com.redhat.syseng.tools.cloudevents.service;

import java.util.concurrent.CompletionStage;

import javax.json.JsonObject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.cloudevents.Attributes;
import io.cloudevents.CloudEvent;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/")
@RegisterRestClient
public interface BrokerService {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<Response> send(CloudEvent<? extends Attributes, JsonObject> payload);
}
