# vaadin-quarkus
An extension to Quarkus to support Vaadin Flow.

## Development has moved to vaadin/flow

From **Vaadin 25.4** onwards `vaadin-quarkus` is developed and released as part
of Flow, in the [vaadin/flow](https://github.com/vaadin/flow) repository:

* the extension: [`vaadin-quarkus/`](https://github.com/vaadin/flow/tree/main/vaadin-quarkus)
* the integration tests: [`flow-tests/vaadin-quarkus-tests/`](https://github.com/vaadin/flow/tree/main/flow-tests/vaadin-quarkus-tests)

It follows Flow's version numbering there, so the release that goes with Vaadin
25.4 is `vaadin-quarkus` 25.4.0 rather than a 3.x one, and a new
`vaadin-quarkus` is published with every Flow release. The history of this
repository was carried over with the move, so `git log` and `git blame` on
those files still reach the commits made here.

**Please open issues and pull requests for Vaadin 25.4 and later in
[vaadin/flow](https://github.com/vaadin/flow/issues).** This tracker stays open
for the versions listed below.

## Branches for earlier Vaadin versions

Vaadin 25.3 and earlier are still served from this repository:

* `main` holds the 3.3 line, for Vaadin 25.3 Quarkus 3.33 (LTS)
* 3.2 for Vaadin 25.2 and Quarkus 3.33 (LTS)
* 3.1 for Vaadin 25.1 and Quarkus 3.32
* 3.0 for Vaadin 25.0 and Quarkus 3.32
* 2.2 for Vaadin 24 and Quarkus 3.20
* 1.1 for Vaadin 23 and Quarkus 2

> **NOTE:** The minimum supported Quarkus version for Vaadin 25.0 has been raised from 3.27 LTS to 3.32. This change is required because Flow now depends on Jackson 3.1.x and Jackson Annotations 2.21.x to address a security vulnerability.

## Getting started

To try it out, you can get a project https://github.com/vaadin/base-starter-flow-quarkus/

## Native image builds starting with Quarkus 3.33.3.3

Starting with Quarkus 3.33.3.3, the native image build of a Vaadin application fails while it analyses the application, with:

```
Error: Class initialization of io.vertx.core.json.Json failed.
...
Caused by: java.lang.NoClassDefFoundError: com/fasterxml/jackson/core/util/ByteArrayBuilder
```

Vert.x needs Jackson 2 core (`com.fasterxml.jackson.core:jackson-core`), but `quarkus-undertow` excludes it from Vert.x, and since that release no other dependency brings it in. Vaadin itself uses Jackson 3, which does not help here. This is a Quarkus bug, tracked in [quarkusio/quarkus#56989](https://github.com/quarkusio/quarkus/issues/56989). JVM mode is not affected unless the application uses the Vert.x JSON API.

Until a Quarkus release with the fix is out, either stay on Quarkus 3.33.3.2 or earlier, or add Jackson 2 core to the application. The Quarkus BOM manages its version:

```xml
<dependency>
  <groupId>com.fasterxml.jackson.core</groupId>
  <artifactId>jackson-core</artifactId>
</dependency>
```

## devUI URL
After executing the quarkus project with dev profile `mvn quarkus:dev`, the devUI can be accessed (with not overwritten configuration) at URL: `http://localhost:8080/q/dev-ui/extensions`

## Push dispatch

The extension sets `quarkus.websocket.dispatch-to-worker=true` as a default. This routes inbound Vaadin Push websocket frames through the Quarkus worker thread pool instead of the Vert.x event loop.

**The Quarkus default (`false`) is unsafe for Vaadin applications.** Vaadin's `PushHandler` acquires the session lock before dispatching, and application code is free to block while holding that lock (e.g. synchronous REST calls or database operations inside `BeforeEnterObserver` / `AfterNavigationListener`). With the Quarkus default, that blocking happens on the same Vert.x event loop that Push uses — deadlocking the loop and, if the REST client's response is pinned to the same loop, hanging the request indefinitely.

Override to `false` only if you fully control all session-locked code paths and have a reason to prefer event-loop dispatch (e.g. measurable latency-sensitive Push patterns with strictly non-blocking handlers).
