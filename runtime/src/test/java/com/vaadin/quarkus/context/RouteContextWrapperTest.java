/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.quarkus.context;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import com.vaadin.quarkus.context.RouteContextWrapperWithinDifferentUITest.TestRouteContextWrapper;

@QuarkusTest
public class RouteContextWrapperTest
        extends InjectableContextTest<TestRouteContextWrapper> {

    @Override
    @Test
    public void destroyContext_beanExistsInContext_beanDestroyed() {
        destroyContext_beanExistsInContext_beanDestroyed(true);
    }

    @Override
    protected UnderTestContext newContextUnderTest() {
        return new RouteUnderTestContext();
    }

    @Override
    protected Class<TestRouteContextWrapper> getContextType() {
        return TestRouteContextWrapper.class;
    }

}
