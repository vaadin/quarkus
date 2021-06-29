package com.vaadin.flow.quarkus.it;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import com.vaadin.flow.server.VaadinServlet;

@WebServlet(urlPatterns = "/*", name = "TestServlet", asyncSupported = true, initParams = {
        @WebInitParam(name = "org.atmosphere.websocket.suppressJSR356", value = "true") })
public class TestServlet extends VaadinServlet {
    // This servlet is here to give the suppressJSR356 parameter
}
