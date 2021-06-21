package com.vaadin.flow.test;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.vaadin.flow.testcategory.ChromeTests;
import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.ScreenshotOnFailureRule;

/**
 * Simplified chrome test that doesn't handle view/IT class paths.
 * Uses Jupiter API
 */
@Category(ChromeTests.class)
public abstract class AbstractChromeTest extends ChromeBrowserTest {

    @Rule
    public ScreenshotOnFailureRule screenshotOnFailure = new ScreenshotOnFailureRule(
            this, true);

    @AfterAll
    public void tearDown() {
        getDriver().quit();
    }

    @BeforeEach
    public void beforeTest() {
        ChromeBrowserTest.setChromeDriverPath();
    }

    @BeforeAll
    public void setup() throws Exception {
        super.setup();
    }
    @Override
    protected String getTestPath() {
        return "/";
    }

    @Override
    protected int getDeploymentPort() {
        return 8081;
    }

}
