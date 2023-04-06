/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.quarkus;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.vaadin.quarkus.annotation.VaadinServiceEnabled;

/**
 * Utility class for Quarkus CDI lookup, and instantiation.
 * <p>
 * Dependent beans are instantiated without any warning, but do not get
 * destroyed properly. {@link javax.annotation.PreDestroy} won't run.
 *
 * @param <T>
 *            Bean Type
 */
class BeanLookup<T> {

    static final Annotation SERVICE = new ServiceLiteral();

    private final BeanManager beanManager;
    private final Class<T> type;
    private final Annotation[] qualifiers;
    private UnsatisfiedHandler unsatisfiedHandler = () -> {
    };
    private Consumer<AmbiguousResolutionException> ambiguousHandler = e -> {
        throw e;
    };

    private static final Annotation[] ANY = new Annotation[] {
            new AnyLiteral() };

    private static class ServiceLiteral
            extends AnnotationLiteral<VaadinServiceEnabled>
            implements VaadinServiceEnabled {
    }

    @FunctionalInterface
    public interface UnsatisfiedHandler {
        void handle();
    }

    BeanLookup(final BeanManager beanManager, final Class<T> type,
            final Annotation... qualifiers) {
        this.beanManager = beanManager;
        this.type = type;
        if (qualifiers.length > 0) {
            this.qualifiers = qualifiers;
        } else {
            this.qualifiers = ANY;
        }
    }

    BeanLookup<T> setUnsatisfiedHandler(
            final UnsatisfiedHandler unsatisfiedHandler) {
        this.unsatisfiedHandler = unsatisfiedHandler;
        return this;
    }

    BeanLookup<T> setAmbiguousHandler(
            final Consumer<AmbiguousResolutionException> ambiguousHandler) {
        this.ambiguousHandler = ambiguousHandler;
        return this;
    }

    T lookupOrElseGet(final Supplier<T> fallback) {
        final Set<Bean<?>> beans = this.beanManager.getBeans(this.type,
                this.qualifiers);
        if (beans == null || beans.isEmpty()) {
            this.unsatisfiedHandler.handle();
            return fallback.get();
        }
        final Bean<?> bean;
        try {
            bean = this.beanManager.resolve(beans);
        } catch (final AmbiguousResolutionException e) {
            this.ambiguousHandler.accept(e);
            return fallback.get();
        }
        final CreationalContext<?> ctx = this.beanManager
                .createCreationalContext(bean);
        // noinspection unchecked
        return (T) this.beanManager.getReference(bean, this.type, ctx);
    }

    T lookup() {
        return lookupOrElseGet(() -> null);
    }
}
