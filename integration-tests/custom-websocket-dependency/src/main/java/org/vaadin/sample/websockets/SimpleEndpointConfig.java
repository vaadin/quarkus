package org.vaadin.sample.websockets;

import jakarta.websocket.Endpoint;
import jakarta.websocket.server.ServerApplicationConfig;
import jakarta.websocket.server.ServerEndpointConfig;
import java.util.Collections;
import java.util.Set;

public class SimpleEndpointConfig implements ServerApplicationConfig {
    @Override
    public Set<ServerEndpointConfig> getEndpointConfigs(
            Set<Class<? extends Endpoint>> endpointClasses) {
        return Set.of(ServerEndpointConfig.Builder
                .create(SimpleEndpoint.class, SimpleEndpoint.URI).build());
    }

    @Override
    public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scanned) {
        return Collections.emptySet();
    }
}
