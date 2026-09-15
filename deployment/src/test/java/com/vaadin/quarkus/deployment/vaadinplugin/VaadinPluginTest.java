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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import io.quarkus.bootstrap.model.ApplicationModel;
import io.quarkus.bootstrap.workspace.WorkspaceModule;
import io.quarkus.builder.BuildException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.vaadin.flow.internal.FrontendUtils;
import com.vaadin.flow.server.Constants;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VaadinPluginTest {

    @TempDir
    Path projectDir;

    private VaadinPlugin plugin;
    private File tokenFile;
    private Path generatedResourcesDir;

    @BeforeEach
    void setUp() throws Exception {
        File buildDir = projectDir.resolve("target").toFile();

        WorkspaceModule module = mock(WorkspaceModule.class);
        when(module.getModuleDir()).thenReturn(projectDir.toFile());
        when(module.getBuildDir()).thenReturn(buildDir);
        when(module.hasMainSources()).thenReturn(false);

        ApplicationModel model = mock(ApplicationModel.class);
        when(model.getApplicationModule()).thenReturn(module);

        VaadinBuildTimeConfig config = mock(VaadinBuildTimeConfig.class);
        when(config.generatedResourceOutputDirectory())
                .thenReturn(new File(Constants.VAADIN_SERVLET_RESOURCES));
        // Prevents the clean frontend files task from being created, it is not
        // needed to exercise the token file removal.
        when(config.cleanFrontendFiles()).thenReturn(false);

        plugin = VaadinPlugin.of(config, model, buildDir.toPath());
        generatedResourcesDir = buildDir.toPath()
                .resolve("classes/" + Constants.VAADIN_SERVLET_RESOURCES);
        tokenFile = new File(generatedResourcesDir.toFile(),
                FrontendUtils.TOKEN_FILE);
    }

    @Test
    void removeTokenFile_tokenFilePresent_deleted() throws Exception {
        Files.createDirectories(tokenFile.getParentFile().toPath());
        Files.writeString(tokenFile.toPath(), "{ \"productionMode\": true }");
        assertTrue(tokenFile.exists(), "Test setup should create token file");

        plugin.removeTokenFile();

        assertFalse(tokenFile.exists(),
                "Token file should have been deleted, otherwise it is packaged "
                        + "twice and a later dev mode run starts in production mode");
    }

    @Test
    void removeTokenFile_tokenFileMissing_doesNotFail() {
        assertFalse(tokenFile.exists(),
                "Test setup should not create token file");

        assertDoesNotThrow(() -> plugin.removeTokenFile());
    }

    @Test
    void removeTokenFile_tokenFileCannotBeDeleted_doesNotFailBuild()
            throws Exception {
        Files.createDirectories(tokenFile.getParentFile().toPath());
        Files.writeString(tokenFile.toPath(), "{ \"productionMode\": true }");
        File configDir = tokenFile.getParentFile();
        // A file to probe the deletion with, so that the probe does not consume
        // the token file the assertion needs
        File probe = new File(configDir, "probe.txt");
        Files.writeString(probe.toPath(), "probe");

        assumeTrue(configDir.setWritable(false),
                "A directory cannot be made read only on this file system");
        try {
            assumeFalse(probe.delete(),
                    "Test runs as a user that can delete from a read only "
                            + "directory, likely root");

            assertDoesNotThrow(() -> plugin.removeTokenFile(),
                    "A token file that cannot be deleted must not fail a build "
                            + "whose frontend build succeeded: the token file "
                            + "has already been added to the application, so "
                            + "the copy left behind is a duplicate and not a "
                            + "missing file");
            assertTrue(tokenFile.exists(),
                    "Test setup should have prevented the deletion");
        } finally {
            configDir.setWritable(true);
        }
    }

    @Test
    void emitGeneratedFiles_generatedResources_emittedRelativeToOutputDirectory()
            throws Exception {
        writeGeneratedFile(FrontendUtils.TOKEN_FILE,
                "{ \"productionMode\": true }");
        writeGeneratedFile("build/indexhtml-1234.js", "console.log('hi');");

        Map<String, byte[]> emitted = new LinkedHashMap<>();
        plugin.emitGeneratedFiles(emitted::put);

        assertEquals(Set.of(
                Constants.VAADIN_SERVLET_RESOURCES + FrontendUtils.TOKEN_FILE,
                Constants.VAADIN_SERVLET_RESOURCES + "build/indexhtml-1234.js"),
                emitted.keySet(),
                "Generated files should be emitted with their path relative to "
                        + "the build output directory");
        assertEquals("{ \"productionMode\": true }",
                new String(
                        emitted.get(Constants.VAADIN_SERVLET_RESOURCES
                                + FrontendUtils.TOKEN_FILE),
                        StandardCharsets.UTF_8),
                "Emitted content should be the content of the file on disk");
    }

    @Test
    void emitGeneratedFiles_unreadableTokenFile_failsBuild() throws Exception {
        assertUnreadableFileFailsBuild(FrontendUtils.TOKEN_FILE);
    }

    @Test
    void emitGeneratedFiles_unreadableBundleFile_failsBuild() throws Exception {
        assertUnreadableFileFailsBuild("build/indexhtml-1234.js");
    }

    @Test
    void emitGeneratedFiles_noGeneratedResources_emitsNothing() {
        assertFalse(Files.exists(generatedResourcesDir),
                "Test setup should not create the generated resources directory");

        Map<String, byte[]> emitted = new LinkedHashMap<>();
        assertDoesNotThrow(() -> plugin.emitGeneratedFiles(emitted::put));

        assertTrue(emitted.isEmpty(),
                "Nothing should be emitted when the Vaadin build produced no files");
    }

    /**
     * Asserts that the build fails when the given generated file cannot be
     * read, whichever file it is: skipping any of them packages an application
     * that is missing it, and skipping the token file in particular leaves the
     * application with no {@literal flow-build-info.json} at all, because
     * {@link VaadinPlugin#removeTokenFile()} deletes the copy in the build
     * output directory right after.
     * <p>
     * The assertion needs a file the build cannot read, which the test is not
     * always able to produce. It is skipped when the file cannot be made
     * unreadable, as on Windows, where {@link File#setReadable(boolean)}
     * reports a failure, and when the test user can read any file regardless of
     * its permissions, typically root.
     *
     * @param relativePath
     *            path of the file to make unreadable, relative to the generated
     *            resources directory.
     */
    private void assertUnreadableFileFailsBuild(String relativePath)
            throws Exception {
        // Writes both files, so that the build fails because of the unreadable
        // one and not because it is the only file it found.
        writeGeneratedFile(FrontendUtils.TOKEN_FILE,
                "{ \"productionMode\": true }");
        writeGeneratedFile("build/indexhtml-1234.js", "console.log('hi');");

        File unreadableFile = generatedResourcesDir.resolve(relativePath)
                .toFile();
        assumeTrue(unreadableFile.setReadable(false),
                "A file cannot be made unreadable on this file system");
        assumeFalse(unreadableFile.canRead(),
                "Test runs as a user that can read any file, likely root");

        BiConsumer<String, byte[]> emitter = (path, content) -> {
        };
        try {
            BuildException exception = assertThrows(BuildException.class,
                    () -> plugin.emitGeneratedFiles(emitter),
                    "A file produced by the Vaadin build that cannot be read must "
                            + "fail the build, otherwise the application is packaged "
                            + "without it and the build reports success");
            assertTrue(
                    exception.getMessage()
                            .contains(unreadableFile.getAbsolutePath()),
                    "Failure should name the file that could not be read, was: "
                            + exception.getMessage());
        } finally {
            unreadableFile.setReadable(true);
        }
    }

    private void writeGeneratedFile(String relativePath, String content)
            throws Exception {
        Path file = generatedResourcesDir.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }
}
