package com.vaadin.flow.quarkus.test.executor;

import java.util.Properties;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.servlet.ServletContext;

import com.vaadin.flow.di.Lookup;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.quarkus.QuarkusVaadinServlet;
import com.vaadin.quarkus.QuarkusVaadinServletService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestQuarkusVaadinServletService extends QuarkusVaadinServletService {

    TestQuarkusVaadinServletService(BeanManager beanManager) {
        super(mock(QuarkusVaadinServlet.class),
                mock(DeploymentConfiguration.class), beanManager);
        when(getServlet().getServletName()).thenReturn("QuarkusVaadinServlet");
        when(getServlet().getService()).thenReturn(this);
        final ServletContext servletcontext = mock(ServletContext.class);
        when(getServlet().getServletContext()).thenReturn(servletcontext);
        when(servletcontext.getAttribute(Lookup.class.getName()))
                .thenReturn(mock(Lookup.class));
        DeploymentConfiguration config = getDeploymentConfiguration();
        Properties properties = new Properties();
        when(config.getInitParameters()).thenReturn(properties);
    }

    // We have nothing to do with atmosphere,
    // and mocking is much easier without it.
    @Override
    protected boolean isAtmosphereAvailable() {
        return false;
    }

    @Override
    public void setClassLoader(ClassLoader classLoader) {
        if (classLoader != null) {
            super.setClassLoader(classLoader);
        }
    }

}
