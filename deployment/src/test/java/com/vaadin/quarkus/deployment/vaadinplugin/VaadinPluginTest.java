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
import java.nio.file.Files;
import java.nio.file.Path;

import io.quarkus.bootstrap.model.ApplicationModel;
import io.quarkus.bootstrap.workspace.WorkspaceModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.vaadin.flow.internal.FrontendUtils;
import com.vaadin.flow.server.Constants;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VaadinPluginTest {

    @TempDir
    Path projectDir;

    private VaadinPlugin plugin;
    private File tokenFile;

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
        tokenFile = new File(
                new File(buildDir,
                        "classes/" + Constants.VAADIN_SERVLET_RESOURCES),
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
}
