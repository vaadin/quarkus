package com.vaadin.quarkus;

import javax.enterprise.inject.spi.BeanManager;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.UsageStatistics;

public class UsageStatisticsTest {

    @After
    public void cleanup() {
        UsageStatistics.clearEntries();
    }
    
    @Test
    public void devMode_callsUsageStatistics() {
        initMocks(false);

        // QuarkusVaadinServlet will call statistics in development mode
        // There will be other entries too to filter out
        List<UsageStatistics.UsageEntry> entries = UsageStatistics.getEntries()
                .filter(entry -> entry.getName().contains("quarkus"))
                .collect(Collectors.toList());
        Assert.assertEquals("One quarkus call should be recorded", 1,
                entries.size());

        UsageStatistics.UsageEntry entry = entries.get(0);
        Assert.assertEquals("flow/quarkus", entry.getName());
    }

    @Test
    public void prodMode_noCallToUsageStatistics() {
        initMocks(true);

        // QuarkusVaadinServlet will call statistics in development mode
        // There will be other entries too to filter out
        List<UsageStatistics.UsageEntry> entries = UsageStatistics.getEntries()
                .filter(entry -> entry.getName().contains("quarkus"))
                .collect(Collectors.toList());
        Assert.assertEquals("No entries should be available in production mode",
                0, entries.size());
    }

    private void initMocks(boolean productionMode) {
        final QuarkusVaadinServlet servlet = Mockito
                .mock(QuarkusVaadinServlet.class);
        final DeploymentConfiguration configuration = Mockito
                .mock(DeploymentConfiguration.class);
        Mockito.when(configuration.isProductionMode())
                .thenReturn(productionMode);
        Mockito.when(configuration.getInitParameters())
                .thenReturn(new Properties());
        final BeanManager beanManager = Mockito.mock(BeanManager.class);
        // creation of quarkus vaadin servlet service calls statistics
        new QuarkusVaadinServletService(servlet, configuration, beanManager);
    }
}
