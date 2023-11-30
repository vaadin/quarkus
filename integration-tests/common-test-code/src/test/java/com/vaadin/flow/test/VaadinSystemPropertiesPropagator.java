package com.vaadin.flow.test;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * Propagates all Vaadin related system properties to Quarkus test.
 */
public class VaadinSystemPropertiesPropagator
        implements QuarkusTestResourceLifecycleManager {

    @Override
    public Map<String, String> start() {
        Map<String, String> props = new HashMap<>();
        System.getProperties().stringPropertyNames().stream()
                .filter(key -> key.startsWith("vaadin."))
                .forEach(key -> props.put(key, System.getProperty(key)));
        return props;
    }

    @Override
    public void stop() {
    }
}
