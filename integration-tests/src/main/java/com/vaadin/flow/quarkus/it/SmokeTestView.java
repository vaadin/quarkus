/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vaadin.flow.quarkus.it;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import java.io.Serializable;

@Route("")
public class SmokeTestView extends Div {
    @Inject
    HelloProvider helloProvider;

    public SmokeTestView() {
        add(new NativeButton("Click me",
                event -> add(new Label(getLabelText()))));
    }

    private String getLabelText() {
        if (helloProvider != null) {
            return helloProvider.getHello();
        } else {
            return "no CDI enabled";
        }
    }

    @PostConstruct
    private void init() {
        helloProvider.setHello("hello quarkus CDI");
    }

    @SessionScoped
    public static class HelloProvider implements Serializable {
        private String hello;

        public String getHello() {
            return hello;
        }

        public void setHello(String hello) {
            this.hello = hello;
        }
    }

}
