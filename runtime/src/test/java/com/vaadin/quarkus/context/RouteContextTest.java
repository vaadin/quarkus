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

@QuarkusTest
public class RouteContextTest
        extends AbstractContextTest<TestRouteScopedContext> {

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
    protected Class<TestRouteScopedContext> getContextType() {
        return TestRouteScopedContext.class;
    }

}
