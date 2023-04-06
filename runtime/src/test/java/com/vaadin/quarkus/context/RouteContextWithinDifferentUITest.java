/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.quarkus.context;

import java.util.Collections;

import io.quarkus.test.junit.QuarkusTest;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.quarkus.context.RouteScopedContext.NavigationData;

@QuarkusTest
public class RouteContextWithinDifferentUITest
        extends AbstractContextTest<TestRouteScopedContext> {

    @Override
    protected UnderTestContext newContextUnderTest() {
        // Intentionally UI Under Test Context. Nothing else needed.
        UIUnderTestContext context = new UIUnderTestContext() {

            @Override
            public void activate() {
                super.activate();

                NavigationData data = new NavigationData(
                        TestNavigationTarget.class, Collections.emptyList());
                ComponentUtil.setData(getUi(), NavigationData.class, data);
            }

        };

        return context;
    }

    @Override
    protected Class<TestRouteScopedContext> getContextType() {
        return TestRouteScopedContext.class;
    }

}
