package com.vaadin.flow.quarkus.it;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.quarkus.it.layout.LayoutWithInjection;

@QuarkusIntegrationTest
class LayoutWithInjectionIT extends AbstractCdiIT {

    @Override
    protected String getTestPath() {
        return "/injected-layout-view";
    }

    @Test
    void layout_injectedComponent() {
        open();
        WebElement spanElement = waitUntil(driver -> driver
                .findElement(By.id(LayoutWithInjection.LAYOUT_COUNTER_ID)));

        Assertions.assertEquals("Counter: 1", spanElement.getText());
    }

}
