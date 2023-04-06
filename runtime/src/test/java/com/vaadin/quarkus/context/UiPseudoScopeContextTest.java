/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.quarkus.context;

import io.quarkus.arc.InjectableContext;
import io.quarkus.test.junit.QuarkusTest;

import com.vaadin.quarkus.context.UiContextTest.TestUIScopedContext;
import com.vaadin.quarkus.context.UiPseudoScopeContextTest.TestUIContextWrapper;

@QuarkusTest
public class UiPseudoScopeContextTest
        extends InjectableContextTest<TestUIContextWrapper> {

    @Override
    protected UnderTestContext newContextUnderTest() {
        return new UIUnderTestContext();
    }

    @Override
    protected Class<TestUIContextWrapper> getContextType() {
        return TestUIContextWrapper.class;
    }

    public static class TestUIContextWrapper extends UIContextWrapper {

        @Override
        InjectableContext getContext() {
            return new TestUIScopedContext();
        }
    }
}
