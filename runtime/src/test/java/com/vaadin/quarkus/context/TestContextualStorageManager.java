/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.quarkus.context;

import javax.enterprise.context.Dependent;

import java.util.Set;

import io.quarkus.arc.Unremovable;

import com.vaadin.flow.server.VaadinSession;
import com.vaadin.quarkus.context.RouteScopedContext.ContextualStorageManager;
import com.vaadin.quarkus.context.RouteScopedContext.RouteStorageKey;

/**
 *
 * Quarkus will use newly created instance of ContextualStorageManager to fire
 * navigation events. As a result <code>@Observes</code> methods will be called
 * on instance which is not used by any other piece of the code. To be able to
 * call it on the correct instance it's retreived from the session attribute:
 * it's the valid way since the bean is in fact session scoped (originally).
 */
@Dependent
@Unremovable
public class TestContextualStorageManager extends ContextualStorageManager {
    @Override
    protected Set<RouteStorageKey> getKeySet() {
        TestContextualStorageManager manager = (TestContextualStorageManager) VaadinSession
                .getCurrent().getAttribute(BeanManagerProxy
                        .getAttributeName(TestContextualStorageManager.class));
        if (manager != this) {
            return manager.getKeySet();
        }
        return super.getKeySet();
    }

    @Override
    protected void destroy(RouteStorageKey key) {
        TestContextualStorageManager manager = (TestContextualStorageManager) VaadinSession
                .getCurrent().getAttribute(BeanManagerProxy
                        .getAttributeName(TestContextualStorageManager.class));
        if (manager != this) {
            manager.destroy(key);
        } else {
            super.destroy(key);
        }
    }

}
