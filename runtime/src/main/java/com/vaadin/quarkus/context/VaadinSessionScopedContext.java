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

import java.lang.annotation.Annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.SessionDestroyEvent;
import com.vaadin.flow.server.SessionDestroyListener;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import com.vaadin.quarkus.annotation.VaadinSessionScoped;

/**
 * Context for {@link VaadinSessionScoped @VaadinSessionScoped} beans.
 * <p>
 * Stores contextuals in {@link VaadinSession}. Other Vaadin CDI contexts are
 * stored in the corresponding {@link VaadinSessionScoped} context.
 *
 * @since 1.0
 */
public class VaadinSessionScopedContext extends AbstractContext {
    private static final String ATTRIBUTE_NAME = VaadinSessionScopedContext.class
            .getName();

    @Override
    protected ContextualStorage getContextualStorage(Contextual<?> contextual,
            boolean createIfNotExist) {
        VaadinSession session = VaadinSession.getCurrent();
        ContextualStorage storage = findContextualStorage(session);
        if (storage == null && createIfNotExist) {
            storage = new SessionContextualStorage(session);
            session.setAttribute(ATTRIBUTE_NAME, storage);
        }
        return storage;
    }

    private static ContextualStorage findContextualStorage(
            VaadinSession session) {
        // session lock is checked inside
        return (ContextualStorage) session.getAttribute(ATTRIBUTE_NAME);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return VaadinSessionScoped.class;
    }

    @Override
    public boolean isActive() {
        return VaadinSession.getCurrent() != null;
    }

    private static class SessionContextualStorage extends ContextualStorage
            implements SessionDestroyListener {

        private final Registration registration;

        private final VaadinSession session;

        private SessionContextualStorage(VaadinSession session) {
            super(false);
            this.session = session;
            registration = session.getService().addSessionDestroyListener(this);
        }

        @Override
        public void sessionDestroy(SessionDestroyEvent event) {
            if (!session.equals(event.getSession())) {
                return;
            }
            getLogger().debug("VaadinSessionScopedContext destroy");
            ContextualStorage storage = findContextualStorage(
                    event.getSession());
            registration.remove();
            if (storage != null) {
                AbstractContext.destroyAllActive(storage);
            }
        }

        private Logger getLogger() {
            return LoggerFactory.getLogger(SessionContextualStorage.class);
        }
    }

}
