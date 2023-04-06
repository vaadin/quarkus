/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.quarkus.it;

import java.io.IOException;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.quarkus.it.service.BootstrapCustomizer;
import com.vaadin.flow.quarkus.it.service.ServiceView;
import com.vaadin.flow.quarkus.it.service.TestErrorHandler;
import com.vaadin.flow.quarkus.it.service.TestSystemMessagesProvider;
import com.vaadin.flow.server.SessionDestroyEvent;
import com.vaadin.flow.server.SessionInitEvent;
import com.vaadin.flow.server.UIInitEvent;

@QuarkusIntegrationTest
public class ServiceIT extends AbstractCdiIT {

    @BeforeEach
    public void setUp() throws Exception {
        resetCounts();
    }

    @Override
    protected String getTestPath() {
        return "/service";
    }

    @Test
    public void bootstrapCustomizedByServiceInitEventObserver() {
        getDriver().get(getRootURL() + "/bootstrap");
        waitForDevServer();

        assertTextEquals(BootstrapCustomizer.APPENDED_TXT,
                BootstrapCustomizer.APPENDED_ID);
    }

    @Test
    public void serviceScopedBeanIsPreservedAcrossUIs() throws IOException {
        open();

        String id = getText("service-id");

        // open another UI
        open();

        Assertions.assertTrue(id.endsWith("-1"),
                "The number of created beans must be 1 but it's "
                        + id.substring(id.indexOf("-") + 1));
        Assertions.assertEquals(id, getText("service-id"));
    }

    @Test
    public void sessionExpiredMessageCustomized() {
        open();
        click(ServiceView.EXPIRE);
        click(ServiceView.ACTION);
        assertSystemMessageEquals(TestSystemMessagesProvider.EXPIRED_BY_TEST);
    }

    @Test
    public void errorHandlerCustomized() throws IOException {
        String counter = TestErrorHandler.class.getSimpleName();
        assertCountEquals(0, counter);
        open();
        click(ServiceView.FAIL);
        assertCountEquals(1, counter);
    }

    @Test
    public void sessionInitEventObserved() throws IOException {
        String initCounter = SessionInitEvent.class.getSimpleName();
        assertCountEquals(0, initCounter);
        getDriver().manage().deleteAllCookies();
        open();
        assertCountEquals(1, initCounter);
    }

    @Test
    public void sessionDestroyEventObserved() throws IOException {
        String destroyCounter = SessionDestroyEvent.class.getSimpleName();
        assertCountEquals(0, destroyCounter);
        open();
        assertCountEquals(0, destroyCounter);
        click(ServiceView.EXPIRE);
        assertCountEquals(1, destroyCounter);
    }

    @Test
    public void uiInitEventObserved() throws IOException {
        String uiInitCounter = UIInitEvent.class.getSimpleName();
        assertCountEquals(0, uiInitCounter);
        open();
        assertCountEquals(1, uiInitCounter);
    }

    private void assertSystemMessageEquals(String expected) {
        WebElement message = findElement(
                By.cssSelector("div.v-system-error div.message"));
        Assertions.assertEquals(expected, message.getText());
    }

}
