/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.quarkus.it.routecontext;

import javax.annotation.PostConstruct;

import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.quarkus.annotation.RouteScopeOwner;
import com.vaadin.quarkus.annotation.RouteScoped;

@RouteScoped
@RouteScopeOwner(ErrorParentView.class)
@Route("error-layout")
public class ErrorParentView extends AbstractCountedView
        implements RouterLayout {

    public static final String ROOT = "root";

    @PostConstruct
    private void init() {
        add(new RouterLink(ROOT, RootView.class));
    }

}
