/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.quarkus.context;

import javax.annotation.PreDestroy;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for manage and store ContextualStorages.
 *
 * This class is responsible for - creating, and providing the ContextualStorage
 * for a context key - destroying ContextualStorages
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
abstract class AbstractContextualStorageManager<K> implements Serializable {
    private final boolean concurrent;
    private final Map<K, ContextualStorage> storageMap;

    protected AbstractContextualStorageManager(boolean concurrent) {
        if (concurrent) {
            this.storageMap = new ConcurrentHashMap<>();
        } else {
            this.storageMap = new HashMap<>();
        }
        this.concurrent = concurrent;
    }

    /**
     * Gets the {@link ContextualStorage} associated with the given context
     * {@code key}, possibly creating a new instance, if requested and not
     * already existing.
     * 
     * @param key
     *            context key
     * @param createIfNotExist
     *            whether a ContextualStorage shall get created if it doesn't
     *            yet exist.
     * @return a {@link ContextualStorage} instance.
     */
    protected ContextualStorage getContextualStorage(K key,
            boolean createIfNotExist) {
        if (createIfNotExist) {
            return storageMap.computeIfAbsent(key, this::newContextualStorage);
        } else {
            return storageMap.get(key);
        }
    }

    /**
     * Changes the context key for a contextual storage.
     * 
     * @param from
     *            the contextual storage context key
     * @param to
     *            the new context for the contextual storage
     */
    protected void relocate(K from, K to) {
        ContextualStorage storage = storageMap.remove(from);
        if (storage != null) {
            storageMap.put(to, storage);
        }
    }

    /**
     * Creates a new {@link ContextualStorage} for the given context key.
     * 
     * @param key
     *            the context key
     * @return a new {@link ContextualStorage} instance.
     */
    protected ContextualStorage newContextualStorage(K key) {
        return new ContextualStorage(concurrent);
    }

    /**
     * Destroys all existing contextual storages.
     */
    @PreDestroy
    protected void destroyAll() {
        Collection<ContextualStorage> storages = storageMap.values();
        for (ContextualStorage storage : storages) {
            AbstractContext.destroyAllActive(storage);
        }
        storageMap.clear();
    }

    /**
     * Destroys the contextual storage associated with the given context key.
     * 
     * @param key
     *            the context key
     */
    protected void destroy(K key) {
        ContextualStorage storage = storageMap.remove(key);
        if (storage != null) {
            AbstractContext.destroyAllActive(storage);
        }
    }

    /**
     * Gets context keys of all registered contextual storages.
     * 
     * @return immutable set of context keys.
     */
    protected Set<K> getKeySet() {
        return Collections.unmodifiableSet(storageMap.keySet());
    }

}
