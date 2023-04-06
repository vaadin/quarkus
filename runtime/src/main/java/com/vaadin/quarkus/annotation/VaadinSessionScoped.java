/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.quarkus.annotation;

import javax.enterprise.context.NormalScope;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.vaadin.flow.server.VaadinSession;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The lifecycle of a VaadinSessionScoped bean is bound to a
 * {@link VaadinSession}.
 * <p>
 * Injecting with this annotation will create a proxy for the contextual
 * instance rather than provide the contextual instance itself.
 * <p>
 * Contextual instances stored in {@link VaadinSession}, so indirectly stored in
 * HTTP session. {@link javax.annotation.PreDestroy} called after
 * {@link com.vaadin.flow.server.SessionDestroyEvent} fired.
 *
 * @since 1.0
 */
@NormalScope
@Inherited
@Target({ ANNOTATION_TYPE, TYPE, FIELD, METHOD, CONSTRUCTOR })
@Retention(RUNTIME)
public @interface VaadinSessionScoped {
}
