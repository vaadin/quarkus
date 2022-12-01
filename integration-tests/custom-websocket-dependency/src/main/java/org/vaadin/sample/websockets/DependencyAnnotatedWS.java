package org.vaadin.sample.websockets;

import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint(DependencyAnnotatedWS.URI)
public class DependencyAnnotatedWS {

    public static final String URI = "/dependency-annotated-websocket";
    public static final String PREFIX = ">> Dependency Annotated Endpoint: ";

    @OnOpen
    public void onOpen(Session session) {
        session.getAsyncRemote().sendText(PREFIX + "Welcome");
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        session.getAsyncRemote().sendText(PREFIX + message);
    }
}
