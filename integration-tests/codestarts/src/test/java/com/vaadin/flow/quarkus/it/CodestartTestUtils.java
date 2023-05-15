package com.vaadin.flow.quarkus.it;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.RepositoryBase;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.Assertions.assertThat;

final class CodestartTestUtils {

    static void assertThatHasVaadinBom(Model pom, SoftAssertions soft) {
        soft.assertThat(pom.getDependencyManagement().getDependencies())
                .filteredOn(dep -> "com.vaadin".equals(dep.getGroupId())
                        && "vaadin-bom".equals(dep.getArtifactId()))
                .singleElement()
                .extracting(Dependency::getType, Dependency::getScope)
                .contains("pom", "import");
    }

    static void assertThatHasVaadinQuarkusExtension(Model pom,
            SoftAssertions soft) {
        soft.assertThat(pom.getDependencies())
                .filteredOn(dep -> "com.vaadin".equals(dep.getGroupId())
                        && "vaadin-quarkus-extension"
                                .equals(dep.getArtifactId()))
                .hasSize(1);
    }

    static void assertThatHasProductionProfile(Model pom, SoftAssertions soft) {
        soft.assertThat(pom.getProfiles())
                .filteredOn(dep -> "production".equals(dep.getId()))
                .flatExtracting(profile -> profile.getBuild().getPlugins())
                .filteredOn(plugin -> "com.vaadin".equals(plugin.getGroupId())
                        && "vaadin-maven-plugin".equals(plugin.getArtifactId()))
                .singleElement()
                .satisfies(plugin -> assertThat(plugin.getExecutions())
                        .flatMap(PluginExecution::getGoals)
                        .contains("prepare-frontend", "build-frontend"))
                .extracting(Plugin::getVersion).isEqualTo("${vaadin.version}");
    }

    static void assertThatHasPreReleaseRepositories(Model pom,
            SoftAssertions soft) {
        soft.assertThat(pom.getRepositories())
                .filteredOn(repo -> "vaadin-prereleases".equals(repo.getId()))
                .singleElement().extracting(RepositoryBase::getUrl)
                .isEqualTo("https://maven.vaadin.com/vaadin-prereleases/");
        soft.assertThat(pom.getPluginRepositories())
                .filteredOn(repo -> "vaadin-prereleases".equals(repo.getId()))
                .singleElement().extracting(RepositoryBase::getUrl)
                .isEqualTo("https://maven.vaadin.com/vaadin-prereleases/");

    }

}
