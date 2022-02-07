package com.vaadin.flow.quarkus.it;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.ui.Transport;
import com.vaadin.flow.theme.Theme;

@Theme("reusable-theme")
@Push(transport = Transport.LONG_POLLING) // Websocket not supported currently
public class AppShellConfig implements AppShellConfigurator {
}
