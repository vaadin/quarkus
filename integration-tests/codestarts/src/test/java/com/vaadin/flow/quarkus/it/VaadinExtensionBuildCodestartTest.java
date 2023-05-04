package com.vaadin.flow.quarkus.it;

import io.quarkus.devtools.codestarts.quarkus.QuarkusCodestartCatalog.Language;
import io.quarkus.devtools.testing.codestarts.QuarkusCodestartTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.RegisterExtension;

@EnabledIfSystemProperty(named = "vaadin.platform.version", matches = ".*", disabledReason = "Project should not have dependencies on platform. "
        + "Vaadin platform version should be provided as system property to test code start build.")
public class VaadinExtensionBuildCodestartTest {

    @RegisterExtension
    public static QuarkusCodestartTest codestartTest = QuarkusCodestartTest
            .builder().languages(Language.JAVA)
            .setupStandaloneExtensionTest("com.vaadin:vaadin-quarkus")
            .putData("vaadin-flow-codestart.vaadinVersion",
                    System.getProperty("vaadin.platform.version"))
            .build();

    /**
     * This test runs the build (with tests) on generated projects for all
     * selected languages. To compile the source classes dependencies to vaadin
     * platform artifacts are required. Use {@literal vaadin.platform.version}
     * system property to provide a valid Vaadin platform version to enable the
     * test, for example {@literal -Dvaadin.platform.version=24.1-SNAPSHOT}
     */
    @Test
    void buildAllProjects() throws Throwable {
        codestartTest.buildAllProjects();
    }
}
