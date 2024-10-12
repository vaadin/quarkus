package com.vaadin.quarkus;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.DefaultMenuAccessControl;
import com.vaadin.flow.server.auth.MenuAccessControl;
import com.vaadin.quarkus.annotation.VaadinServiceEnabled;
import com.vaadin.quarkus.context.ServiceUnderTestContext;

@QuarkusTest
@TestProfile(QuarkusInstantiatorDefaultsTest.NoBeansTestProfile.class)
class QuarkusInstantiatorDefaultsTest {

    @Inject
    BeanManager beanManager;

    @Inject
    @VaadinServiceEnabled
    QuarkusInstantiatorFactory instantiatorFactory;

    Instantiator instantiator;

    ServiceUnderTestContext serviceUnderTestContext;

    @BeforeEach
    public void setUp() {
        serviceUnderTestContext = new ServiceUnderTestContext(beanManager);
        serviceUnderTestContext.activate();
        instantiator = instantiatorFactory
                .createInstantitor(VaadinService.getCurrent());
    }

    @AfterEach
    public void tearDown() {
        serviceUnderTestContext.tearDownAll();
    }

    @Test
    public void getMenuAccessControl_beanNotProvided_instanceReturned() {
        MenuAccessControl menuAccessControl = instantiator
                .getMenuAccessControl();
        Assertions.assertNotNull(menuAccessControl);
        Assertions.assertTrue(
                menuAccessControl instanceof DefaultMenuAccessControl);
    }

    public static class NoBeansTestProfile implements QuarkusTestProfile {
        @Override
        public String getConfigProfile() {
            return "empty";
        }

        @Override
        public Map<String, String> getConfigOverrides() {
            // Prevents discovering beans defined in QuarkusInstantiatorTest
            return Map.of("quarkus.arc.exclude-types",
                    QuarkusInstantiatorTest.TestMenuAccessControl.class
                            .getName());
        }
    }
}
