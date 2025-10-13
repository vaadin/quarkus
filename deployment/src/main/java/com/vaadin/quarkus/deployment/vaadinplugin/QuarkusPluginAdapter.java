/*
 * Copyright 2025 Marco Collovati, Dario Götze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vaadin.quarkus.deployment.vaadinplugin;

import com.vaadin.flow.internal.StringUtil;
import com.vaadin.flow.plugin.base.BuildFrontendUtil;
import com.vaadin.flow.plugin.base.PluginAdapterBuild;
import com.vaadin.flow.server.frontend.FrontendUtils;
import com.vaadin.flow.server.frontend.installer.Platform;
import com.vaadin.flow.server.frontend.scanner.ClassFinder;
import com.vaadin.flow.server.scanner.ReflectionsClassFinder;
import com.vaadin.flow.utils.FlowFileUtils;
import io.quarkus.bootstrap.model.ApplicationModel;
import io.quarkus.bootstrap.workspace.SourceDir;
import io.quarkus.bootstrap.workspace.WorkspaceModule;
import io.quarkus.runtime.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Quarkus implementation of Vaadin build plugin adapter.
 */
class QuarkusPluginAdapter implements PluginAdapterBuild {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(QuarkusPluginAdapter.class);

    private final VaadinBuildTimeConfig config;
    private final ApplicationModel model;
    private final WorkspaceModule appModule;
    private final SourceDir sourcesDir;
    private final SourceDir resourcesDir;

    /**
     * Creates a new instance of {@link QuarkusPluginAdapter} for the give build
     * configuration and application.
     *
     * @param config
     *            the Vaadin build configuration.
     * @param applicationModel
     *            the application model.
     */
    QuarkusPluginAdapter(VaadinBuildTimeConfig config,
            ApplicationModel applicationModel) {
        this(config, applicationModel, applicationModel.getApplicationModule());
    }

    /**
     * Creates a new instance of {@link QuarkusPluginAdapter} for the give build
     * configuration and application.
     *
     * @param config
     *            the Vaadin build configuration.
     * @param applicationModel
     *            the application model.
     * @param appModule
     *            the application module.
     */
    QuarkusPluginAdapter(VaadinBuildTimeConfig config,
            ApplicationModel applicationModel, WorkspaceModule appModule) {
        this.config = config;
        this.model = applicationModel;
        this.appModule = appModule;
        SourceDir assumedSources = SourceDir.of(
                appModule.getModuleDir().toPath()
                        .resolve(Paths.get("src", "main", "java")),
                appModule.getBuildDir().toPath().resolve("classes"));
        SourceDir assumedResources = SourceDir.of(
                appModule.getModuleDir().toPath()
                        .resolve(Paths.get("src", "main", "resources")),
                appModule.getBuildDir().toPath().resolve("classes"));
        if (appModule.hasMainSources()) {
            sourcesDir = appModule.getMainSources().getSourceDirs().stream()
                    .findFirst().orElse(assumedSources);
            resourcesDir = appModule.getMainSources().getResourceDirs().stream()
                    .findFirst().orElse(assumedResources);
        } else {
            sourcesDir = assumedSources;
            resourcesDir = assumedResources;
        }
    }

    @Override
    public File frontendResourcesDirectory() {
        return resolveProjectDirectory(config.frontendResourcesDirectory(),
                "vaadin.build.frontendResourcesDirectory");
    }

    @Override
    public boolean generateBundle() {
        return config.generateBundle();
    }

    @Override
    public boolean generateEmbeddableWebComponents() {
        return config.generateEmbeddableWebComponents();
    }

    @Override
    public boolean optimizeBundle() {
        return config.optimizeBundle();
    }

    @Override
    public boolean runNpmInstall() {
        return config.runNpmInstall();
    }

    @Override
    public boolean ciBuild() {
        return config.ciBuild();
    }

    @Override
    public boolean forceProductionBuild() {
        return config.forceProductionBuild();
    }

    @Override
    public boolean compressBundle() {
        return true;
    }

    @Override
    public boolean checkRuntimeDependency(String groupId, String artifactId,
            Consumer<String> missingDependencyMessageConsumer) {
        if (model.getRuntimeDependencies().stream().noneMatch(
                dependency -> dependency.getGroupId().equals(groupId))) {
            if (missingDependencyMessageConsumer != null) {
                missingDependencyMessageConsumer.accept(String.format(
                        """
                                The dependency %1$s:%2$s has not been found in the project configuration.
                                Please add the following dependency to your POM file:

                                <dependency>
                                    <groupId>%1$s</groupId>
                                    <artifactId>%2$s</artifactId>
                                    <scope>runtime</scope>
                                </dependency>
                                """,
                        groupId, artifactId));
            }
            return true;
        }
        return true;
    }

    @Override
    public File applicationProperties() {
        return config.applicationProperties();
    }

    @Override
    public boolean eagerServerLoad() {
        return config.eagerServerLoad();
    }

    @Override
    public File frontendDirectory() {
        return resolveProjectDirectory(config.frontendDirectory(),
                "vaadin.build.frontedDirectory");
    }

    private File resolveProjectDirectory(File directory, String name) {
        return resolveDirectory(projectBaseDirectory().toFile(), directory,
                name);
    }

    private File resolveBuildDirectory(File directory, String name) {
        return resolveDirectory(resourcesDir.getOutputDir().toFile(), directory,
                name);
    }

    private File resolveDirectory(File base, File directory, String key) {
        if (directory.isAbsolute() && directory.isDirectory()) {
            return directory;
        }
        directory = base.toPath().resolve(directory.toPath()).toFile();
        if (directory.exists() && !directory.isDirectory()) {
            throw new ConfigurationException(
                    key + " must be a directory: " + directory, Set.of(key));
        }
        return directory;
    }

    @Override
    public File generatedTsFolder() {
        return config.generatedTsFolder().orElseGet(() -> frontendDirectory()
                .toPath().resolve(FrontendUtils.GENERATED).toFile());
    }

    private ClassFinder classFinder;

    @Override
    public ClassFinder getClassFinder() {
        if (classFinder == null) {
            URL[] urls = buildClasspath().map(Path::toFile)
                    .map(FlowFileUtils::convertToUrl).toArray(URL[]::new);
            URLClassLoader classLoader = new URLClassLoader(urls,
                    Thread.currentThread().getContextClassLoader());
            classFinder = new ReflectionsClassFinder(classLoader, urls);
        }
        return classFinder;
    }

    @Override
    public Set<File> getJarFiles() {
        return model.getRuntimeDependencies().stream()
                .flatMap(dep -> dep.getResolvedPaths().stream())
                .map(Path::toFile).filter(file -> !file.isDirectory())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isJarProject() {
        return true;
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public File javaSourceFolder() {
        return sourcesDir.getDir().toFile();
    }

    @Override
    public File javaResourceFolder() {
        return resourcesDir.getDir().toFile();
    }

    @Override
    public boolean isFrontendIgnoreVersionChecks() {
        return config.frontendIgnoreVersionChecks();
    }

    @Override
    public void logDebug(CharSequence charSequence) {
        LOGGER.debug(charSequence.toString());
    }

    @Override
    public void logDebug(CharSequence charSequence, Throwable throwable) {
        LOGGER.debug(charSequence.toString(), throwable);
    }

    @Override
    public void logInfo(CharSequence charSequence) {
        LOGGER.info(charSequence.toString());
    }

    @Override
    public void logWarn(CharSequence charSequence) {
        LOGGER.warn(charSequence.toString());
    }

    @Override
    public void logError(CharSequence charSequence) {
        LOGGER.error(charSequence.toString());
    }

    @Override
    public void logWarn(CharSequence charSequence, Throwable throwable) {
        LOGGER.warn(charSequence.toString(), throwable);
    }

    @Override
    public void logError(CharSequence charSequence, Throwable throwable) {
        LOGGER.error(charSequence.toString(), throwable);
    }

    @Override
    public URI nodeDownloadRoot() throws URISyntaxException {
        String nodeDownloadRoot = config.nodeDownloadRoot()
                .orElseGet(() -> Platform.guess().getNodeDownloadRoot());
        try {
            return new URI(nodeDownloadRoot);
        } catch (URISyntaxException e) {
            logError("Failed to parse nodeDownloadRoot uri", e);
            throw new URISyntaxException(nodeDownloadRoot,
                    "Failed to parse nodeDownloadRoot uri");
        }
    }

    @Override
    public boolean nodeAutoUpdate() {
        return config.nodeAutoUpdate();
    }

    @Override
    public String nodeVersion() {
        return config.nodeVersion();
    }

    @Override
    public File npmFolder() {
        return config.npmFolder()
                .map(dir -> resolveProjectDirectory(dir, "npmFolder"))
                .orElseGet(() -> projectBaseDirectory().toFile());
    }

    @Override
    public File openApiJsonFile() {
        return config.openApiJsonFile();
    }

    @Override
    public boolean pnpmEnable() {
        return config.pnpmEnable();
    }

    @Override
    public boolean bunEnable() {
        return config.bunEnable();
    }

    @Override
    public boolean useGlobalPnpm() {
        return config.useGlobalPnpm();
    }

    @Override
    public Path projectBaseDirectory() {
        return appModule.getModuleDir().toPath();
    }

    @Override
    public boolean requireHomeNodeExec() {
        return config.requireHomeNodeExec();
    }

    @Override
    public File servletResourceOutputDirectory() {
        return resolveBuildDirectory(config.resourceOutputDirectory(),
                "resourceOutputDirectory");
    }

    @Override
    public File webpackOutputDirectory() {
        return frontendOutputDirectory();
    }

    @Override
    public File frontendOutputDirectory() {
        File outputDir = resolveBuildDirectory(config.frontendOutputDirectory(),
                "frontendOutputDirectory");
        config.webpackOutputDirectory()
                .map(f -> resolveBuildDirectory(f, "webpackOutputDirectory"))
                .filter(f -> !f.equals(outputDir))
                .ifPresent(deprecatedOutputDir -> logWarn(
                        "Both 'frontendOutputDirectory' and 'webpackOutputDirectory' are set. "
                                + "'webpackOutputDirectory' property will be removed in future releases and will be ignored. "
                                + "Please use only 'frontendOutputDirectory'."));
        return outputDir;
    }

    @Override
    public String buildFolder() {
        Path projectDir = appModule.getModuleDir().toPath();
        Path buildDir = appModule.getBuildDir().toPath();
        if (buildDir.startsWith(projectDir)) {
            return projectDir.relativize(buildDir).toString();
        }
        return buildDir.toString();
    }

    @Override
    public List<String> postinstallPackages() {
        return config.postinstallPackages().orElseGet(List::of);
    }

    @Override
    public boolean isFrontendHotdeploy() {
        return true;
    }

    @Override
    public boolean skipDevBundleBuild() {
        return config.skipDevBundleBuild();
    }

    @Override
    public boolean isPrepareFrontendCacheDisabled() {
        return false;
    }

    @Override
    public boolean isReactEnabled() {
        return config.reactEnabled()
                .orElseGet(() -> FrontendUtils.isReactRouterRequired(
                        BuildFrontendUtil.getFrontendDirectory(this)));
    }

    @Override
    public String applicationIdentifier() {
        return config.applicationIdentifier().filter(id -> !id.isBlank())
                .orElseGet(() -> "app-" + StringUtil.getHash(
                        model.getAppArtifact().getGroupId()
                                + model.getAppArtifact().getArtifactId(),
                        StandardCharsets.UTF_8));
    }

    @Override
    public List<String> frontendExtraFileExtensions() {
        return config.frontendExtraFileExtensions().orElseGet(List::of);
    }

    @Override
    public boolean isNpmExcludeWebComponents() {
        return config.npmExcludeWebComponents();
    }

    @Override
    public boolean isCommercialBannerEnabled() {
        return config.commercialWithBanner();
    }

    /**
     * Gets whether to cleans generated frontend files after the execution of
     * the frontend build. This is generally enabled by default to ensure a
     * clean state for after the build completes.
     *
     * @return {@code true} if the frontend files will be cleaned, {@code false}
     *         otherwise
     */
    public boolean cleanFrontendFiles() {
        return config.cleanFrontendFiles();
    }

    /**
     * Resolves and returns the build output directory.
     *
     * @return the path representing the build output directory.
     */
    Path buildDir() {
        return resourcesDir.getOutputDir();
    }

    /**
     * Collects the path of the artifacts that compose the application
     * classpath.
     *
     * @return the path of the artifacts that compose the application classpath.
     */
    private Stream<Path> buildClasspath() {
        return Stream.concat(
                appModule.getMainSources().getOutputTree().getRoots().stream(),
                model.getRuntimeDependencies().stream()
                        .flatMap(dep -> dep.getResolvedPaths().stream()));
    }
}
