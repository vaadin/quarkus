package org.vaadin.sample.websockets;

import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;

public class SimpleEndpoint extends Endpoint {

    public static final String URI = "/dependency-websocket";

    public static final String PREFIX = ">> Dependency Simple Endpoint: ";

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        Handler handler = new Handler(session.getAsyncRemote());
        session.addMessageHandler(handler);
        handler.reply("Welcome");
    }

    private static class Handler implements MessageHandler.Whole<String> {

        private final RemoteEndpoint.Async remote;

        public Handler(RemoteEndpoint.Async remote) {
            this.remote = remote;
        }

        @Override
        public void onMessage(String message) {
            reply(message);
        }

        private void reply(String message) {
            remote.sendText(PREFIX + message);
        }
    }
}
