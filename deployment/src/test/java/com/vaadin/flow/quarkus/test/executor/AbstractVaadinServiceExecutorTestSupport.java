package com.vaadin.flow.quarkus.test.executor;

import java.util.concurrent.Executor;

import io.quarkus.test.QuarkusUnitTest;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.server.ServiceException;

abstract class AbstractVaadinServiceExecutorTestSupport {

    @Inject
    BeanManager beanManager;

    @Inject
    ManagedExecutor managedExecutor;

    abstract Executor expectedExecutor();

    @Test
    void getExecutor_serviceInitialized_getsExpectedExecutor()
            throws ServiceException {
        var service = new TestQuarkusVaadinServletService(beanManager);
        service.init();
        var executor = service.getExecutor();
        Assertions.assertSame(expectedExecutor(), executor);
    }

    static QuarkusUnitTest quarkusUnitTest(Class<?>... testClasses) {
        return new QuarkusUnitTest()
                .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                        .addClass(TestQuarkusVaadinServletService.class)
                        .addClasses(testClasses).addAsManifestResource(
                                EmptyAsset.INSTANCE, "beans.xml"));
    }

}
