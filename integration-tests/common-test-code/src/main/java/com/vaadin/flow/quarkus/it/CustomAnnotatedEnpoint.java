package com.vaadin.flow.quarkus.it;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(CustomAnnotatedEnpoint.URI)
public class CustomAnnotatedEnpoint {

    public static final String URI = "/app-annotated-websocket";
    public static final String PREFIX = ">> Application Annotated Endpoint: ";

    @OnOpen
    public void onOpen(Session session) {
        session.getAsyncRemote().sendText(PREFIX + "Welcome");
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        session.getAsyncRemote().sendText(PREFIX + message);
    }
}
