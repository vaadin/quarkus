package com.vaadin.flow.test;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.vaadin.flow.testcategory.ChromeTests;
import com.vaadin.flow.testutil.AbstractTestBenchTest;
import com.vaadin.testbench.ScreenshotOnFailureRule;
import com.vaadin.testbench.TestBench;
import com.vaadin.testbench.parallel.Browser;

/**
 * Simplified chrome test that doesn't handle view/IT class paths.
 * Uses Jupiter API
 */
@Category(ChromeTests.class)
public abstract class AbstractChromeTest extends AbstractTestBenchTest {

    @Rule
    public ScreenshotOnFailureRule screenshotOnFailure = new ScreenshotOnFailureRule(
            this, true);

    /**
     * Sets up the chrome driver path in a system variable.
     */
    @BeforeAll
    public static void setChromeDriverPath() {
        ChromeDriverLocator.fillEnvironmentProperty();
    }

    @AfterAll
    public void tearDown() {
        getDriver().quit();
    }

    @BeforeEach
    @Override
    public void setup() throws Exception {
        if (Browser.CHROME == getRunLocallyBrowser() && !isJavaInDebugMode()) {
            setDriver(createHeadlessChromeDriver(
                    this::updateHeadlessChromeOptions));
        } else {
            super.setup();
        }
    }

    @Override
    protected String getTestPath() {
        return "/";
    }

    @Override
    protected int getDeploymentPort() {
        return 8081;
    }

    /**
     * Allows modifying the chrome options to be used when running on a local
     * Chrome.
     *
     * @param chromeOptions
     *         chrome options to use when running on a local Chrome
     */
    protected void updateHeadlessChromeOptions(ChromeOptions chromeOptions) {
    }

    static boolean isJavaInDebugMode() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments()
                .toString().contains("jdwp");
    }

    static WebDriver createHeadlessChromeDriver(
            Consumer<ChromeOptions> optionsUpdater) {
        ChromeOptions headlessOptions = createHeadlessChromeOptions();
        optionsUpdater.accept(headlessOptions);
        return TestBench.createDriver(new ChromeDriver(headlessOptions));
    }

    @Override
    protected List<DesiredCapabilities> getHubBrowsersToTest() {
        if (!getLocalExecution().isPresent() && USE_BROWSERSTACK) {
            // Use IE11 when running with Browserstack
            return getBrowserCapabilities(Browser.IE11);
        }

        return getBrowserCapabilities(Browser.CHROME);
    }

    @Override
    protected List<DesiredCapabilities> getBrowserCapabilities(
            Browser... browsers) {
        return customizeCapabilities(super.getBrowserCapabilities(browsers));
    }

    protected List<DesiredCapabilities> customizeCapabilities(
            List<DesiredCapabilities> capabilities) {

        capabilities.stream()
                .filter(cap -> "chrome".equalsIgnoreCase(cap.getBrowserName()))
                .forEach(cap -> cap.setCapability(ChromeOptions.CAPABILITY,
                        createHeadlessChromeOptions()));

        return capabilities;
    }

    static ChromeOptions createHeadlessChromeOptions() {
        final ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu");
        return options;
    }

}
