/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.quarkus.it.routecontext;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.quarkus.annotation.RouteScopeOwner;

@PreserveOnRefresh
@Route(value = "preserve-on-refresh", layout = MainLayout.class)
public class PreserveOnRefreshView extends Div {

    @Inject
    @RouteScopeOwner(PreserveOnRefreshView.class)
    Instance<PreserveOnRefreshBean> injection;

    public PreserveOnRefreshView() {
        setId("preserve-on-refresh");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        setText(injection.get().getData());
    }
}
