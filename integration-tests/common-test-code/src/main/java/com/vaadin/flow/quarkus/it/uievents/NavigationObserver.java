/*
 * Copyright 2000-2018 Vaadin Ltd.
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

package com.vaadin.flow.quarkus.it.uievents;

import jakarta.enterprise.event.Observes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.quarkus.annotation.VaadinSessionScoped;

@VaadinSessionScoped
public class NavigationObserver implements Serializable {

    private List<EventObject> navigationEvents = new ArrayList<>();

    private void onBeforeLeave(@Observes BeforeLeaveEvent event) {
        navigationEvents.add(event);
    }

    private void onBeforeEnter(@Observes BeforeEnterEvent event) {
        navigationEvents.add(event);
    }

    /**
     * <a href="https://stackoverflow.com/questions/48902847/cdi-observer-condition-in-dependent-bean">...</a>
     * <a href="https://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#conditional_observer_methods">...</a>
     * <p>
     * Removed conditional observer method because it is not supported 1.2 CDI spec.
     * <p>
     * Beans with scope @Dependent may not have conditional observer methods.
     * If a bean with scope @Dependent has an observer method declared receive=IF_EXISTS,
     * the container automatically detects the problem and treats it as a definition error.
     */
    private void onAfterNavigation(@Observes AfterNavigationEvent event) {
        navigationEvents.add(event);
    }

    public List<EventObject> getNavigationEvents() {
        return navigationEvents;
    }

}
