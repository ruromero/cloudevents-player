package com.redhat.syseng.tools.cloudevents.service;

import java.util.concurrent.CompletionStage;

import javax.json.JsonObject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.cloudevents.CloudEvent;
import io.cloudevents.v1.AttributesImpl;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/")
@RegisterRestClient(configKey = "brokerUrl")
public interface BrokerService {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<Response> sendEvent(CloudEvent<AttributesImpl, JsonObject> payload);
}
