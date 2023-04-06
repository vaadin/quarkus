/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.quarkus.it.uicontext;

import javax.annotation.PreDestroy;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.quarkus.it.Counter;
import com.vaadin.quarkus.annotation.UIScoped;

@UIScoped
public class UIScopedLabel extends Label {

    public static final String DESTROY_COUNT = "UIScopedLabelDestroy";

    @Inject
    private Counter counter;

    private int uiId;

    public static final String ID = "UISCOPED_LABEL";

    public UIScopedLabel() {
        setId(ID);
        uiId = UI.getCurrent().getUIId();
    }

    @PreDestroy
    private void destroy() {
        counter.increment(DESTROY_COUNT + uiId);
    }

    void onSetText(@Observes SetTextEvent event) {
        setText(event.getText());
    }

    public static class SetTextEvent {
        private final String text;

        public SetTextEvent(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }
}
