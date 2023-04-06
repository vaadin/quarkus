/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.quarkus.it;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import java.io.Serializable;

@Route("")
public class SmokeTestView extends Div {
    @Inject
    HelloProvider helloProvider;

    public SmokeTestView() {
        add(new NativeButton("Click me",
                event -> add(new Label(getLabelText()))));

        // Use custom CSS classes to apply styling. This is defined in
        // shared-styles.css.
        addClassName("centered-content");
    }

    private String getLabelText() {
        if (helloProvider != null) {
            return helloProvider.getHello();
        } else {
            return "no CDI enabled";
        }
    }

    @PostConstruct
    private void init() {
        helloProvider.setHello("hello quarkus CDI");
    }

    @SessionScoped
    public static class HelloProvider implements Serializable {
        private String hello;

        public String getHello() {
            return hello;
        }

        public void setHello(String hello) {
            this.hello = hello;
        }
    }

}
