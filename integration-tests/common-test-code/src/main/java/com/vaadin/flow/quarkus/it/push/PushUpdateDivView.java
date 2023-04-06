/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.quarkus.it.push;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("push")
public class PushUpdateDivView extends Div {
    private final AtomicInteger count = new AtomicInteger();

    private final ScheduledExecutorService service = Executors
            .newScheduledThreadPool(1);

    private static final int DELAY = 100;

    private static final int MAX_UPDATE = 50;

    public PushUpdateDivView() {
        setId("push-update");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        updateDiv();
        scheduleUpdate(attachEvent.getUI());
    }

    private void scheduleUpdate(final UI ui) {
        service.schedule(() -> {
            ui.access(this::updateDiv);
            if (count.getAndIncrement() < MAX_UPDATE) {
                scheduleUpdate(ui);
            } else {
                service.shutdown();
            }
        }, DELAY, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        service.shutdownNow();
    }

    private void updateDiv() {
        setText(String.valueOf(count.get()));
    }

}
