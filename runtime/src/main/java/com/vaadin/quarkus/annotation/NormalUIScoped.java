/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.quarkus.annotation;

import jakarta.enterprise.context.NormalScope;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

/**
 * The lifecycle of a UIScoped component is bound to a browser tab.
 * <p>
 * Injecting with this annotation will create a proxy for the contextual
 * instance rather than provide the contextual instance itself.
 * <p>
 * When using proxies, be aware that it's not guaranteed that the hashcode or
 * equals will match when comparing a proxy to it's underlying instance. It's
 * imperative to be aware of this when (for example) adding proxies to a
 * Collection.
 * <p>
 * You cannot use this scope with Vaadin Components. Proxy Components do not
 * work correctly within the Vaadin framework, so as a precaution the Vaadin CDI
 * plugin will not deploy if any such beans are discovered.
 * <p>
 * The sister annotation to this is the {@link UIScoped}. Both annotations
 * reference the same underlying scope, so it is possible to get both a proxy
 * and a direct reference to the same object by using different annotations.
 */
@NormalScope
@Inherited
@Target({ ANNOTATION_TYPE, TYPE, FIELD, METHOD, CONSTRUCTOR })
@Retention(RetentionPolicy.RUNTIME)
public @interface NormalUIScoped {
}
