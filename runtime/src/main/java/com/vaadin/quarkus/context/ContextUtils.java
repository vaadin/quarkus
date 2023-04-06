/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.quarkus.context;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.BeanManager;

import java.lang.annotation.Annotation;

import io.quarkus.arc.Arc;

/**
 * A modified copy of org.apache.deltaspike.core.util.ContextUtils.
 * 
 * A set of utility methods for working with contexts.
 */
@Typed()
public abstract class ContextUtils {
    private ContextUtils() {
        // prevent instantiation
    }

    /**
     * Checks if the context for the given scope annotation is active.
     *
     * @param scopeAnnotationClass
     *            The scope annotation (e.g. @RequestScoped.class)
     * @return If the context is active.
     */
    public static boolean isContextActive(
            Class<? extends Annotation> scopeAnnotationClass) {
        return isContextActive(scopeAnnotationClass,
                Arc.container().beanManager());
    }

    /**
     * Checks if the context for the given scope annotation is active.
     *
     * @param scopeAnnotationClass
     *            The scope annotation (e.g. @RequestScoped.class)
     * @param beanManager
     *            The {@link BeanManager}
     * @return If the context is active.
     */
    public static boolean isContextActive(
            Class<? extends Annotation> scopeAnnotationClass,
            BeanManager beanManager) {
        try {
            if (beanManager.getContext(scopeAnnotationClass) == null
                    || !beanManager.getContext(scopeAnnotationClass)
                            .isActive()) {
                return false;
            }
        } catch (ContextNotActiveException e) {
            return false;
        }

        return true;
    }
}
