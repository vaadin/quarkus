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
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import java.util.UUID;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.quarkus.annotation.NormalRouteScoped;
import com.vaadin.quarkus.annotation.RouteScopeOwner;
import com.vaadin.quarkus.annotation.RouteScoped;

@RouteScoped
@RouteScopeOwner(ErrorParentView.class)
@ParentLayout(ErrorParentView.class)
public class ErrorHandlerView extends AbstractCountedView
        implements HasErrorParameter<CustomException> {

    public static final String PARENT = "parent";

    @Inject
    @RouteScopeOwner(ErrorHandlerView.class)
    private Instance<ErrorBean1> bean1;

    @Inject
    @RouteScopeOwner(ErrorHandlerView.class)
    private Instance<ErrorBean2> bean2;

    private AbstractCountedBean current;

    @NormalRouteScoped
    @RouteScopeOwner(ErrorHandlerView.class)
    public static class ErrorBean1 extends AbstractCountedBean {

        @PostConstruct
        void init() {
            setData(UUID.randomUUID().toString());
        }
    }

    @NormalRouteScoped
    @RouteScopeOwner(ErrorHandlerView.class)
    public static class ErrorBean2 extends AbstractCountedBean {

        @PostConstruct
        void init() {
            setData(UUID.randomUUID().toString());
        }
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event,
            ErrorParameter<CustomException> parameter) {
        add(new RouterLink(PARENT, ErrorParentView.class));

        Div div = new Div();
        div.setId("bean-data");

        NativeButton button = new NativeButton("switch content", ev -> {
            if (current instanceof ErrorBean1) {
                current = bean2.get();
            } else {
                current = bean1.get();
            }
            div.setText(current.getData());
        });
        button.setId("switch-content");
        add(button);
        current = bean1.get();
        div.setText(current.getData());
        add(div);

        return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }

}
