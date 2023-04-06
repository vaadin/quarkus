/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.quarkus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;

import com.vaadin.flow.di.ResourceProvider;

/**
 * A {@link ResourceProvider} implementation that delegates resource loading to
 * current thread context ClassLoader.
 */
public class QuarkusResourceProvider implements ResourceProvider {

    private Map<String, CachedStreamData> cache = new ConcurrentHashMap<>();

    @Override
    public URL getApplicationResource(String path) {
        return Thread.currentThread().getContextClassLoader().getResource(path);
    }

    @Override
    public List<URL> getApplicationResources(String path) throws IOException {
        return Collections.list(Thread.currentThread().getContextClassLoader()
                .getResources(path));
    }

    @Override
    public URL getClientResource(String path) {
        return getApplicationResource(path);
    }

    @Override
    public InputStream getClientResourceAsStream(String path)
            throws IOException {
        // the client resource should be available in the classpath, so
        // its content is cached once. If an exception is thrown then
        // something is broken and it's also cached and will be rethrown on
        // every subsequent access
        CachedStreamData cached = cache.computeIfAbsent(path, key -> {
            URL url = getClientResource(key);
            try (InputStream stream = url.openStream()) {
                ByteArrayOutputStream tempBuffer = new ByteArrayOutputStream();
                IOUtils.copy(stream, tempBuffer);
                return new CachedStreamData(tempBuffer.toByteArray(), null);
            } catch (IOException e) {
                return new CachedStreamData(null, e);
            }
        });

        IOException exception = cached.exception;
        if (exception == null) {
            return new ByteArrayInputStream(cached.data);
        }
        throw exception;
    }

    private static class CachedStreamData {

        private final byte[] data;
        private final IOException exception;

        private CachedStreamData(byte[] data, IOException exception) {
            this.data = data;
            this.exception = exception;
        }
    }

}
