/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.quarkus.deployment.vaadinplugin;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.plugin.base.BuildFrontendUtil;
import com.vaadin.flow.server.Constants;
import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.BundleValidationUtil;
import com.vaadin.flow.server.frontend.FrontendUtils;
import com.vaadin.flow.server.frontend.Options;
import com.vaadin.flow.server.frontend.TaskCleanFrontendFiles;
import com.vaadin.flow.server.frontend.scanner.ClassFinder;
import com.vaadin.flow.server.frontend.scanner.FrontendDependenciesScanner;
import com.vaadin.flow.theme.Theme;
import com.vaadin.pro.licensechecker.LicenseChecker;
import com.vaadin.pro.licensechecker.MissingLicenseKeyException;
import io.quarkus.bootstrap.model.ApplicationModel;
import io.quarkus.bootstrap.workspace.WorkspaceModule;
import io.quarkus.builder.BuildException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;

/**
 * Implementation of the Vaadin plugin.
 * <p>
 * </p>
 * This class is a porting of Vaadin Maven prepare-frontend and build-frontend
 * mojos.
 */
public final class VaadinPlugin {

    private final QuarkusPluginAdapter pluginAdapter;

    /**
     * Creates a new instance of Quarkus Vaadin plugin for the given build
     * configuration and application.
     *
     * @param vaadinConfig
     *            the Vaadin build configuration.
     * @param applicationModel
     *            the application model.
     */
    private VaadinPlugin(VaadinBuildTimeConfig vaadinConfig,
            ApplicationModel applicationModel, WorkspaceModule appModule) {
        this.pluginAdapter = new QuarkusPluginAdapter(vaadinConfig,
                applicationModel, appModule);
    }

    /**
     * Creates a new instance of the VaadinPlugin based on the provided
     * configuration and application model. If necessary, it attempts to load
     * workspace information for the plugin.
     *
     * @param vaadinConfig
     *            the Vaadin build time configuration.
     * @param applicationModel
     *            the application model representing the current application.
     * @param outputTarget
     *            the target directory for output operations.
     * @return an instance of VaadinPlugin initialized with the given
     *         parameters.
     * @throws BuildException
     *             if workspace information cannot be loaded or an error occurs
     *             during the process.
     */
    public static VaadinPlugin of(VaadinBuildTimeConfig vaadinConfig,
            ApplicationModel applicationModel, Path outputTarget)
            throws BuildException {
        WorkspaceModule module = applicationModel.getApplicationModule();
        if (module == null) {
            try {
                module = WorkspaceInfo.load(outputTarget);
            } catch (Exception e) {
                throw new BuildException(
                        "Cannot load workspace information for Vaadin plugin",
                        e, List.of());
            }
        }
        return new VaadinPlugin(vaadinConfig, applicationModel, module);
    }

    /**
     * Checks that node and npm tools are installed and creates or updates
     * `package.json` and the frontend build tool configuration files.
     * <p>
     * </p>
     * Copies frontend resources available inside `.jar` dependencies to
     * `node_modules` when building a jar package.
     *
     * @throws BuildException
     *             if any error occurs.
     */
    public void prepareFrontend() throws BuildException {
        // propagate info via System properties and token file
        BuildFrontendUtil.propagateBuildInfo(pluginAdapter);

        try {
            BuildFrontendUtil.prepareFrontend(pluginAdapter);
        } catch (Exception exception) {
            throw new BuildException("Could not execute prepare-frontend goal.",
                    exception, List.of());
        }
    }

    /**
     * Builds the frontend bundle.
     * <p>
     * </p>
     * It performs the following actions when creating a package:
     * <ul>
     * <li>Update {@link Constants#PACKAGE_JSON} file with the
     * {@link NpmPackage} annotations defined in the classpath,</li>
     * <li>Copy resource files used by flow from `.jar` files to the
     * `node_modules` folder</li>
     * <li>Install dependencies by running <code>npm install</code></li>
     * <li>Update the {@link FrontendUtils#IMPORTS_NAME} file imports with the
     * {@link JsModule} {@link Theme} and {@link JavaScript} annotations defined
     * in the classpath,</li>
     * <li>Update {@link FrontendUtils#VITE_CONFIG} file.</li>
     * </ul>
     *
     * @param emitter
     *            generated files emitter.
     * @throws BuildException
     *             if any error occurs.
     */
    public void buildFrontend(BiConsumer<String, byte[]> emitter)
            throws BuildException {
        long start = System.nanoTime();

        FrontendDependenciesScanner frontendDependencies = createFrontendScanner();
        try {
            BuildFrontendUtil.runNodeUpdater(pluginAdapter,
                    frontendDependencies);
        } catch (ExecutionFailedException | URISyntaxException exception) {
            throw new BuildException("Could not execute build-frontend goal",
                    exception, List.of());
        }

        if (pluginAdapter.generateBundle()
                && BundleValidationUtil.needsBundleBuild(
                        pluginAdapter.servletResourceOutputDirectory())) {
            try {
                BuildFrontendUtil.runFrontendBuild(pluginAdapter);
            } catch (URISyntaxException | TimeoutException exception) {
                throw new BuildException(exception.getMessage(), exception,
                        List.of());
            }
        }
        LicenseChecker.setStrictOffline(true);
        boolean licenseRequired;
        boolean commercialBannerRequired;
        try {
            licenseRequired = BuildFrontendUtil.validateLicenses(pluginAdapter,
                    frontendDependencies);
            commercialBannerRequired = false;
        } catch (MissingLicenseKeyException ex) {
            licenseRequired = true;
            commercialBannerRequired = true;
            pluginAdapter.logInfo(ex.getMessage());
        }

        BuildFrontendUtil.updateBuildFile(pluginAdapter, licenseRequired,
                commercialBannerRequired);

        long ms = (System.nanoTime() - start) / 1000000;
        pluginAdapter.logInfo("Build frontend completed in " + ms + " ms.");

        emitGeneratedFiles(emitter);
    }

    private void emitGeneratedFiles(BiConsumer<String, byte[]> emitter)
            throws BuildException {
        Path vaadinMetaInfDir = pluginAdapter.servletResourceOutputDirectory()
                .toPath();
        Path buildFolder = pluginAdapter.buildDir();

        if (Files.exists(vaadinMetaInfDir)) {
            try (var stream = Files.walk(vaadinMetaInfDir)) {
                stream.filter(Files::isRegularFile).forEach(filePath -> {
                    try {
                        // Calculate relative path from target/classes
                        Path relativePath = buildFolder.relativize(filePath);
                        byte[] content = Files.readAllBytes(filePath);
                        emitter.accept(
                                relativePath.toString().replace('\\', '/'),
                                content);

                        pluginAdapter.logDebug(
                                "Added Vaadin resource: " + relativePath);
                    } catch (IOException e) {
                        pluginAdapter
                                .logWarn("Failed to read Vaadin resource file: "
                                        + filePath, e);
                    }
                });

                pluginAdapter.logInfo(
                        "Added Vaadin frontend resources from META-INF/VAADIN to artifact");

            } catch (IOException e) {
                throw new BuildException(
                        "Failed to scan Vaadin resources directory", e,
                        List.of());
            }
        } else {
            pluginAdapter.logInfo(
                    "No META-INF/VAADIN directory found, skipping resource addition");
        }
    }

    /**
     * Cleans up generated frontend files if the corresponding configuration
     * setting is enabled. The process involves creating a new cleaning task
     * with specific options derived from the plugin adapter's configuration and
     * executing it. If an error occurs during the execution of the clean task,
     * it is logged for debugging purposes.
     * <p>
     * The cleanup operation ensures the following: - Deletes the generated
     * frontend files in the `node_modules` folder. - Utilizes the directory
     * configurations such as frontend directory, npm folder, and generated
     * TypeScript folder to locate the files. - Makes use of the
     * `TaskCleanFrontendFiles` for cleanup operations.
     */
    public void clean() {
        if (pluginAdapter.cleanFrontendFiles()) {
            Options options = new Options(null, pluginAdapter.getClassFinder(),
                    pluginAdapter.npmFolder())
                    .withFrontendDirectory(pluginAdapter.frontendDirectory())
                    .withFrontendGeneratedFolder(
                            pluginAdapter.generatedTsFolder());
            TaskCleanFrontendFiles cleanTask = new TaskCleanFrontendFiles(
                    options);
            try {
                cleanTask.execute();
            } catch (ExecutionFailedException exception) {
                pluginAdapter.logError("Error cleaning frontend files",
                        exception);
            }
        }
    }

    private FrontendDependenciesScanner createFrontendScanner() {
        boolean reactEnabled = pluginAdapter.isReactEnabled()
                && FrontendUtils.isReactRouterRequired(
                        BuildFrontendUtil.getFrontendDirectory(pluginAdapter));
        ClassFinder classFinder = pluginAdapter.getClassFinder();
        FeatureFlags featureFlags = new FeatureFlags(
                pluginAdapter.createLookup(classFinder));
        if (pluginAdapter.javaResourceFolder() != null) {
            featureFlags
                    .setPropertiesLocation(pluginAdapter.javaResourceFolder());
        }
        return new FrontendDependenciesScanner.FrontendDependenciesScannerFactory()
                .createScanner(!pluginAdapter.optimizeBundle(), classFinder,
                        pluginAdapter.generateEmbeddableWebComponents(),
                        featureFlags, reactEnabled);
    }

}
