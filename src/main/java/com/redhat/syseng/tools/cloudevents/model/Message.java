package com.redhat.syseng.tools.cloudevents.model;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Map;

import io.cloudevents.CloudEvent;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;

@RegisterForReflection
public class Message {

    public enum MessageType {
        SENT,
        RECEIVED
    }

    private ZonedDateTime receivedAt;
    private CloudEvent event;
    private MessageType type;

    public Message(CloudEvent event, MessageType type) {
        this.receivedAt = ZonedDateTime.now();
        this.event = event;
        this.type = type;
    }

    public String getId() {
        return event.getId();
    }

    public ZonedDateTime getReceivedAt() {
        return receivedAt;
    }

    public Message setEvent(CloudEvent event) {
        this.event = event;
        return this;
    }

    public String getSubject() {
        return event.getSubject();
    }

    public String getEventType() {
        return event.getType();
    }

    public URI getSource() {
        return event.getSource();
    }

    public Map getData() {
        if (event.getData() == null) {
            return null;
        }
        return Json.decodeValue(Buffer.buffer(event.getData()), Map.class);
    }

    public MessageType getType() {
        return type;
    }
}
