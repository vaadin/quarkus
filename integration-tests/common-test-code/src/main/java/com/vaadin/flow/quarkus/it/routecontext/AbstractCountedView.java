/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.quarkus.it.routecontext;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.vaadin.flow.component.html.Div;

public abstract class AbstractCountedView extends Div implements CountedPerUI {

    @PostConstruct
    private void construct() {
        countConstruct();
    }

    @PreDestroy
    private void destroy() {
        countDestroy();
    }

}
