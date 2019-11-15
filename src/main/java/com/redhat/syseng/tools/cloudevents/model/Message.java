package com.redhat.syseng.tools.cloudevents.model;

import java.time.ZonedDateTime;

import javax.json.JsonObject;

import io.cloudevents.Attributes;
import io.cloudevents.CloudEvent;

public class Message {

    public enum MessageType {
        SENT,
        RECEIVED
    }

    private String id;
    private ZonedDateTime receivedAt;
    private CloudEvent<? extends Attributes, JsonObject> event;
    private MessageType type;

    public Message(CloudEvent<? extends Attributes, JsonObject> event, MessageType type) {
        this.receivedAt = ZonedDateTime.now();
        this.event = event;
        this.type = type;
        this.id = event.getAttributes().getId();
    }

    public Message setId(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }

    public ZonedDateTime getReceivedAt() {
        return receivedAt;
    }

    public Message setEvent(CloudEvent<? extends Attributes, JsonObject> event) {
        this.event = event;
        return this;
    }

    public CloudEvent<? extends Attributes, JsonObject> getEvent() {
        return event;
    }

    public MessageType getType() {
        return type;
    }
}
