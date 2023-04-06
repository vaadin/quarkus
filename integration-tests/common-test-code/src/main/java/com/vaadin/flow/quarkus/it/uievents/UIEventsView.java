/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.quarkus.it.uievents;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import java.util.EventObject;
import java.util.List;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.PollEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.quarkus.annotation.NormalUIScoped;

@Route("uievents")
public class UIEventsView extends Div implements AfterNavigationObserver {

    public static final String POLL_FROM_CLIENT = "POLL_FROM_CLIENT";
    public static final String NAVIGATION_EVENTS = "NAVIGATION_EVENTS";

    @NormalUIScoped
    public static class PollObserver {
        private void showPollEvent(@Observes PollEvent pollEvent) {
            UI ui = pollEvent.getSource();

            List<HasElement> chain = ui.getInternals()
                    .getActiveRouterTargetsChain();

            HasElement leaf = chain.get(chain.size() - 1);
            if (leaf instanceof UIEventsView) {

                final Label poll = new Label(pollEvent.isFromClient() + "");
                poll.setId(POLL_FROM_CLIENT);

                ((UIEventsView) leaf).add(new Div(poll));
            }
        }
    }

    @Inject
    private NavigationObserver navigationObserver;

    @PostConstruct
    private void init() {
        UI.getCurrent().setPollInterval(500);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
        showNavigationEvents();
    }

    private void showNavigationEvents() {
        Div events = new Div();
        events.setId(NAVIGATION_EVENTS);
        List<EventObject> navigationEvents = navigationObserver
                .getNavigationEvents();
        navigationEvents.stream()
                .map(event -> new Label(event.getClass().getSimpleName()))
                .forEach(events::add);
        add(events);
    }

}
