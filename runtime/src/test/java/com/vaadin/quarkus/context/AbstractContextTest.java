/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.quarkus.context;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Basic tests for all custom abstract contexts.
 * 
 * @param <C>
 *            a context type
 */
public abstract class AbstractContextTest<C extends AbstractContext>
        extends InjectableContextTest<C> {

    @Test
    public void destroyAllActive_beanExistsInContext_beanDestroyed() {
        createContext().activate();

        TestBean referenceA = getContext().get(contextual, creationalContext);
        referenceA.setState("hello");

        getContext().destroyAllActive();
        assertEquals(1, getDestroyedBeans().size());
    }

    @Test
    public void destroyAllActive_severalContexts_beanDestroyed() {
        UnderTestContext contextUnderTestA = createContext();
        contextUnderTestA.activate();
        TestBean referenceA = getContext().get(contextual, creationalContext);

        ContextualStorage storageA = getContext()
                .getContextualStorage(contextual, false);

        referenceA.setState("hello");

        final UnderTestContext contextUnderTestB = createContext();
        contextUnderTestB.activate();
        TestBean referenceB = getContext().get(contextual, creationalContext);

        referenceB.setState("foo");

        ContextualStorage storageB = getContext()
                .getContextualStorage(contextual, false);

        assertEquals(2, getCreatedBeansCount());

        AbstractContext.destroyAllActive(storageA);
        assertEquals(1, getDestroyedBeans().size());

        AbstractContext.destroyAllActive(storageB);
        assertEquals(2, getDestroyedBeans().size());
    }

}
