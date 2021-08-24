/*
 * Copyright 2000-2018 Vaadin Ltd.
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

package com.vaadin.quarkus.context;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.CreationalContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.quarkus.arc.InjectableBean;
import io.quarkus.arc.InjectableContext.ContextState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.vaadin.flow.internal.ReflectTools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

public abstract class AbstractContextTest<T extends TestBean, C extends AbstractContext> {

    private List<UnderTestContext> contexts;

    @SuppressWarnings("unchecked")
    private CreationalContext<T> creationalContext = Mockito
            .mock(CreationalContext.class);
    private InjectableBean<T> contextual;

    private C context;

    private Set<T> destroyedBeans = new HashSet<>();

    private int createdBeans;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUp() {
        createdBeans = 0;
        contexts = new ArrayList<>();
        destroyedBeans.clear();

        contextual = Mockito.mock(InjectableBean.class);
        Mockito.doAnswer(invocation -> createBean()).when(contextual)
                .create(creationalContext);

        Mockito.doAnswer(invocation -> {
            destroyedBeans.add(invocation.getArgument(0));
            return null;
        }).when(contextual).destroy(Mockito.any(),
                Mockito.eq(creationalContext));

        context = createQuarkusContext();
    }

    @AfterEach
    public void tearDown() {
        newContextUnderTest().tearDownAll();
        contexts = null;
    }

    @Test
    public void get_contextNotActive_ExceptionThrown() {
        Assertions.assertThrows(ContextNotActiveException.class, () -> {
            C context = createQuarkusContext();
            context.get(contextual);
        });
    }

    @Test
    public void get_sameContextActive_beanCreatedOnce() {
        createContext().activate();

        T referenceA = context.get(contextual, creationalContext);
        referenceA.setState("hello");
        assertEquals("hello", referenceA.getState());
        T referenceB = context.get(contextual, creationalContext);
        assertEquals("hello", referenceB.getState());
        assertEquals(0, destroyedBeans.size());

        referenceB = context.get(contextual);
        assertSame(referenceA, referenceB);

        assertEquals(1, createdBeans);
    }

    @Test
    public void get_newContextActive_newBeanCreated() {
        createContext().activate();

        T referenceA = context.get(contextual, creationalContext);
        referenceA.setState("hello");

        createContext().activate();

        T referenceB = context.get(contextual, creationalContext);
        assertEquals("", referenceB.getState());
        assertEquals(0, destroyedBeans.size());
        assertNotSame(referenceA, referenceB);

        referenceB = context.get(contextual);
        assertNotSame(referenceA, referenceB);

        assertEquals(2, createdBeans);
    }

    @Test
    public void destroyContext_beanExistsInContext_beanDestroyed() {
        UnderTestContext contextUnderTestA = createContext();
        contextUnderTestA.activate();
        T referenceA = context.get(contextual, creationalContext);

        referenceA.setState("hello");

        final UnderTestContext contextUnderTestB = createContext();
        contextUnderTestB.activate();
        T referenceB = context.get(contextual, creationalContext);

        referenceB.setState("foo");

        assertEquals(2, createdBeans);

        contextUnderTestA.destroy();
        assertEquals(1, destroyedBeans.size());

        contextUnderTestB.destroy();
        assertEquals(2, destroyedBeans.size());
    }

    @Test
    public void destroy_beanExistsInContext_beanDestroyed() {
        createContext().activate();

        T referenceA = context.get(contextual, creationalContext);
        referenceA.setState("hello");

        context.destroy(contextual);
        assertEquals(1, destroyedBeans.size());
    }

    @Test
    public void destroyQuarkusContext_beanExistsInContext_beanDestroyed() {
        createContext().activate();

        T referenceA = context.get(contextual, creationalContext);
        referenceA.setState("hello");

        context.destroy();
        assertEquals(1, destroyedBeans.size());
    }

    @Test
    public void destroyAllActive_beanExistsInContext_beanDestroyed() {
        createContext().activate();

        T referenceA = context.get(contextual, creationalContext);
        referenceA.setState("hello");

        context.destroyAllActive();
        assertEquals(1, destroyedBeans.size());
    }

    @Test
    public void destroyAllActive_severalContexts_beanDestroyed() {
        UnderTestContext contextUnderTestA = createContext();
        contextUnderTestA.activate();
        T referenceA = context.get(contextual, creationalContext);

        ContextualStorage storageA = context.getContextualStorage(contextual,
                false);

        referenceA.setState("hello");

        final UnderTestContext contextUnderTestB = createContext();
        contextUnderTestB.activate();
        T referenceB = context.get(contextual, creationalContext);

        referenceB.setState("foo");

        ContextualStorage storageB = context.getContextualStorage(contextual,
                false);

        assertEquals(2, createdBeans);

        AbstractContext.destroyAllActive(storageA);
        assertEquals(1, destroyedBeans.size());

        AbstractContext.destroyAllActive(storageB);
        assertEquals(2, destroyedBeans.size());
    }

    @Test
    public void getState_beanExistsInContext_contextualInstanceAndBeanAreReturned() {
        createContext().activate();

        T reference = context.get(contextual, creationalContext);
        reference.setState("hello");

        ContextState state = context.getState();

        Map<InjectableBean<?>, Object> instances = state
                .getContextualInstances();
        assertNotNull(instances);
        assertSame(reference, instances.get(contextual));
    }

    protected UnderTestContext createContext() {
        UnderTestContext underTestContext = newContextUnderTest();
        /*
         * UnderTestContext implementations set fields to Vaadin
         * CurrentInstance. Need to hold a hard reference to prevent possible
         * GC, because CurrentInstance works with weak reference.
         */
        contexts.add(underTestContext);
        return underTestContext;
    }

    protected abstract UnderTestContext newContextUnderTest();

    protected abstract boolean isNormalScoped();

    protected abstract Class<T> getBeanType();

    protected abstract Class<C> getContextType();

    private C createQuarkusContext() {
        return getContextType()
                .cast(ReflectTools.createInstance(getContextType()));
    }

    private T createBean() {
        createdBeans++;
        return getBeanType().cast(ReflectTools.createInstance(getBeanType()));
    }

}
