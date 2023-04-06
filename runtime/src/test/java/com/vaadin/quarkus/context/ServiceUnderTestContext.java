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
import javax.enterprise.context.spi.AlterableContext;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import java.util.Set;

import com.vaadin.flow.server.ServiceDestroyEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.quarkus.QuarkusVaadinServletService;
import com.vaadin.quarkus.TestQuarkusVaadinServletService;
import com.vaadin.quarkus.context.VaadinServiceScopedContext.ContextualStorageManager;

public class ServiceUnderTestContext implements UnderTestContext {
    private QuarkusVaadinServletService service;
    private static int NDX;
    private final BeanManager beanManager;

    public ServiceUnderTestContext(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    @Override
    public void activate() {
        NDX++;
        service = new TestQuarkusVaadinServletService(beanManager, NDX + "");
        VaadinService.setCurrent(service);
    }

    @Override
    public void tearDownAll() {
        VaadinService.setCurrent(null);
        Context appContext = beanManager.getContext(ApplicationScoped.class);
        Set<Bean<?>> beans = beanManager
                .getBeans(ContextualStorageManager.class);
        ((AlterableContext) appContext).destroy(beanManager.resolve(beans));
    }

    @Override
    public void destroy() {
        if (service != null) {
            beanManager.fireEvent(new ServiceDestroyEvent(service));
        }
    }

    public QuarkusVaadinServletService getService() {
        return service;
    }
}
