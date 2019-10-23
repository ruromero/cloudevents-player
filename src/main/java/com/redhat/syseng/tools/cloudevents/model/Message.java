package com.redhat.syseng.tools.cloudevents.model;

import java.time.ZonedDateTime;

import javax.json.JsonObject;

import io.cloudevents.v03.CloudEventImpl;

public class Message {

    public enum MessageType {
        SENT,
        RECEIVED
    }

    private String id;
    private ZonedDateTime receivedAt;
    private CloudEventImpl<JsonObject> event;
    private MessageType type;

    public Message(CloudEventImpl<JsonObject> event, MessageType type) {
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
