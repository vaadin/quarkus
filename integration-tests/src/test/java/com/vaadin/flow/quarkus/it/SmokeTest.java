package com.vaadin.flow.quarkus.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.By;

import com.vaadin.flow.component.html.testbench.LabelElement;
import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.test.AbstractChromeTest;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SmokeTest extends AbstractChromeTest {

    @Test
    public void testHelloEndpoint() {
        open();
        waitForElementPresent(By.tagName("button"));
        final NativeButtonElement button = $(NativeButtonElement.class).first();
        Assertions.assertTrue(button.isDisplayed());

        button.click();

        // As we don't have CDI functionality for Route annotations we check that we are in
        // the expected feature.
        Assertions.assertEquals("no CDI enabled",
                $(LabelElement.class).first().getText());
    }

}
