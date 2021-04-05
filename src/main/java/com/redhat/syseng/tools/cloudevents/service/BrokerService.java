package com.redhat.syseng.tools.cloudevents.service;

import java.util.concurrent.CompletionStage;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.cloudevents.CloudEvent;

@Path("/")
public interface BrokerService {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<Response> send(CloudEvent payload);
}
