package com.redhat.syseng.tools.cloudevents.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestResponse;

import io.cloudevents.CloudEvent;
import io.cloudevents.jackson.JsonFormat;

@Path("/")
@RegisterClientHeaders
@RegisterRestClient
public interface BrokerService {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    RestResponse<Void> sendBinary(CloudEvent payload);

    @POST
    @Consumes(JsonFormat.CONTENT_TYPE)
    @Produces(MediaType.APPLICATION_JSON)
    RestResponse<Void> sendStructured(CloudEvent payload);
}
