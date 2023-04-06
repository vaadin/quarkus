/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.quarkus.it;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.Assert;
import org.openqa.selenium.By;

import com.vaadin.flow.test.AbstractChromeIT;

abstract public class AbstractCdiIT extends AbstractChromeIT {

    protected void click(String elementId) {
        findElement(By.id(elementId)).click();
    }

    protected void follow(String linkText) {
        findElement(By.linkText(linkText)).click();
    }

    protected String getText(String id) {
        return findElement(By.id(id)).getText();
    }

    protected void assertCountEquals(int expectedCount, String counter)
            throws IOException {
        Assert.assertEquals(expectedCount, getCount(counter));
    }

    protected void assertTextEquals(String expectedText, String elementId) {
        Assert.assertEquals(expectedText, getText(elementId));
    }

    protected void resetCounts() throws IOException {
        slurp("?resetCounts");
    }

    protected int getCount(String id) throws IOException {
        getCommandExecutor().waitForVaadin();
        String line = slurp("?getCount=" + id);
        return Integer.parseInt(line);
    }

    private String slurp(String uri) throws IOException {
        URL url = new URL(getRootURL() + uri);
        InputStream is = url.openConnection().getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line = reader.readLine();
        reader.close();
        return line;
    }
}
