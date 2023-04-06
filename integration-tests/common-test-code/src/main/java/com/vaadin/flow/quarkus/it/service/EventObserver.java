/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.quarkus.it.service;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.vaadin.flow.quarkus.it.Counter;
import com.vaadin.flow.server.SessionDestroyEvent;
import com.vaadin.flow.server.SessionInitEvent;
import com.vaadin.flow.server.UIInitEvent;

public class EventObserver {
    @Inject
    private Counter counter;

    private void onSessionInit(@Observes SessionInitEvent sessionInitEvent) {
        counter.increment(SessionInitEvent.class.getSimpleName());
    }

    private void onSessionDestroy(
            @Observes SessionDestroyEvent sessionDestroyEvent) {
        counter.increment(SessionDestroyEvent.class.getSimpleName());
    }

    private void onUIInit(@Observes UIInitEvent uiInitEvent) {
        counter.increment(UIInitEvent.class.getSimpleName());
    }
}
