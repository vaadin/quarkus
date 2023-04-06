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
import javax.inject.Inject;

import io.quarkus.test.junit.QuarkusTest;

import com.vaadin.quarkus.annotation.VaadinServiceScoped;

@QuarkusTest
public class ServiceContextTest
        extends AbstractContextTest<VaadinServiceScopedContext> {
    @Inject
    private BeanManager beanManager;

    @Override
    protected UnderTestContext newContextUnderTest() {
        return new ServiceUnderTestContext(beanManager);
    }

    @Override
    protected Class<VaadinServiceScopedContext> getContextType() {
        return VaadinServiceScopedContext.class;
    }

    @VaadinServiceScoped
    public static class ServiceScopedTestBean extends TestBean {
    }

}
