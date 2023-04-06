/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.quarkus.context;

import javax.enterprise.context.spi.CreationalContext;

import java.io.Serializable;

/**
 * A copy of org.apache.deltaspike.core.util.context.ContextualInstanceInfo.
 * 
 * 
 * This data holder contains all necessary data you need to store a Contextual
 * Instance in a CDI Context.
 */
public class ContextualInstanceInfo<T> implements Serializable {

    /**
     * The actual Contextual Instance in the context
     */
    private T contextualInstance;

    /**
     * We need to store the CreationalContext as we need it for properly
     * destroying the contextual instance via
     * {@link javax.enterprise.context.spi.Contextual#destroy(Object, javax.enterprise.context.spi.CreationalContext)}
     */
    private CreationalContext<T> creationalContext;

    /**
     * @return the CreationalContext of the bean
     */
    public CreationalContext<T> getCreationalContext() {
        return creationalContext;
    }

    /**
     * @param creationalContext
     *            the CreationalContext of the bean
     */
    public void setCreationalContext(CreationalContext<T> creationalContext) {
        this.creationalContext = creationalContext;
    }

    /**
     * @return the contextual instance itself
     */
    public T getContextualInstance() {
        return contextualInstance;
    }

    /**
     * @param contextualInstance
     *            the contextual instance itself
     */
    public void setContextualInstance(T contextualInstance) {
        this.contextualInstance = contextualInstance;
    }

}
