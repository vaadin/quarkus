/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.quarkus.it.routecontext;

import java.util.UUID;

import com.vaadin.quarkus.annotation.NormalRouteScoped;

@NormalRouteScoped
public class BeanNoOwner extends AbstractCountedBean {

    public BeanNoOwner() {
        setData(UUID.randomUUID().toString());
    }

}
