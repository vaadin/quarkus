package com.vaadin.flow.quarkus.it;

import java.util.Map;

import io.quarkus.devtools.codestarts.quarkus.QuarkusCodestartCatalog.Language;
import io.quarkus.devtools.testing.codestarts.QuarkusCodestartTest;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.vaadin.flow.quarkus.it.CodestartTestUtils.assertThatHasProductionProfile;
import static com.vaadin.flow.quarkus.it.CodestartTestUtils.assertThatHasVaadinBom;
import static com.vaadin.flow.quarkus.it.CodestartTestUtils.assertThatHasVaadinQuarkusExtension;
import static io.quarkus.devtools.testing.SnapshotTesting.checkContains;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class VaadinExtensionCodestartTest {

    @RegisterExtension
    public static QuarkusCodestartTest codestartTest = QuarkusCodestartTest
            .builder().languages(Language.JAVA)
            .setupStandaloneExtensionTest("com.vaadin:vaadin-quarkus")
            .putData("vaadin-flow-codestart.vaadinVersion",
                    System.getProperty("vaadin.platform.version", "24.0.5"))
            .build();

    @Test
    void testApplicationContents() throws Throwable {
        // source code
        codestartTest.checkGeneratedSource("org.acme.example.MainView");
        codestartTest.checkGeneratedSource("org.acme.example.AppConfig");
        codestartTest.checkGeneratedSource("org.acme.example.GreetService");

        codestartTest.assertThatGeneratedTreeMatchSnapshots(Language.JAVA,
                "frontend");

        codestartTest.assertThatGeneratedFile(Language.JAVA, ".gitignore")
                .satisfies(checkContains("node_modules/"),
                        checkContains("frontend/generated/"),
                        checkContains("vite.generated.ts"));

        // Check POM file: vaadin-bom, deps, production profile
        codestartTest.assertThatGeneratedFile(Language.JAVA, "pom.xml")
                .satisfies(pomFile -> {
                    Model pom = new DefaultModelReader().read(pomFile.toFile(),
                            Map.of());
                    assertSoftly(soft -> {
                        assertThatHasVaadinBom(pom, soft);
                        assertThatHasVaadinQuarkusExtension(pom, soft);
                        assertThatHasProductionProfile(pom, soft);
                    });
                });
    }

}
