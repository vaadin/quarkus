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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratedResourceEmitterTest {

    private static final String BUNDLE = "META-INF/VAADIN/build/bundle.js";
    private static final String TOKEN = "META-INF/VAADIN/"
            + FrontendUtils.TOKEN_FILE;

    private final List<GeneratedResourceBuildItem> emitted = new ArrayList<>();

    private final BuildProducer<GeneratedResourceBuildItem> producer = emitted::add;

    @TempDir
    Path tempDir;

    @Test
    void emit_generatedDirectoryNotPackaged_emitsEverything() {
        Path outputDir = tempDir.resolve("classes");
        Path unrelatedRoot = tempDir.resolve("somewhere-else");

        GeneratedResourceEmitter emitter = GeneratedResourceEmitter
                .of(List.of(unrelatedRoot), outputDir, producer);

        assertEquals(false, emitter.isPackagedFromOutputDirectory(),
                "The output directory is not under the packaged root, so "
                        + "packaging cannot pick the generated files up");

        emitter.accept(BUNDLE, "bundle".getBytes(StandardCharsets.UTF_8));
        emitter.accept(TOKEN, "token".getBytes(StandardCharsets.UTF_8));

        assertIterableEquals(List.of(BUNDLE, TOKEN), emittedNames());
    }

    @Test
    void emit_generatedDirectoryPackaged_emitsOnlyTokenFile() {
        Path outputDir = tempDir.resolve("classes");

        GeneratedResourceEmitter emitter = GeneratedResourceEmitter
                .of(List.of(outputDir), outputDir, producer);

        assertTrue(emitter.isPackagedFromOutputDirectory(),
                "The output directory is itself the packaged root");

        emitter.accept(BUNDLE, "bundle".getBytes(StandardCharsets.UTF_8));
        emitter.accept(TOKEN, "token".getBytes(StandardCharsets.UTF_8));

        assertIterableEquals(List.of(TOKEN), emittedNames(),
                "The token file is deleted from the output directory after the "
                        + "build, so it has to be emitted even when everything "
                        + "else is packaged from there");
    }

    @Test
    void emit_generatedDirectoryNestedInPackagedRoot_emitsOnlyTokenFile() {
        Path packagedRoot = tempDir.resolve("classes");
        Path outputDir = packagedRoot.resolve("META-INF").resolve("VAADIN");

        GeneratedResourceEmitter emitter = GeneratedResourceEmitter
                .of(List.of(packagedRoot), outputDir, producer);

        assertTrue(emitter.isPackagedFromOutputDirectory());

        emitter.accept(BUNDLE, "bundle".getBytes(StandardCharsets.UTF_8));

        assertIterableEquals(List.of(), emittedNames());
    }

    @Test
    void emit_severalPackagedRoots_matchesAnyOfThem() {
        // Gradle reports build/classes/java/main and build/resources/main
        Path classesRoot = tempDir.resolve("classes").resolve("java")
                .resolve("main");
        Path resourcesRoot = tempDir.resolve("resources").resolve("main");

        GeneratedResourceEmitter emitter = GeneratedResourceEmitter.of(
                List.of(classesRoot, resourcesRoot), resourcesRoot, producer);

        assertTrue(emitter.isPackagedFromOutputDirectory(),
                "The output directory matches the second root");

        emitter.accept(BUNDLE, "bundle".getBytes(StandardCharsets.UTF_8));

        assertIterableEquals(List.of(), emittedNames());
    }

    @Test
    void emit_noPackagedRoots_emitsEverything() {
        GeneratedResourceEmitter emitter = GeneratedResourceEmitter
                .of(List.of(), tempDir.resolve("classes"), producer);

        assertEquals(false, emitter.isPackagedFromOutputDirectory());

        emitter.accept(BUNDLE, "bundle".getBytes(StandardCharsets.UTF_8));

        assertIterableEquals(List.of(BUNDLE), emittedNames());
    }

    @Test
    void emit_packagedRootIsSealedArchive_emitsEverything() throws IOException {
        Path outputDir = tempDir.resolve("classes");
        Path archive = tempDir.resolve("application.jar");
        try (ZipOutputStream zip = new ZipOutputStream(
                Files.newOutputStream(archive))) {
            zip.putNextEntry(new ZipEntry("README"));
            zip.closeEntry();
        }

        try (FileSystem archiveFs = FileSystems.newFileSystem(
                URI.create("jar:" + archive.toUri()), Map.of())) {
            GeneratedResourceEmitter emitter = GeneratedResourceEmitter
                    .of(archiveFs.getRootDirectories(), outputDir, producer);

            assertEquals(false, emitter.isPackagedFromOutputDirectory(),
                    "A root mounted from a JAR was sealed before this build "
                            + "ran, so nothing written to the output directory "
                            + "reaches the application on its own");

            emitter.accept(BUNDLE, "bundle".getBytes(StandardCharsets.UTF_8));

            assertIterableEquals(List.of(BUNDLE), emittedNames());
        }
    }

    @Test
    void emit_pathEndingLikeTheTokenFile_isNotMistakenForIt() {
        Path outputDir = tempDir.resolve("classes");
        GeneratedResourceEmitter emitter = GeneratedResourceEmitter
                .of(List.of(outputDir), outputDir, producer);

        emitter.accept("META-INF/VAADIN/config/not-flow-build-info.json",
                "nope".getBytes(StandardCharsets.UTF_8));
        emitter.accept("META-INF/VAADIN/backup-config/flow-build-info.json",
                "nope".getBytes(StandardCharsets.UTF_8));

        assertIterableEquals(List.of(), emittedNames());
    }

    private List<String> emittedNames() {
        return emitted.stream().map(GeneratedResourceBuildItem::getName)
                .toList();
    }
}
