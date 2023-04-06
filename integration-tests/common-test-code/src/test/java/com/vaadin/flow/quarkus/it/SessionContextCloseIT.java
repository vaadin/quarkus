/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.quarkus.it;

import java.io.IOException;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.quarkus.it.sessioncontext.SessionContextView;

import static com.vaadin.flow.quarkus.it.sessioncontext.SessionContextView.SessionScopedBean.DESTROY_COUNT;

@QuarkusIntegrationTest
public class SessionContextCloseIT extends AbstractCdiIT {

    @Override
    protected String getTestPath() {
        return "/session";
    }

    @BeforeEach
    public void setUp() throws Exception {
        resetCounts();
        open();
    }

    @Test
    public void vaadinSessionCloseDestroysSessionContext() throws Exception {
        assertDestroyCountEquals(0);
        click(SessionContextView.INVALIDATEBTN_ID);
        assertDestroyCountEquals(1);
    }

    private void assertDestroyCountEquals(int expectedCount)
            throws IOException {
        assertCountEquals(expectedCount, DESTROY_COUNT);
    }
}
