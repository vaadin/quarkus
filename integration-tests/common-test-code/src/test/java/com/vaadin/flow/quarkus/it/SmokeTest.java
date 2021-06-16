package com.vaadin.flow.quarkus.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.html.testbench.LabelElement;
import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.test.AbstractChromeTest;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SmokeTest extends AbstractChromeTest {

    @Test
    public void smokeTest_clickButton() {
        open();
        checkLogsForErrors();
        waitForElementPresent(By.tagName("button"));
        final NativeButtonElement button = $(NativeButtonElement.class).first();
        Assertions.assertTrue(button.isDisplayed());

        button.click();

        // As we don't have CDI functionality for Route annotations we check that we are in
        // the expected feature.
        Assertions.assertEquals("hello quarkus CDI",
                $(LabelElement.class).first().getText());
    }

    @Test
    public void smokeTest_validateReusableTheme() {
        open();
        checkLogsForErrors();
        waitForElementPresent(By.tagName("button"));
        final WebElement element = findElement(
                By.className("centered-content"));

        Assertions.assertEquals("250px", element.getCssValue("max-width"),
                "Theme max-width was not applied.");
    }
}
