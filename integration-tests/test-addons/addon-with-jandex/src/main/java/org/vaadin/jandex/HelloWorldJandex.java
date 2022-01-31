package org.vaadin.jandex;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.littemplate.LitTemplate;

@Tag("hello-world-jandex")
@NpmPackage(value = "@axa-ch/input-text", version = "4.3.11")
@JsModule("./src/hello-world-jandex.ts")
public class HelloWorldJandex extends LitTemplate {

    /**
     * Creates the hello world template.
     */
    public HelloWorldJandex() {
    }
}