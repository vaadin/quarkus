package com.vaadin.flow.quarkus.it.layout;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route(value = "injected-layout-view", layout = LayoutWithInjection.class)
public class LayoutWithInjectionView extends Div {

}
