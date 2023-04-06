/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.quarkus.it.service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route("service")
public class ServiceView extends Div {

    public static final String EXPIRE = "EXPIRE";
    public static final String ACTION = "ACTION";
    public static final String FAIL = "FAIL";

    @Inject
    ServiceBean bean;

    @PostConstruct
    private void init() {
        NativeButton expireBtn = new NativeButton("expire",
                event -> VaadinSession.getCurrent().getSession().invalidate());
        expireBtn.setId(EXPIRE);

        NativeButton actionButton = new NativeButton("an action", event -> {
        });
        actionButton.setId(ACTION);

        NativeButton failBtn = new NativeButton("fail", event -> {
            if (true) {
                throw new NullPointerException();
            }
        });
        failBtn.setId(FAIL);

        add(expireBtn, actionButton, failBtn);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        Div div = new Div();
        div.setId("service-id");
        div.setText(bean.getId());
        add(div);
    }
}
