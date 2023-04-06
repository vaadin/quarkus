/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.quarkus.it.uicontext;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import java.util.UUID;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.quarkus.it.Counter;
import com.vaadin.quarkus.annotation.NormalUIScoped;

@NormalUIScoped
public class UIScopedBean {

    private final String id = UUID.randomUUID().toString();

    public static final String CREATE_COUNTER_KEY = UIScopedBean.class.getName()
            + "Create";
    public static final String DESTROY_COUNTER_KEY = UIScopedBean.class
            .getName() + "Destroy";

    private int uiId;

    @Inject
    private Counter counter;

    public String getId() {
        return id;
    }

    @PostConstruct
    void postConstruct() {
        uiId = UI.getCurrent().getUIId();
        counter.increment(CREATE_COUNTER_KEY + uiId);
    }

    @PreDestroy
    void preDestroy() {
        counter.increment(DESTROY_COUNTER_KEY + uiId);
    }
}
