package org.vaadin.sample.websockets;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

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
