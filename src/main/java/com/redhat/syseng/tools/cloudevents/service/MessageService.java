package com.redhat.syseng.tools.cloudevents.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

import com.redhat.syseng.tools.cloudevents.model.Message;
import com.redhat.syseng.tools.cloudevents.model.Message.MessageType;
import com.redhat.syseng.tools.cloudevents.resources.MessagesSocket;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.cloudevents.v03.CloudEventImpl;
import io.vertx.core.eventbus.EventBus;

@ApplicationScoped
public class MessageService {

    private static final int MAX_SIZE = 200;
    private final List<Message> messages = new ArrayList<>();

    @Inject
    EventBus eventBus;

    @ConfigProperty(name = "broker.url", defaultValue = "default-broker.default.svc.cluster.local")
    String brokerUrl;

    @Inject
    @RestClient
    BrokerService brokerService;

    public void send(CloudEventImpl<JsonObject> event) {
        newEvent(event, MessageType.SENT);
        brokerService.sendEvent(event);
    }

    public void receive(CloudEventImpl<JsonObject> event) {
        newEvent(event, MessageType.RECEIVED);
    }

    private void newEvent(CloudEventImpl<JsonObject> event, MessageType type) {
        messages.add(new Message(event, type));
        if (messages.size() > MAX_SIZE) {
            messages.stream().sorted(Comparator.comparing(Message::getReceivedAt)).limit(messages.size() - MAX_SIZE)
                    .forEach(m -> messages.remove(m));
        }
        eventBus.publish(MessagesSocket.MESSAGES_ADDRESS, event.getAttributes().getId());
    }

    public List<Message> list(int page, int size) {
        return messages.stream().sorted(Comparator.comparing(Message::getReceivedAt).reversed()).skip(page * size)
                .limit(page * size + size).collect(Collectors.toList());
    }

    public void clear() {
        messages.clear();
    }
}
