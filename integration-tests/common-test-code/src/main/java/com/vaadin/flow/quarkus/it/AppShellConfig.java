package com.vaadin.flow.quarkus.it;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import io.quarkus.runtime.annotations.RegisterForReflection;

@Theme("reusable-theme")
@Push
@RegisterForReflection(classNames = "org.vaadin.sample.websockets.SimpleEndpoint")
public class AppShellConfig implements AppShellConfigurator {
}
