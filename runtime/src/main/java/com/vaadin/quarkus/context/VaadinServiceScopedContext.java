/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.quarkus.context;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.event.Observes;

import java.lang.annotation.Annotation;

import io.quarkus.arc.Arc;
import io.quarkus.arc.Unremovable;

import com.vaadin.flow.server.ServiceDestroyEvent;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.quarkus.QuarkusVaadinServlet;
import com.vaadin.quarkus.annotation.VaadinServiceScoped;

import static javax.enterprise.event.Reception.IF_EXISTS;

/**
 * Context for {@link VaadinServiceScoped @VaadinServiceScoped} beans.
 */
public class VaadinServiceScopedContext extends AbstractContext {

    @Override
    protected ContextualStorage getContextualStorage(Contextual<?> contextual,
            boolean createIfNotExist) {
        QuarkusVaadinServlet servlet = (QuarkusVaadinServlet) VaadinServlet
                .getCurrent();
        String servletName;
        if (servlet != null) {
            servletName = servlet.getServletName();
        } else {
            servletName = QuarkusVaadinServlet.getCurrentServletName().get();
        }
        return BeanProvider
                .getContextualReference(Arc.container().beanManager(),
                        ContextualStorageManager.class, false)
                .getContextualStorage(servletName, createIfNotExist);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return VaadinServiceScoped.class;
    }

    @Override
    public boolean isActive() {
        VaadinServlet servlet = VaadinServlet.getCurrent();
        return servlet instanceof QuarkusVaadinServlet || (servlet == null
                && QuarkusVaadinServlet.getCurrentServletName() != null);
    }

    @ApplicationScoped
    @Unremovable
    public static class ContextualStorageManager
            extends AbstractContextualStorageManager<String> {

        public ContextualStorageManager() {
            super(true);
        }

        /**
         * Service destroy event observer.
         *
         * During application shutdown it is container specific whether this
         * observer being called, or not. Application context destroy may happen
         * earlier, and cleanup done by {@link #destroyAll()}.
         *
         * @param event
         *            service destroy event
         */
        private void onServiceDestroy(
                @Observes(notifyObserver = IF_EXISTS) ServiceDestroyEvent event) {
            if (!(event.getSource() instanceof VaadinServletService)) {
                return;
            }
            VaadinServletService service = (VaadinServletService) event
                    .getSource();
            String servletName = service.getServlet().getServletName();
            destroy(servletName);
        }

    }

}
