package com.vaadin.flow.quarkus.it;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.vaadin.sample.websockets.DependencyAnnotatedWS;
import org.vaadin.sample.websockets.SimpleEndpoint;

@QuarkusIntegrationTest
class CustomWebsocketsIT {

    @TestHTTPResource(DependencyAnnotatedWS.URI)
    URI dependencyAnnotatedWSURI;

    @TestHTTPResource(SimpleEndpoint.URI)
    URI dependencyNotAnnotatedWSURI;

    @TestHTTPResource(CustomAnnotatedEnpoint.URI)
    URI appAnnotatedWSURI;

    @Test
    void dependencyAnnotatedEndpointShouldWork() throws Exception {
        assertWebsocketWorks(dependencyAnnotatedWSURI,
                DependencyAnnotatedWS.PREFIX);
    }

    @Test
    void applicationAnnotatedEndpointShouldWork() throws Exception {
        assertWebsocketWorks(appAnnotatedWSURI, CustomAnnotatedEnpoint.PREFIX);
    }

    @Test
    void dependencyNotAnnotatedEndpointShouldWork() throws Exception {
        assertWebsocketWorks(dependencyNotAnnotatedWSURI,
                SimpleEndpoint.PREFIX);
    }

    void assertWebsocketWorks(URI uri, String messagePrefix) throws Exception {
        Client client = new Client();
        try (Session session = ContainerProvider.getWebSocketContainer()
                .connectToServer(client, uri)) {
            Assertions.assertEquals("CONNECT", client.receivedMessage());
            Assertions.assertEquals(messagePrefix + "Welcome",
                    client.receivedMessage());
            session.getBasicRemote().sendText("hello world");
            Assertions.assertEquals(messagePrefix + "hello world",
                    client.receivedMessage());
        }
    }

    @ClientEndpoint
    public static class Client {
        final LinkedBlockingDeque<String> messages = new LinkedBlockingDeque<>();

        @OnOpen
        public void open(Session session) throws IOException {
            messages.add("CONNECT");
        }

        @OnMessage
        void message(String msg) {
            messages.add(msg);
        }

        String receivedMessage() throws InterruptedException {
            return messages.poll(12, TimeUnit.SECONDS);
        }

    }
}
