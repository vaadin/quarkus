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

import java.io.IOException;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.quarkus.it.service.ServiceView;
import com.vaadin.flow.quarkus.it.service.TestErrorHandler;
import com.vaadin.flow.quarkus.it.service.TestSystemMessagesProvider;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ServiceBeanTest extends AbstractCdiTest {

    @BeforeEach
    public void setUp() throws Exception {
        resetCounts();
    }

    @Override
    protected String getTestPath() {
        return "/service";
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

    private void assertSystemMessageEquals(String expected) {
        WebElement message = findElement(
                By.cssSelector("div.v-system-error div.message"));
        Assertions.assertEquals(expected, message.getText());
    }
}
