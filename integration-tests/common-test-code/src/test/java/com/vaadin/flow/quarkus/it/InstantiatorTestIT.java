package com.vaadin.flow.quarkus.it;

import java.util.concurrent.atomic.AtomicReference;

import com.vaadin.flow.test.AbstractChromeIT;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

@QuarkusIntegrationTest
public class InstantiatorTestIT extends AbstractChromeIT {

    @Test
    public void navigationCorrectlyHandlesProxiedViews() {
        open();

        String prevUuid = null;
        AtomicReference<String> prevCounter = new AtomicReference<>("");
        for (int i = 0; i < 5; i++) {
            String uuid = waitUntil(d -> d.findElement(By.id("COMPONENT_ID")))
                    .getText();

            waitUntil(d -> !prevCounter.get()
                    .equals(d.findElement(By.id("CLICK_COUNTER")).getText()));

            if (prevUuid != null) {
                Assert.assertEquals("UUID should not have been changed",
                        prevUuid, uuid);
            }
            String counter = findElement(By.id("CLICK_COUNTER")).getText();
            Assert.assertEquals(
                    "Parameter and counter should have the same value",
                    "P:" + i + ", C:" + i, counter);

            prevUuid = uuid;
            prevCounter.set(counter);

            $("a").first().click();
        }
    }

    @Override
    protected String getTestPath() {
        return "/proxied";
    }

    private void checkLogs() {
        checkLogsForErrors(msg -> msg.contains("webpack-internal:")
                && msg.contains("VaadinDevmodeGizmo") && msg.contains("Event"));
    }
}
