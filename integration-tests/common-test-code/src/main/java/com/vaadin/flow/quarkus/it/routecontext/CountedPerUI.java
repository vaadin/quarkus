/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.quarkus.it.routecontext;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.quarkus.it.Counter;
import com.vaadin.quarkus.context.BeanProvider;

public interface CountedPerUI {

    default int getUiId() {
        return UI.getCurrent().getUIId();
    }

    default Counter getCounter() {
        return BeanProvider.getContextualReference(Counter.class);
    }

    default void countConstruct() {
        getCounter().increment(getClass().getSimpleName() + "C" + getUiId());
    }

    default void countDestroy() {
        if (UI.getCurrent() != null) {
            getCounter()
                    .increment(getClass().getSimpleName() + "D" + getUiId());
        }
    }

}
