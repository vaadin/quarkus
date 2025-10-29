/*
 *  Copyright 2000-2025 Vaadin Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package com.vaadin.quarkus;

import com.vaadin.flow.shared.ApplicationConstants;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class BodyHandlerRecorder {

    /**
     * In hybrid environment sometimes the requests hangs while reading body,
     * causing the UI to freeze until read timeout is reached. This method
     * returns a vert.x handler that delegates Flow requests to the Quarkus
     * request body handler before proceeding with the execution.
     * <p>
     * See https://github.com/vaadin/quarkus/issues/138
     *
     * @param bodyHandler
     *            Quarkus request body handler
     * @return a new handler that delegates Flow requests to request body
     *         handler.
     */
    public Handler<RoutingContext> installBodyHandler(
            Handler<RoutingContext> bodyHandler) {
        return routingContext -> {
            HttpServerRequest request = routingContext.request();
            // Do not delegate request to static resources
            if (request.params()
                    .contains(ApplicationConstants.REQUEST_TYPE_PARAMETER)
                    && !request.uri().startsWith("/VAADIN/")
                    && !request.uri().startsWith("/HILLA/")) {
                bodyHandler.handle(routingContext);
            } else {
                routingContext.next();
            }
        };
    }
}
