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

@QuarkusTest
public class SessionContextTest
        extends AbstractContextTest<VaadinSessionScopedContext> {

    @Override
    protected UnderTestContext newContextUnderTest() {
        return new SessionUnderTestContext();
    }

    @Override
    protected Class<VaadinSessionScopedContext> getContextType() {
        return VaadinSessionScopedContext.class;
    }

}
