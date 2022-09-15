package com.vaadin.flow.quarkus.it.layout;

import javax.inject.Inject;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.quarkus.it.Counter;
import com.vaadin.flow.router.RouterLayout;

public class LayoutWithInjection extends Div implements RouterLayout {

    public static final String LAYOUT_COUNTER_ID = "layoutCounter";

    @Inject
    Counter counter;

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        int value = counter.increment(LayoutWithInjection.class.getName());
        Span span = new Span("Counter: " + value);
        span.setId(LAYOUT_COUNTER_ID);
        getElement().appendChild(span.getElement());
    }
}
