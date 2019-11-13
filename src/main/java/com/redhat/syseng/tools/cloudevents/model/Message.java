package com.redhat.syseng.tools.cloudevents.model;

import java.time.ZonedDateTime;

import javax.json.JsonObject;

import io.cloudevents.CloudEvent;
import io.cloudevents.v1.AttributesImpl;
import io.cloudevents.v1.CloudEventImpl;

public class Message {

    public enum MessageType {
        SENT,
        RECEIVED
    }

    private String id;
    private ZonedDateTime receivedAt;
    private CloudEventImpl<JsonObject> event;
    private MessageType type;

    public Message(CloudEvent<AttributesImpl, JsonObject> event, MessageType type) {
        this.receivedAt = ZonedDateTime.now();
        this.event = (CloudEventImpl<JsonObject>) event;
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

    public Message setEvent(CloudEvent<AttributesImpl, JsonObject> event) {
        this.event = (CloudEventImpl<JsonObject>) event;
        return this;
    }

    public CloudEvent<AttributesImpl, JsonObject> getEvent() {
        return event;
    }

    public MessageType getType() {
        return type;
    }
}
