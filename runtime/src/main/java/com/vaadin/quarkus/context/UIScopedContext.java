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
import javax.enterprise.inject.spi.BeanManager;

import java.lang.annotation.Annotation;

import io.quarkus.arc.Arc;
import io.quarkus.arc.Unremovable;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.quarkus.annotation.NormalUIScoped;
import com.vaadin.quarkus.annotation.VaadinSessionScoped;

/**
 * UIScopedContext is the context for {@link NormalUIScoped @NormalUIScoped}
 * beans.
 */
public class UIScopedContext extends AbstractContext {

    @Override
    protected ContextualStorage getContextualStorage(Contextual<?> contextual,
            boolean createIfNotExist) {
        return BeanProvider
                .getContextualReference(getBeanManager(),
                        ContextualStorageManager.class, false)
                .getContextualStorage(createIfNotExist);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return NormalUIScoped.class;
    }

    @Override
    public boolean isActive() {
        return VaadinSession.getCurrent() != null && UI.getCurrent() != null;
    }

    /**
     * Gets a bean manager.
     * <p>
     * Not a private for testing purposes only.
     * 
     * @return a bean manager
     */
    BeanManager getBeanManager() {
        return Arc.container().beanManager();
    }

    @VaadinSessionScoped
    @Unremovable
    public static class ContextualStorageManager
            extends AbstractContextualStorageManager<Integer> {

        public ContextualStorageManager() {
            // Session lock checked in VaadinSessionScopedContext while
            // getting the session attribute of this beans context.
            super(false);
        }

        public ContextualStorage getContextualStorage(
                boolean createIfNotExist) {
            final Integer uiId = UI.getCurrent().getUIId();
            return super.getContextualStorage(uiId, createIfNotExist);
        }

        @Override
        protected ContextualStorage newContextualStorage(Integer uiId) {
            UI.getCurrent().addDetachListener(this::destroy);
            return super.newContextualStorage(uiId);
        }

        private void destroy(DetachEvent event) {
            final int uiId = event.getUI().getUIId();
            super.destroy(uiId);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public ContextState getState() {
        return super.getState();
    }

}
