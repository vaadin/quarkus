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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.quarkus.annotation.VaadinServiceScoped;

@VaadinServiceScoped
public class ServiceBean {
    private static final String id = UUID.randomUUID().toString();

    private static final AtomicInteger beansCount = new AtomicInteger();

    public String getId() {
        return id + "-" + beansCount.get();
    }

    @PostConstruct
    void postConstruct() {
        beansCount.incrementAndGet();
    }
}
