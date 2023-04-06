/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.quarkus.context;

import javax.enterprise.inject.spi.BeanManager;

import java.lang.reflect.Proxy;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

public class TestRouteScopedContext extends RouteScopedContext {

    @Override
    BeanManager getBeanManager() {
        BeanManager beanManager = super.getBeanManager();
        BeanManager proxy = (BeanManager) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { BeanManager.class }, new BeanManagerProxy(
                        beanManager, TestContextualStorageManager.class));
        return proxy;
    }

    @Override
    public boolean isActive() {
        return VaadinSession.getCurrent() != null && UI.getCurrent() != null;
    }

    @Override
    Class<? extends ContextualStorageManager> getContextualStorageManagerClass() {
        return TestContextualStorageManager.class;
    }
}
