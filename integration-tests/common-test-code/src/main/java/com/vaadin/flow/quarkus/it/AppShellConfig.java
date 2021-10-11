package com.vaadin.flow.quarkus.it;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.ui.Transport;
import com.vaadin.flow.theme.Theme;

@Theme("reusable-theme")
// TODO: Enable websockets when they are in use in Quarkus
@Push(transport = Transport.LONG_POLLING)
public class AppShellConfig implements AppShellConfigurator {
}
