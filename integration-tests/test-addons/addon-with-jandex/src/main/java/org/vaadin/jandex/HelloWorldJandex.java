/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package org.vaadin.jandex;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

@Tag("hello-world-jandex")
@NpmPackage(value = "@axa-ch/input-text", version = "4.3.11")
@JsModule("./src/hello-world-jandex.ts")
public class HelloWorldJandex extends Component {

    /**
     * Creates the hello world template.
     */
    public HelloWorldJandex() {
    }
}
