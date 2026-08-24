# vaadin-quarkus
An extension to Quarkus to support Vaadin Flow.

Supports Quarkus 3.33+

To try it out, you can get a project https://github.com/vaadin/base-starter-flow-quarkus/

This branch is compatible with upcoming Vaadin 25.2+ platform versions and uses Quarkus 3.33 (LTS). See other branches for other Vaadin versions:

* 3.2 for Vaadin 25.2 and Quarkus 3.33
* 3.1 for Vaadin 25.1 and Quarkus 3.32
* 3.0 for Vaadin 25.0 and Quarkus 3.32
* 2.2 for Vaadin 24 and Quarkus 3.20
* 1.1 for Vaadin 23 and Quarkus 2

> **NOTE:** The minimum supported Quarkus version for Vaadin 25.0 has been raised from 3.27 LTS to 3.32. This change is required because Flow now depends on Jackson 3.1.x and Jackson Annotations 2.21.x to address a security vulnerability.

## Configuration

Vaadin configuration parameters can be set through MicroProfile Config, i.e. in `application.properties` or `application.yaml`, using the `vaadin.` prefix. The part after the prefix is the standard Vaadin init parameter name (see `com.vaadin.flow.server.InitParameters`). For example:

```properties
vaadin.heartbeatInterval=120
vaadin.closeIdleSessions=true
```

Values defined this way override the corresponding servlet init parameters declared via `@WebServlet` / `@WebInitParam`.

## devUI URL
After executing the quarkus project with dev profile `mvn quarkus:dev`, the devUI can be accessed (with not overwritten configuration) at URL: `http://localhost:8080/q/dev-ui/extensions`

## Push dispatch

The extension sets `quarkus.websocket.dispatch-to-worker=true` as a default. This routes inbound Vaadin Push websocket frames through the Quarkus worker thread pool instead of the Vert.x event loop.

**The Quarkus default (`false`) is unsafe for Vaadin applications.** Vaadin's `PushHandler` acquires the session lock before dispatching, and application code is free to block while holding that lock (e.g. synchronous REST calls or database operations inside `BeforeEnterObserver` / `AfterNavigationListener`). With the Quarkus default, that blocking happens on the same Vert.x event loop that Push uses — deadlocking the loop and, if the REST client's response is pinned to the same loop, hanging the request indefinitely.

Override to `false` only if you fully control all session-locked code paths and have a reason to prefer event-loop dispatch (e.g. measurable latency-sensitive Push patterns with strictly non-blocking handlers).
