/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.quarkus;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

import com.vaadin.flow.function.DeploymentConfiguration;
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
