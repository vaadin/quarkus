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
package com.vaadin.quarkus;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.InitParameters;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.VaadinServletService;

/**
 * Servlet to create {@link QuarkusVaadinServletService}.
 *
 * An instance of this servlet is automatically registered if no other custom
 * VaadinServlet class with Servlet 3.0 annotations is present on classpath. A
 * subclass of this servlet can be to provide a customized
 * {@link QuarkusVaadinServletService} implementation, in which case
 * {@link #createServletService(DeploymentConfiguration)} must call
 * {@code service.init()}.
 */
public class QuarkusVaadinServlet extends VaadinServlet {

    @Inject
    BeanManager beanManager;

    private static final ThreadLocal<Optional<String>> SERVLET_NAME = new ThreadLocal<>();

    /**
     * Prefix used for Vaadin configuration properties defined through
     * MicroProfile Config (e.g. in {@code application.properties} or
     * {@code application.yaml}), matching the convention used by the Vaadin
     * Spring integration.
     */
    private static final String CONFIGURATION_PREFIX = "vaadin.";

    /**
     * Names of all known Vaadin configuration parameters, as declared by the
     * {@link InitParameters} constants.
     */
    private static final List<String> CONFIGURATION_PROPERTY_NAMES = Stream
            .of(InitParameters.class.getDeclaredFields())
            .filter(field -> Modifier.isStatic(field.getModifiers())
                    && field.getType() == String.class)
            .map(QuarkusVaadinServlet::getStringValue)
            .collect(Collectors.toList());

    @Override
    protected VaadinServletService createServletService(
            final DeploymentConfiguration configuration)
            throws ServiceException {

        final QuarkusVaadinServletService service = new QuarkusVaadinServletService(
                this, configuration, this.beanManager);
        service.init();
        return service;
    }

    @Override
    protected DeploymentConfiguration createDeploymentConfiguration(
            final Properties initParameters) {
        applyConfigurationProperties(initParameters);
        return super.createDeploymentConfiguration(initParameters);
    }

    /**
     * Copies Vaadin configuration properties defined through MicroProfile
     * Config (e.g. in {@code application.properties} or
     * {@code application.yaml}) into the servlet init parameters, so that they
     * are taken into account when building the {@link DeploymentConfiguration}.
     * <p>
     * Each known Vaadin parameter is looked up using the {@code vaadin.} prefix
     * and, when present, overrides any value coming from servlet init
     * parameters.
     *
     * @param initParameters
     *            the init parameters to enrich, not {@code null}
     */
    static void applyConfigurationProperties(
            final Properties initParameters) {
        final Config config = ConfigProvider.getConfig();
        for (final String name : CONFIGURATION_PROPERTY_NAMES) {
            config.getOptionalValue(CONFIGURATION_PREFIX + name, String.class)
                    .ifPresent(value -> initParameters.setProperty(name,
                            value));
        }
    }

    private static String getStringValue(final Field field) {
        try {
            return (String) field.get(null);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(
                    "Unable to read Vaadin init parameter name from field "
                            + field.getName(),
                    e);
        }
    }

    @Override
    public void init(final ServletConfig servletConfig)
            throws ServletException {
        SERVLET_NAME.set(Optional.of(servletConfig.getServletName()));
        try {
            super.init(servletConfig);
        } finally {
            SERVLET_NAME.set(Optional.empty());
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        SERVLET_NAME.remove();
    }

    @Override
    protected void service(final HttpServletRequest request,
            final HttpServletResponse response)
            throws ServletException, IOException {
        SERVLET_NAME.set(Optional.of(getServletName()));
        try {
            super.service(request, response);
        } finally {
            SERVLET_NAME.set(Optional.empty());
        }
    }

    /**
     * Name of the Vaadin servlet for the current thread.
     * <p>
     * Until VaadinService appears in CurrentInstance, it have to be used to get
     * the servlet name.
     * <p>
     * This method is meant for internal use only.
     *
     * @return currently processing vaadin servlet name
     * @see VaadinServlet#getCurrent()
     */
    public static Optional<String> getCurrentServletName() {
        return SERVLET_NAME.get();
    }

}
