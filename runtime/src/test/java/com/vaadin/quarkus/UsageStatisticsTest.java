package com.vaadin.quarkus;

import javax.enterprise.inject.spi.BeanManager;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.UsageStatistics;

public class UsageStatisticsTest {

    @AfterEach
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
        Assertions.assertEquals(1, entries.size(),
                "One quarkus call should be recorded");

        UsageStatistics.UsageEntry entry = entries.get(0);
        Assertions.assertEquals("flow/quarkus", entry.getName());
    }

    @Test
    public void prodMode_noCallToUsageStatistics() {
        initMocks(true);

        // QuarkusVaadinServlet will call statistics in development mode
        // There will be other entries too to filter out
        List<UsageStatistics.UsageEntry> entries = UsageStatistics.getEntries()
                .filter(entry -> entry.getName().contains("quarkus"))
                .collect(Collectors.toList());
        Assertions.assertEquals(0, entries.size(),
                "No entries should be available in production mode");
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
