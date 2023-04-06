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
import javax.inject.Inject;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("injecter")
public class UIScopeInjecterView extends Div {

    @Inject
    UIScopedBean bean;

    @Inject
    UIScopedLabel label;

    @PostConstruct
    private void init() {
        Div div = new Div();
        div.setId(UIContextRootView.UI_SCOPED_BEAN_ID);
        div.setText(bean.getId());
        add(div);

        add(label);
    }
}
