/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.quarkus.context;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This Storage holds all information needed for storing Contextual Instances in
 * a Context.
 *
 * It also addresses Serialisation in case of passivating scopes.
 */
public class ContextualStorage implements Serializable {

    private final Map<Object, ContextualInstanceInfo<?>> contextualInstances;

    private final boolean concurrent;

    /**
     * Creates a new instance of storage.
     * 
     * @param concurrent
     *            whether the ContextualStorage might get accessed concurrently
     *            by different threads
     */
    public ContextualStorage(boolean concurrent) {
        this.concurrent = concurrent;
        if (concurrent) {
            contextualInstances = new ConcurrentHashMap<Object, ContextualInstanceInfo<?>>();
        } else {
            contextualInstances = new HashMap<Object, ContextualInstanceInfo<?>>();
        }
    }

    /**
     * @return the underlying storage map.
     */
    public Map<Object, ContextualInstanceInfo<?>> getStorage() {
        return contextualInstances;
    }

    /**
     * @return whether the ContextualStorage might get accessed concurrently by
     *         different threads.
     */
    public boolean isConcurrent() {
        return concurrent;
    }

    /**
     *
     * @param bean
     *            the contextual type
     * @param creationalContext
     *            a context
     * @param <T>
     *            contextual instance type
     * @return a created contextual instance
     */
    public <T> T createContextualInstance(Contextual<T> bean,
            CreationalContext<T> creationalContext) {
        Object beanKey = getBeanKey(bean);
        if (isConcurrent()) {
            // locked approach
            ContextualInstanceInfo<T> instanceInfo = new ContextualInstanceInfo<T>();

            ConcurrentMap<Object, ContextualInstanceInfo<?>> concurrentMap = (ConcurrentHashMap<Object, ContextualInstanceInfo<?>>) contextualInstances;

            ContextualInstanceInfo<T> oldInstanceInfo = (ContextualInstanceInfo<T>) concurrentMap
                    .putIfAbsent(beanKey, instanceInfo);

            if (oldInstanceInfo != null) {
                instanceInfo = oldInstanceInfo;
            }
            synchronized (instanceInfo) {
                T instance = instanceInfo.getContextualInstance();
                if (instance == null) {
                    instance = bean.create(creationalContext);
                    instanceInfo.setContextualInstance(instance);
                    instanceInfo.setCreationalContext(creationalContext);
                }

                return instance;
            }

        } else {
            // simply create the contextual instance
            ContextualInstanceInfo<T> instanceInfo = new ContextualInstanceInfo<T>();
            instanceInfo.setCreationalContext(creationalContext);
            instanceInfo.setContextualInstance(bean.create(creationalContext));

            contextualInstances.put(beanKey, instanceInfo);

            return instanceInfo.getContextualInstance();
        }
    }

    /**
     * If the context is a passivating scope then we return the passivationId of
     * the Bean. Otherwise we use the Bean directly.
     * 
     * @param <T>
     *            bean type
     * @param bean
     *            the contextual type
     * 
     * @return the key to use in the context map
     */
    public <T> Object getBeanKey(Contextual<T> bean) {
        return bean;
    }

    /**
     * Restores the Bean from its beanKey.
     * 
     * @see #getBeanKey(javax.enterprise.context.spi.Contextual)
     * 
     * @param beanKey
     *            a bean key
     * 
     * @return the contextual type
     */
    public Contextual<?> getBean(Object beanKey) {
        return (Contextual<?>) beanKey;
    }
}
