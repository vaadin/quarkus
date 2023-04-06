/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.quarkus.context;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import java.lang.annotation.Annotation;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InjectableContext;

import com.vaadin.quarkus.annotation.NormalRouteScoped;
import com.vaadin.quarkus.annotation.RouteScoped;

/**
 * Used to bind multiple scope annotations to a single context. Will delegate
 * all context-related operations to it's underlying instance, apart from
 * getting the scope of the context.
 *
 */
public class RouteContextWrapper implements InjectableContext {

    @Override
    public Class<? extends Annotation> getScope() {
        return RouteScoped.class;
    }

    @Override
    public <T> T get(final Contextual<T> component,
            final CreationalContext<T> creationalContext) {
        return getContext().get(component, creationalContext);
    }

    @Override
    public <T> T get(final Contextual<T> component) {
        return getContext().get(component);
    }

    @Override
    public boolean isActive() {
        return getContext().isActive();
    }

    @Override
    public void destroy(final Contextual<?> contextual) {
        getContext().destroy(contextual);
    }

    @Override
    public void destroy() {
        getContext().destroy();
    }

    @Override
    public ContextState getState() {
        return getContext().getState();
    }

    /**
     * Gets a delegating context.
     * <p>
     * Not a private for testing purposes only.
     * 
     * @return a delegating context
     */
    InjectableContext getContext() {
        return Arc.container().getActiveContext(NormalRouteScoped.class);
    }
}
