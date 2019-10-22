package com.redhat.syseng.tools.cloudevents.resources;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.redhat.syseng.tools.cloudevents.service.MessageService;
import io.cloudevents.v03.CloudEventImpl;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint("/messages")
@ApplicationScoped
public class MessagesSocket {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessagesSocket.class);
    public static final String MESSAGES_ADDRESS = "messages";

    final Map<String, Session> sessions = new ConcurrentHashMap<>();

    @Inject
    Vertx vertx;

    @Inject
    MessageService msgService;

    @PostConstruct
    public void onInit() {
        vertx.eventBus().consumer(MESSAGES_ADDRESS, msg -> {
            broadcast(msgService.get((String) msg.body()));
        });
    }

    @OnOpen
    public void onOpen(Session session) {
        sessions.put(session.getId(), session);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOGGER.error("Error received", throwable);
        sessions.remove(session.getId());
    }

    private void broadcast(CloudEventImpl<JsonObject> message) {
        sessions.values().forEach(s -> {
            s.getAsyncRemote().sendObject(message.getAttributes().getId(), result -> {
                if (result.getException() != null) {
                    LOGGER.error("Unable to broadcast message", result.getException());
                }
            });
        });
    }
}
