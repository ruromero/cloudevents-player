package com.redhat.syseng.tools.cloudevents.model;

import java.time.LocalDateTime;

import javax.json.JsonObject;

import io.cloudevents.v03.CloudEventImpl;

public class Message {

    private String id;
    private LocalDateTime receivedAt;
    private LocalDateTime updatedAt;
    private CloudEventImpl<JsonObject> event;
    private Boolean acknowledged;

    public Message(CloudEventImpl<JsonObject> event) {
        this.receivedAt = LocalDateTime.now();
        this.event = event;
        this.id = event.getAttributes().getId();
    }

    public Message setId(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }

    public Message setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
        return this;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public Message setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Message setEvent(CloudEventImpl<JsonObject> event) {
        this.event = event;
        return this;
    }

    public CloudEventImpl<JsonObject> getEvent() {
        return event;
    }

    public void setAcknowledge() {
        this.acknowledged = true;
    }
}
