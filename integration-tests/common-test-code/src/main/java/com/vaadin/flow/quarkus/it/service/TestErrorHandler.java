/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.quarkus.it.service;

import javax.inject.Inject;

import io.quarkus.arc.Unremovable;

import com.vaadin.flow.quarkus.it.Counter;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.quarkus.annotation.VaadinServiceEnabled;
import com.vaadin.quarkus.annotation.VaadinServiceScoped;

@VaadinServiceEnabled
@VaadinServiceScoped
@Unremovable
public class TestErrorHandler implements ErrorHandler {

    @Inject
    private Counter counter;

    @Override
    public void error(ErrorEvent event) {
        counter.increment(TestErrorHandler.class.getSimpleName());
    }
}
