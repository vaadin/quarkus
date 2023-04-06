/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.quarkus;

import javax.enterprise.inject.Any;
import javax.enterprise.util.AnnotationLiteral;

/**
 * Literal for the {@link javax.enterprise.inject.Any} annotation.
 * 
 * @author Vaadin Ltd
 * @since 1.0
 *
 */
public class AnyLiteral extends AnnotationLiteral<Any> implements Any {
}
