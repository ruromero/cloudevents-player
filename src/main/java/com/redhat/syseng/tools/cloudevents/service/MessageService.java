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
import io.cloudevents.CloudEvent;
import io.cloudevents.v1.AttributesImpl;
import io.vertx.core.eventbus.EventBus;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class MessageService {

    private static final int MAX_SIZE = 200;
    private final List<Message> messages = new ArrayList<>();

    @Inject
    EventBus eventBus;

    @Inject
    @RestClient
    BrokerService brokerService;

    public void send(CloudEvent<AttributesImpl, JsonObject> event) {
        newEvent(event, MessageType.SENT);
        brokerService.sendEvent(event);
    }

    public void receive(CloudEvent<AttributesImpl, JsonObject> event) {
        newEvent(event, MessageType.RECEIVED);
    }

    private void newEvent(CloudEvent<AttributesImpl, JsonObject> event, MessageType type) {
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
