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

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.builditem.GeneratedResourceBuildItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.vaadin.flow.internal.FrontendUtils;
import com.vaadin.flow.server.Constants;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratedResourceEmitterTest {

    /**
     * Emitted paths are relative to the resources output directory, and the
     * Vaadin build writes below {@literal META-INF/VAADIN} in it.
     */
    private static final String BUNDLE = Constants.VAADIN_SERVLET_RESOURCES
            + "webapp/VAADIN/build/generated-flow-imports-D6_me9xd.js";

    private static final String TOKEN = Constants.VAADIN_SERVLET_RESOURCES
            + FrontendUtils.TOKEN_FILE;

    private final List<GeneratedResourceBuildItem> emitted = new ArrayList<>();

    private final BuildProducer<GeneratedResourceBuildItem> producer = emitted::add;

    @TempDir
    Path tempDir;

    @Test
    void emit_outputDirectoryIsPackaged_emitsOnlyTokenFile() {
        // What a Maven build looks like: Quarkus packages target/classes and
        // the Vaadin build writes into target/classes/META-INF/VAADIN
        Path classesDir = tempDir.resolve("classes");

        GeneratedResourceEmitter emitter = GeneratedResourceEmitter.of(
                List.of(classesDir), generatedResourcesDirectory(classesDir),
                producer);

        assertTrue(emitter.isPackagedFromOutputDirectory(),
                "The generated resources directory is inside the directory "
                        + "the application is packaged from");

        emitAll(emitter);

        assertIterableEquals(List.of(TOKEN), emittedNames(),
                "The bundle is packaged from the output directory, but the "
                        + "token file is deleted from it once the build is "
                        + "done, so only the token file has to be emitted");
    }

    @Test
    void emit_outputDirectoryIsUnderOneOfSeveralRoots_emitsOnlyTokenFile() {
        // What a Gradle build looks like: two packaged roots, and the Vaadin
        // build writes below the resources one
        Path classesRoot = tempDir.resolve("classes").resolve("java")
                .resolve("main");
        Path resourcesRoot = tempDir.resolve("resources").resolve("main");

        GeneratedResourceEmitter emitter = GeneratedResourceEmitter.of(
                List.of(classesRoot, resourcesRoot),
                generatedResourcesDirectory(resourcesRoot), producer);

        assertTrue(emitter.isPackagedFromOutputDirectory(),
                "The generated resources directory is under the second root");

        emitAll(emitter);

        assertIterableEquals(List.of(TOKEN), emittedNames());
    }

    @Test
    void emit_outputDirectoryIsNotPackaged_emitsEverything() {
        // The generated resources directory was configured to somewhere the
        // application is not packaged from
        Path classesDir = tempDir.resolve("classes");
        Path generatedDir = tempDir.resolve("generated").resolve("vaadin");

        GeneratedResourceEmitter emitter = GeneratedResourceEmitter
                .of(List.of(classesDir), generatedDir, producer);

        assertFalse(emitter.isPackagedFromOutputDirectory());

        emitAll(emitter);

        assertIterableEquals(List.of(BUNDLE, TOKEN), emittedNames(),
                "Packaging cannot pick anything up from a directory it does "
                        + "not copy, so everything has to be emitted");
    }

    @Test
    void emit_packagedRootIsSealedArchive_emitsEverything() throws IOException {
        Path classesDir = tempDir.resolve("classes");
        Path archive = tempDir.resolve("application.jar");
        try (ZipOutputStream zip = new ZipOutputStream(
                Files.newOutputStream(archive))) {
            zip.putNextEntry(new ZipEntry("README"));
            zip.closeEntry();
        }

        // When the application is packaged from a JAR, Quarkus does not report
        // the JAR itself as the archive root: it mounts it as a file system and
        // reports the root of that one, so the root is a path of a different
        // file system than the generated resources directory. That is what
        // makes Path.startsWith return false and what this test has to
        // reproduce, so it mounts a real JAR rather than using a plain path.
        try (FileSystem archiveFs = FileSystems.newFileSystem(
                URI.create("jar:" + archive.toUri()), Map.of())) {
            GeneratedResourceEmitter emitter = GeneratedResourceEmitter.of(
                    archiveFs.getRootDirectories(),
                    generatedResourcesDirectory(classesDir), producer);

            assertFalse(emitter.isPackagedFromOutputDirectory(),
                    "A root mounted from a JAR was sealed before this build "
                            + "ran, so nothing written to the output directory "
                            + "afterwards reaches the application on its own");

            emitAll(emitter);

            assertIterableEquals(List.of(BUNDLE, TOKEN), emittedNames());
        }
    }

    @Test
    void emit_noPackagedRoots_emitsEverything() {
        Path classesDir = tempDir.resolve("classes");

        GeneratedResourceEmitter emitter = GeneratedResourceEmitter.of(
                List.of(), generatedResourcesDirectory(classesDir), producer);

        assertFalse(emitter.isPackagedFromOutputDirectory());

        emitAll(emitter);

        assertIterableEquals(List.of(BUNDLE, TOKEN), emittedNames());
    }

    @Test
    void emit_pathsResemblingTokenFile_emitsOnlyTheTokenFile() {
        Path classesDir = tempDir.resolve("classes");

        GeneratedResourceEmitter emitter = GeneratedResourceEmitter.of(
                List.of(classesDir), generatedResourcesDirectory(classesDir),
                producer);

        emitter.accept(Constants.VAADIN_SERVLET_RESOURCES
                + "config/not-flow-build-info.json", content());
        emitter.accept(Constants.VAADIN_SERVLET_RESOURCES
                + "backup-config/flow-build-info.json", content());
        emitter.accept(TOKEN, content());

        assertIterableEquals(List.of(TOKEN), emittedNames(),
                "Only the token file itself is emitted, not the files whose "
                        + "path happens to end like it");
    }

    /**
     * The directory the Vaadin build writes its generated resources into,
     * {@literal META-INF/VAADIN} below the resources output directory.
     */
    private static Path generatedResourcesDirectory(Path resourcesOutputDir) {
        return resourcesOutputDir.resolve("META-INF").resolve("VAADIN");
    }

    private void emitAll(GeneratedResourceEmitter emitter) {
        emitter.accept(BUNDLE, content());
        emitter.accept(TOKEN, content());
    }

    private static byte[] content() {
        return "content".getBytes(StandardCharsets.UTF_8);
    }

    private List<String> emittedNames() {
        return emitted.stream().map(GeneratedResourceBuildItem::getName)
                .toList();
    }
}
