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

import java.nio.file.Path;
import java.util.Set;
import java.util.function.BiConsumer;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.builditem.GeneratedResourceBuildItem;

import com.vaadin.flow.internal.FrontendUtils;

/**
 * Registers the files produced by the Vaadin build with the Quarkus
 * application, skipping the ones packaging already picks up on its own.
 * <p>
 * Quarkus packages the application from the archive root. When that root is the
 * directory the Vaadin build writes into, packaging walks the directory at the
 * time the artifact is assembled and therefore already contains everything the
 * build produced; emitting those files again only adds a second copy to the
 * artifact. When the root is an archive that the build tool sealed before the
 * Quarkus augmentation started, packaging cannot see anything written
 * afterwards and emitting is the only way the files reach the application.
 * <p>
 * Files that the build removes from the output directory once it is done are
 * the exception: packaging cannot pick those up from the directory either, so
 * they are always emitted. {@link #ALWAYS_EMITTED} is the list of them.
 */
final class GeneratedResourceEmitter implements BiConsumer<String, byte[]> {

    /**
     * Resources that are emitted whichever way the application is packaged,
     * because the build deletes them from the output directory once the
     * frontend build is done.
     * <p>
     * Add an entry here when a generated file gains that treatment, otherwise
     * it silently stops being packaged. Paths are relative to the generated
     * resources directory.
     *
     * @see VaadinPlugin#removeTokenFile()
     */
    private static final Set<Path> ALWAYS_EMITTED = Set
            .of(Path.of(FrontendUtils.TOKEN_FILE));

    private final BuildProducer<GeneratedResourceBuildItem> producer;
    private final boolean packagedFromOutputDirectory;

    private GeneratedResourceEmitter(
            BuildProducer<GeneratedResourceBuildItem> producer,
            boolean packagedFromOutputDirectory) {
        this.producer = producer;
        this.packagedFromOutputDirectory = packagedFromOutputDirectory;
    }

    /**
     * Creates an emitter for the given application archive roots.
     *
     * @param packagedRootDirectories
     *            the directories Quarkus packages the application from, as
     *            reported by
     *            {@link io.quarkus.deployment.builditem.ArchiveRootBuildItem#getRootDirectories()}.
     * @param generatedResourcesDirectory
     *            the directory the Vaadin build writes its generated resources
     *            into.
     * @param producer
     *            the producer registering the files with the application.
     * @return an emitter that skips what packaging already covers.
     */
    static GeneratedResourceEmitter of(Iterable<Path> packagedRootDirectories,
            Path generatedResourcesDirectory,
            BuildProducer<GeneratedResourceBuildItem> producer) {
        return new GeneratedResourceEmitter(producer, isPackagedFromDirectory(
                packagedRootDirectories, generatedResourcesDirectory));
    }

    /**
     * Registers a file the Vaadin build produced, unless packaging already
     * picks it up from the output directory.
     *
     * @param path
     *            the name the file is added to the application under, its path
     *            relative to the build output directory, with {@literal /} as
     *            the separator whatever the platform, as
     *            {@link VaadinPlugin#emitGeneratedFiles(BiConsumer)} hands it
     *            over.
     * @param content
     *            the content of the file.
     */
    @Override
    public void accept(String path, byte[] content) {
        if (!packagedFromOutputDirectory || isAlwaysEmitted(path)) {
            producer.produce(new GeneratedResourceBuildItem(path, content));
        }
    }

    /**
     * Whether packaging copies the generated resources directory into the
     * application by itself.
     *
     * @return {@literal true} if the files written into the generated resources
     *         directory reach the application without being emitted.
     */
    boolean isPackagedFromOutputDirectory() {
        return packagedFromOutputDirectory;
    }

    private static boolean isPackagedFromDirectory(
            Iterable<Path> packagedRootDirectories,
            Path generatedResourcesDirectory) {
        for (Path root : packagedRootDirectories) {
            // Path.startsWith is false for paths of different file systems, so
            // a root mounted from a JAR never matches, which is exactly the
            // case where the archive was sealed before this build ran.
            if (generatedResourcesDirectory.startsWith(root)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAlwaysEmitted(String path) {
        // Compared as paths rather than as strings, because the question is
        // whether this is the file the build deletes from the output
        // directory, which is a question about the file system the build runs
        // on. Path answers it with that file system's own rules: name
        // elements match exactly on Linux, where config and Config are two
        // directories, and ignoring case on Windows, where they are one and
        // the walk reports whichever casing is on disk. Matching whole name
        // elements also keeps a file such as backup-config/flow-build-info.json
        // from being taken for the token file.
        // The separator in the path is '/' whatever the platform, which
        // Path.of reads as a separator on Windows as well as on Linux.
        Path resource = Path.of(path);
        return ALWAYS_EMITTED.stream().anyMatch(resource::endsWith);
    }
}
