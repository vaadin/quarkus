/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.quarkus;

import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.server.InitParameters;

/**
 * Verifies that Vaadin configuration defined through MicroProfile Config (e.g.
 * in {@code application.properties}) is copied into the servlet init parameters
 * by {@link QuarkusVaadinServlet}.
 *
 * @see <a href="https://github.com/vaadin/quarkus/issues/309">vaadin/quarkus
 *      #309</a>
 */
public class QuarkusVaadinServletConfigurationTest {

    @AfterEach
    public void clearProperties() {
        System.clearProperty(
                "vaadin." + InitParameters.SERVLET_PARAMETER_HEARTBEAT_INTERVAL);
        System.clearProperty("vaadin."
                + InitParameters.SERVLET_PARAMETER_CLOSE_IDLE_SESSIONS);
    }

    @Test
    public void configurationProperties_copiedIntoInitParameters() {
        System.setProperty("vaadin."
                + InitParameters.SERVLET_PARAMETER_HEARTBEAT_INTERVAL, "120");
        System.setProperty("vaadin."
                + InitParameters.SERVLET_PARAMETER_CLOSE_IDLE_SESSIONS, "true");

        Properties initParameters = new Properties();
        QuarkusVaadinServlet.applyConfigurationProperties(initParameters);

        Assertions.assertEquals("120", initParameters.getProperty(
                InitParameters.SERVLET_PARAMETER_HEARTBEAT_INTERVAL));
        Assertions.assertEquals("true", initParameters.getProperty(
                InitParameters.SERVLET_PARAMETER_CLOSE_IDLE_SESSIONS));
    }

    @Test
    public void configurationProperties_overrideExistingInitParameters() {
        System.setProperty("vaadin."
                + InitParameters.SERVLET_PARAMETER_HEARTBEAT_INTERVAL, "120");

        Properties initParameters = new Properties();
        initParameters.setProperty(
                InitParameters.SERVLET_PARAMETER_HEARTBEAT_INTERVAL, "300");

        QuarkusVaadinServlet.applyConfigurationProperties(initParameters);

        Assertions.assertEquals("120", initParameters.getProperty(
                InitParameters.SERVLET_PARAMETER_HEARTBEAT_INTERVAL));
    }

    @Test
    public void noConfigurationProperties_initParametersUnchanged() {
        Properties initParameters = new Properties();
        initParameters.setProperty(
                InitParameters.SERVLET_PARAMETER_HEARTBEAT_INTERVAL, "300");

        QuarkusVaadinServlet.applyConfigurationProperties(initParameters);

        Assertions.assertEquals("300", initParameters.getProperty(
                InitParameters.SERVLET_PARAMETER_HEARTBEAT_INTERVAL));
    }
}
