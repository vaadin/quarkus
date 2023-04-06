/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.quarkus.it;

import java.util.List;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.quarkus.it.uievents.UIEventsView;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.testbench.TestBenchElement;

@QuarkusIntegrationTest
public class UIEventsIT extends AbstractCdiIT {

    @Override
    protected String getTestPath() {
        return "/uievents";
    }

    @BeforeEach
    public void setUp() throws Exception {
        open();
    }

    @Test
    public void navigationEventsObserved() {
        List<TestBenchElement> events = $("div")
                .id(UIEventsView.NAVIGATION_EVENTS).$("label").all();
        Assertions.assertEquals(3, events.size());
        assertEventIs(events.get(0), BeforeLeaveEvent.class);
        assertEventIs(events.get(1), BeforeEnterEvent.class);
        assertEventIs(events.get(2), AfterNavigationEvent.class);
    }

    @Test
    public void pollEventObserved() {
        waitForElementPresent(By.id(UIEventsView.POLL_FROM_CLIENT));
        assertTextEquals("true", UIEventsView.POLL_FROM_CLIENT);
    }

    private void assertEventIs(TestBenchElement eventElem,
            Class<?> eventClass) {
        Assertions.assertEquals(eventClass.getSimpleName(),
                eventElem.getText());
    }

}