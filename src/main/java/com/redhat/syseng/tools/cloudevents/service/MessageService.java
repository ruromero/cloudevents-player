package com.redhat.syseng.tools.cloudevents.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

import com.redhat.syseng.tools.cloudevents.model.Message;
import com.redhat.syseng.tools.cloudevents.resources.MessagesSocket;
import io.cloudevents.v03.CloudEventImpl;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

@ApplicationScoped
public class MessageService {

    private static final int MAX_SIZE = 200;
    private final Map<String, Message> messages = new ConcurrentHashMap<>();

    @Inject
    Vertx vertx;

    public void add(CloudEventImpl<JsonObject> event) {
        Message msg = messages.get(event.getAttributes().getId());
        if (msg == null) {
            msg = new Message(event);
        } else {
            msg.setEvent(event);
            msg.setUpdatedAt(LocalDateTime.now());
        }
        messages.put(msg.getId(), msg);
        if (messages.size() > MAX_SIZE) {
            messages.values()
                .stream()
                .sorted(Comparator.comparing(Message::getReceivedAt))
                .limit(messages.size() - MAX_SIZE)
                .forEach(m -> messages.remove(m.getId()));
        }
        vertx.eventBus().publish(MessagesSocket.MESSAGES_ADDRESS, event.getAttributes().getId());
    }

    public CloudEventImpl<JsonObject> get(String id) {
        Message message = messages.get(id);
        if (message == null) {
            return null;
        }
        return message.getEvent();
    }

    public List<CloudEventImpl<JsonObject>> list(int page, int size) {
        return messages.values().stream().skip(page * size).limit(page * size + size).map(Message::getEvent).collect(Collectors.toList());
    }

    public void clear() {
        messages.clear();
    }
}
