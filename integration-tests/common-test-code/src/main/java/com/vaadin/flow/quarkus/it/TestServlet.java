package com.vaadin.flow.quarkus.it;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import com.vaadin.quarkus.QuarkusVaadinServlet;

@WebServlet(urlPatterns = "/*", name = "TestServlet", asyncSupported = true, initParams = {
        @WebInitParam(name = "org.atmosphere.websocket.suppressJSR356", value = "true") })
public class TestServlet extends QuarkusVaadinServlet {
    // This servlet is here to give the suppressJSR356 parameter
}
