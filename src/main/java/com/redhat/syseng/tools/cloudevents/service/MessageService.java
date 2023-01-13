package com.redhat.syseng.tools.cloudevents.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.syseng.tools.cloudevents.model.Message;
import com.redhat.syseng.tools.cloudevents.model.Message.MessageType;
import com.redhat.syseng.tools.cloudevents.model.PlayerMode;
import com.redhat.syseng.tools.cloudevents.resources.MessagesSocket;

import io.cloudevents.CloudEvent;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.vertx.core.eventbus.EventBus;

@ApplicationScoped
public class MessageService {

    private static final URI LOOPBACK_BASE_URI = URI.create("http://localhost:8080/");
    private static final String DEFAULT_BROKER = "default";
    private static final int MAX_SIZE = 200;
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);

    @ConfigProperty(name = "player.mode")
    Optional<PlayerMode> mode;

    @ConfigProperty(name = "broker.name", defaultValue = DEFAULT_BROKER)
    String brokerName;

    @ConfigProperty(name = "broker.namespace")
    Optional<String> brokerNamespace;

    @ConfigProperty(name = "broker.uri")
    Optional<String> brokerUri;

    private final List<Message> messages = new ArrayList<>();

    @Inject
    EventBus eventBus;

    @Inject
    KubernetesClient kClient;

    BrokerService brokerService;

    @PostConstruct
    public void init() {
        URI baseUri = LOOPBACK_BASE_URI;
        PlayerMode playerMode = mode.orElse(PlayerMode.KNATIVE);
        LOGGER.info("Player mode {}", playerMode);
        if (PlayerMode.KNATIVE.equals(playerMode)) {
            if (brokerUri.isPresent()) {
                baseUri = URI.create(brokerUri.get());
            } else if (kClient.getMasterUrl() != null) {
                var namespace = brokerNamespace.orElse(kClient.getNamespace());
                baseUri = UriBuilder.fromUri("http://broker-ingress.knative-eventing.svc.cluster.local/{namespace}/{broker}").build(namespace, brokerName);
            } else {
                LOGGER.error("Unable to define the default namespace");
                throw new IllegalStateException("Unable to define the default namespace. The Kubernetes Client cannot get the masterUrl");
            }
        }
        brokerService = RestClientBuilder.newBuilder().baseUri(baseUri).build(BrokerService.class);
        LOGGER.info("Broker endpoint: {}", baseUri);
    }

    public void send(CloudEvent event) {
        brokerService.send(event).whenComplete((response, throwable) -> {
            if (throwable != null) {
                LOGGER.error("Unable to send cloudEvent", throwable);
                newEvent(event, MessageType.FAILED);
            } else if (Response.Status.BAD_REQUEST.getStatusCode() <= response.getStatus()) {
                LOGGER.error("Unable to send cloudEvent. StatusCode: {}", response.getStatus());
                newEvent(event, MessageType.FAILED);
            } else {
                LOGGER.debug("Successfully sent cloudevent {}", event);
                newEvent(event, MessageType.SENT);
            }
        });
    }

    public void receive(CloudEvent event) {
        newEvent(event, MessageType.RECEIVED);
    }

    private void newEvent(CloudEvent event, MessageType type) {
        messages.add(new Message(event, type));
        if (messages.size() > MAX_SIZE) {
            messages.stream().sorted(Comparator.comparing(Message::getReceivedAt)).limit(messages.size() - MAX_SIZE)
                    .forEach(messages::remove);
        }
        eventBus.publish(MessagesSocket.MESSAGES_ADDRESS, event.getId());
    }

    public List<Message> list(int page, int size) {
        return messages.stream().sorted(Comparator.comparing(Message::getReceivedAt).reversed()).skip(page * size)
                .limit(page * size + size).collect(Collectors.toList());
    }

    public void clear() {
        messages.clear();
    }

}
