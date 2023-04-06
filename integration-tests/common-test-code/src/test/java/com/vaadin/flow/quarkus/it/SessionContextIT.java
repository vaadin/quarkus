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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.quarkus.it.sessioncontext.SessionContextView;

import static com.vaadin.flow.quarkus.it.sessioncontext.SessionContextView.SessionScopedBean.DESTROY_COUNT;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusIntegrationTest
public class SessionContextIT extends AbstractCdiIT {

    @BeforeEach
    public void setUp() throws Exception {
        resetCounts();
        open();
    }

    @Test
    public void sameSessionIsAccessibleFromUIs() {
        assertLabelEquals("");
        click(SessionContextView.SETVALUEBTN_ID);
        getDriver().navigate().refresh();// creates new UI
        assertLabelEquals(SessionContextView.VALUE);
    }

    @Test
    public void httpSessionCloseDestroysSessionContext() throws Exception {
        assertDestroyCountEquals(0);
        click(SessionContextView.HTTP_INVALIDATEBTN_ID);
        assertDestroyCountEquals(1);
    }

    @Test
    @Tag("slow")
    public void httpSessionExpirationDestroysSessionContext() throws Exception {
        assertDestroyCountEquals(0);
        click(SessionContextView.EXPIREBTN_ID);
        boolean destroyed = false;
        getLogger().info("Waiting for session expiration...");
        for (int i = 0; i < 60; i++) {
            Thread.sleep(1000);
            if (getCount(DESTROY_COUNT) > 0) {
                getLogger().info("session expired after {} seconds", i);
                destroyed = true;
                break;
            }
        }
        assertTrue(destroyed);
    }

    @Override
    protected String getTestPath() {
        return "/session";
    }

    private void assertLabelEquals(String expected) {
        assertTextEquals(expected, SessionContextView.VALUELABEL_ID);
    }

    private void assertDestroyCountEquals(int expectedCount)
            throws IOException {
        assertCountEquals(expectedCount, DESTROY_COUNT);
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(SessionContextIT.class);
    }

}
