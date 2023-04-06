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
import javax.enterprise.inject.spi.BeanManager;

import java.lang.reflect.Proxy;

import io.quarkus.arc.Unremovable;
import io.quarkus.test.junit.QuarkusTest;

import com.vaadin.quarkus.context.UiContextTest.TestUIScopedContext;

@QuarkusTest
public class UiContextTest extends AbstractContextTest<TestUIScopedContext> {

    @Override
    protected UnderTestContext newContextUnderTest() {
        return new UIUnderTestContext();
    }

    @Override
    protected Class<TestUIScopedContext> getContextType() {
        return TestUIScopedContext.class;
    }

    public static class TestUIScopedContext extends UIScopedContext {

        @Override
        BeanManager getBeanManager() {
            BeanManager beanManager = super.getBeanManager();
            BeanManager proxy = (BeanManager) Proxy.newProxyInstance(
                    Thread.currentThread().getContextClassLoader(),
                    new Class<?>[] { BeanManager.class },
                    new BeanManagerProxy(beanManager,
                            UIScopedContext.ContextualStorageManager.class));
            return proxy;
        }
    }

    @Dependent
    @Unremovable
    public static class TestContextualStorageManager
            extends UIScopedContext.ContextualStorageManager {

    }

}
