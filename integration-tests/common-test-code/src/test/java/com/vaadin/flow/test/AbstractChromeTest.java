package com.vaadin.flow.test;

import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.vaadin.flow.testcategory.ChromeTests;
import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.ScreenshotOnFailureRule;
import com.vaadin.testbench.annotations.BrowserConfiguration;
import com.vaadin.testbench.parallel.Browser;
import com.vaadin.testbench.parallel.BrowserUtil;

/**
 * Simplified chrome test that doesn't handle view/IT class paths. Uses Jupiter
 * API
 */
@Category(ChromeTests.class)
public abstract class AbstractChromeTest extends ChromeBrowserTest {

    @Rule
    public ScreenshotOnFailureRule screenshotOnFailure = new ScreenshotOnFailureRule(
            this, true);

    @AfterEach
    public void tearDown() {
        getDriver().quit();
    }

    @BeforeAll
    public void setCapabilities() {
        setDesiredCapabilities(Browser.CHROME.getDesiredCapabilities());
    }

    @BeforeEach
    public void beforeTest() throws Exception {
        ChromeBrowserTest.setChromeDriverPath();
        setup();
        checkIfServerAvailable();
    }

    @Override
    @BrowserConfiguration
    public List<DesiredCapabilities> getBrowsersToTest() {
        return Arrays.asList(BrowserUtil.chrome());
    }

}
