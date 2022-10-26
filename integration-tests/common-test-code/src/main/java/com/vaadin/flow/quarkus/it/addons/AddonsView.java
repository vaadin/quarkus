package com.vaadin.flow.quarkus.it.addons;

import jakarta.annotation.PostConstruct;

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
