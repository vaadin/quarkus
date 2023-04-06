/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.quarkus.it;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.test.AbstractChromeIT;
import com.vaadin.testbench.TestBenchElement;

@QuarkusIntegrationTest
public class PushSmokeIT extends AbstractChromeIT {

    protected String getTestPath() {
        return "/push";
    }

    @Test
    public void pushUpdatesEmbeddedWebComponent() {
        open();
        waitForElementPresent(By.id("push-update"));

        int expectedUpdates = 50;
        int initialUpdateCount = getUpdateCount();
        Assertions.assertTrue(initialUpdateCount < expectedUpdates,
                "The initial update count should be less than maximum 50, but it has value "
                        + initialUpdateCount);

        waitUntil(driver -> getUpdateCount() > initialUpdateCount, 10);

        int nextUpdateCount = getUpdateCount();

        Assertions.assertTrue(nextUpdateCount < expectedUpdates,
                "The next interim update count should be less than maximum 50, but it has value "
                        + nextUpdateCount);

        waitUntil(driver -> getUpdateCount() == expectedUpdates, 5);
    }

    private int getUpdateCount() {
        TestBenchElement div = $(TestBenchElement.class).id("push-update");
        String count = div.getText();
        return Integer.parseInt(count);
    }

}
