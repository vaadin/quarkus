package com.vaadin.quarkus.deployment;

import com.vaadin.flow.router.Route;

import org.jboss.jandex.DotName;

import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class VaadinQuarkusProcessor {

    private static final String FEATURE = "vaadin-quarkus";

    private static final DotName ROUTE_ANNOTATION = DotName.createSimple(Route.class.getName());

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }
    
    @BuildStep
    public void build(final BuildProducer<BeanDefiningAnnotationBuildItem> additionalBeanDefiningAnnotationRegistry) {
        // Make and Route annotated Component a bean for injection
        additionalBeanDefiningAnnotationRegistry.produce(new BeanDefiningAnnotationBuildItem(ROUTE_ANNOTATION));
    }
}
