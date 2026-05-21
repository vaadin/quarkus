/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.quarkus.it.pushdispatch;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

/**
 * Landing route that lets the IT (and a human in dev mode) establish a Vaadin
 * session and Push websocket before triggering the blocking navigation. The
 * {@link RouterLink} goes through Vaadin's in-app router, which (with
 * {@code @Push(WEBSOCKET)}) sends the UIDL navigation request over the existing
 * Push websocket on the event loop — exactly the path that exercises the
 * dispatch-to-worker setting.
 * <p>
 * The on-page text explains what the user is reproducing, which is useful when
 * running {@code mvn quarkus:dev} against this module manually.
 */
@Route("")
public class LandingView extends Div {

    public static final String LINK_ID = "link-to-push-blocking";

    public LandingView() {
        add(new H1("Vaadin Quarkus — Push dispatch deadlock repro"));

        add(new Paragraph(
                "This module reproduces the deadlock that the Vaadin Quarkus "
                        + "extension's default `quarkus.websocket.dispatch-to-worker=true` "
                        + "protects against. With `@Push(WEBSOCKET)` the Vaadin "
                        + "navigation request flows over the Push websocket on the "
                        + "Vert.x event loop, so an `afterNavigation` hook that "
                        + "calls a synchronous Quarkus REST client ends up running "
                        + "blocking code on the event loop."));

        add(new Paragraph(new Span("Click the link below to navigate to "),
                code("/push-blocking"),
                new Span(
                        ", whose `afterNavigation` calls the REST endpoint at "),
                code("/api/slow"),
                new Span(" (sleeps 5s, returns \"ready\").")));

        add(new H1("Expected result"));
        add(new Paragraph(new Span("With the extension's default ("),
                code("quarkus.websocket.dispatch-to-worker=true"),
                new Span(") the navigation runs on a worker thread, the REST "
                        + "call returns normally, and the next page shows "),
                code(PushBlockingAfterNavView.READY_TEXT), new Span(".")));
        add(new Paragraph(new Span("With the Quarkus default ("),
                code("dispatch-to-worker=false"),
                new Span(") the navigation runs on the Vert.x event loop. "
                        + "RESTEasy Reactive refuses to block on that thread "
                        + "and throws "),
                code("BlockingNotAllowedException"),
                new Span("; Vaadin routes " + "to "),
                code("PushBlockingErrorView"), new Span(", which shows "),
                code(PushBlockingErrorView.ERROR_TEXT),
                new Span(" plus the full server-side stack trace.")));

        add(new Paragraph(new Span("Toggle the failure mode by uncommenting "),
                code("quarkus.websocket.dispatch-to-worker=false"),
                new Span(" in "), code("application.properties"),
                new Span(" and restarting.")));

        add(new Hr());

        RouterLink link = new RouterLink("Go to /push-blocking",
                PushBlockingAfterNavView.class);
        link.setId(LINK_ID);
        add(link);
    }

    private static Span code(String text) {
        Span s = new Span(text);
        s.getStyle().set("font-family", "monospace");
        s.getStyle().set("background", "rgba(0,0,0,0.07)");
        s.getStyle().set("padding", "0 0.25em");
        return s;
    }

}
