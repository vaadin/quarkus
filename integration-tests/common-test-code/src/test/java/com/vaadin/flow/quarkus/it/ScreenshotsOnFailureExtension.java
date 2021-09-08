/*
 * Copyright 2000-2021 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.quarkus.it;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.runner.Description;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.vaadin.flow.test.AbstractChromeTest;
import com.vaadin.testbench.screenshot.ImageFileUtil;

public class ScreenshotsOnFailureExtension
        implements TestExecutionExceptionHandler {

    @Override
    public void handleTestExecutionException(ExtensionContext context,
            Throwable throwable) throws Throwable {
        Object object = context.getTestInstance().get();
        AbstractChromeTest test = (AbstractChromeTest) object;
        WebDriver realDriver = test.getDriver();
        System.out.println("qqqqqqqqqq " + realDriver);

        while (realDriver instanceof WrapsDriver) {
            realDriver = ((WrapsDriver) realDriver).getWrappedDriver();
        }
        if (realDriver instanceof RemoteWebDriver
                && ((RemoteWebDriver) realDriver).getSessionId() == null) {
            // logger.warning(
            System.err.println(
                    "Unable capture failure screenshot: web driver is no longer available");
            return;
        }

        // Grab a screenshot when a test fails
        try {
            BufferedImage screenshotImage = ImageIO
                    .read(new ByteArrayInputStream(
                            ((TakesScreenshot) test.getDriver())
                                    .getScreenshotAs(OutputType.BYTES)));
            // Store the screenshot in the errors directory
            ImageFileUtil.createScreenshotDirectoriesIfNeeded();
            final File errorScreenshotFile = ImageFileUtil
                    .getErrorScreenshotFile("foo" + ".png");// getErrorScreenshotFile("foo");
            ImageIO.write(screenshotImage, "png", errorScreenshotFile);
            // logger.info
            System.err.println("Error screenshot written to: "
                    + errorScreenshotFile.getAbsolutePath());
        } catch (IOException e1) {
            throw new RuntimeException(
                    "There was a problem grabbing and writing a screen shot of a test failure.",
                    e1);
        }

        throw throwable;
    }

    /**
     * @param description
     *            test {@link Description}
     * @return Failure screenshot file.
     */
    protected File getErrorScreenshotFile(Description description) {
        return ImageFileUtil
                .getErrorScreenshotFile(description.getDisplayName() + ".png");
    }

}
