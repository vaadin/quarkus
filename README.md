# vaadin-quarkus
An extension to Quarkus to support Vaadin Flow.

Supports Quarkus 3.32+

To try it out, you can get a project https://github.com/vaadin/base-starter-flow-quarkus/

This branch is compatible with upcoming Vaadin 25.1+ platform versions and uses Quarkus 3.32 (LTS). See other branches for other Vaadin versions:

* 3.1 for Vaadin 25.1 and Quarkus 3.32
* 3.0 for Vaadin 25.0 and Quarkus 3.32
* 2.2 for Vaadin 24 and Quarkus 3.20
* 1.1 for Vaadin 23 and Quarkus 2

> **NOTE:** The minimum supported Quarkus version for Vaadin 25.0 has been raised from 3.27 LTS to 3.32 LTS. This change is required because Flow now depends on Jackson 3.1.x and Jackson Annotations 2.21.x to address a security vulnerability.

## devUI URL
After executing the quarkus project with dev profile `mvn quarkus:dev`, the devUI can be accessed (with not overwritten configuration) at URL: `http://localhost:8080/q/dev-ui/extensions`
