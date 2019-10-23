package com.redhat.syseng.tools.cloudevents.model;

import java.time.LocalDateTime;

import javax.json.JsonObject;

import io.cloudevents.v03.CloudEventImpl;

public class Message {

    public enum MessageType {
        SENT,
        RECEIVED
    }

    private String id;
    private LocalDateTime receivedAt;
    private CloudEventImpl<JsonObject> event;
    private MessageType type;

    public Message(CloudEventImpl<JsonObject> event, MessageType type) {
        this.receivedAt = LocalDateTime.now();
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

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public Message setEvent(CloudEventImpl<JsonObject> event) {
        this.event = event;
        return this;
    }

    public CloudEventImpl<JsonObject> getEvent() {
        return event;
    }

    public MessageType getType() {
        return type;
    }
}
