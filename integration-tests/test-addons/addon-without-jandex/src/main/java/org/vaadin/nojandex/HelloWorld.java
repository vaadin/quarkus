package org.vaadin.nojandex;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.littemplate.LitTemplate;

@Tag("hello-world")
@NpmPackage(value = "@axa-ch/input-text", version = "4.3.11")
@JsModule("./src/hello-world.ts")
public class HelloWorld extends LitTemplate {

    /**
     * Creates the hello world template.
     */
    public HelloWorld() {
    }
}