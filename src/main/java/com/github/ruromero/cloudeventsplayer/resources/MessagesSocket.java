package com.github.ruromero.cloudeventsplayer.resources;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.vertx.ConsumeEvent;

@ServerEndpoint("/socket")
@ApplicationScoped
public class MessagesSocket {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessagesSocket.class);
    public static final String MESSAGES_ADDRESS = "messages";

    final Map<String, Session> sessions = new ConcurrentHashMap<>();

    @ConsumeEvent(MESSAGES_ADDRESS)
    public void onNewMessage(String message) {
        broadcast(message);
    }

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.debug("New session: {}", session.getId());
        sessions.put(session.getId(), session);
    }

    @OnClose
    public void onClose(Session session) {
        LOGGER.debug("Session closed: {}", session.getId());
        sessions.remove(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOGGER.error("Error received", throwable);
        sessions.remove(session.getId());
    }

    private void broadcast(String msg) {
        sessions.values().forEach(s -> s.getAsyncRemote().sendObject(msg, result -> {
            if (result.getException() != null) {
                LOGGER.error("Unable to broadcast message", result.getException());
            }
        }));
    }
}
