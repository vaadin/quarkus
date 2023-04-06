/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.quarkus.it.addons;

import javax.annotation.PostConstruct;

import org.vaadin.jandex.HelloWorldJandex;
import org.vaadin.nojandex.HelloWorld;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("addons")
public class AddonsView extends Div {

    @PostConstruct
    private void init() {
        add(new HelloWorld(), new HelloWorldJandex());
    }
}
