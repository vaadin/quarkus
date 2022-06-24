/*
 * Copyright 2000-2021 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.quarkus.deployment;

import javax.servlet.annotation.WebServlet;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanArchiveIndexBuildItem;
import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.arc.deployment.ContextRegistrationPhaseBuildItem;
import io.quarkus.arc.deployment.ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem;
import io.quarkus.arc.deployment.CustomScopeBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.undertow.deployment.ServletBuildItem;
import io.quarkus.undertow.deployment.ServletDeploymentManagerBuildItem;
import io.quarkus.vertx.http.deployment.FilterBuildItem;
import io.quarkus.websockets.client.deployment.ServerWebSocketContainerBuildItem;
import io.quarkus.websockets.client.deployment.WebSocketDeploymentInfoBuildItem;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.quarkus.QuarkusVaadinServlet;
import com.vaadin.quarkus.WebsocketHttpSessionAttachRecorder;
import com.vaadin.quarkus.annotation.NormalRouteScoped;
import com.vaadin.quarkus.annotation.NormalUIScoped;
import com.vaadin.quarkus.annotation.RouteScoped;
import com.vaadin.quarkus.annotation.UIScoped;
import com.vaadin.quarkus.annotation.VaadinServiceScoped;
import com.vaadin.quarkus.annotation.VaadinSessionScoped;
import com.vaadin.quarkus.context.RouteContextWrapper;
import com.vaadin.quarkus.context.RouteScopedContext;
import com.vaadin.quarkus.context.UIContextWrapper;
import com.vaadin.quarkus.context.UIScopedContext;
import com.vaadin.quarkus.context.VaadinServiceScopedContext;
import com.vaadin.quarkus.context.VaadinSessionScopedContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class VaadinQuarkusProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(VaadinQuarkusProcessor.class);

    private static final String FEATURE = "vaadin-quarkus";

    private static final DotName ROUTE_ANNOTATION = DotName
            .createSimple(Route.class.getName());

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public void build(
            final BuildProducer<AdditionalBeanBuildItem> additionalBeanProducer,
            final BuildProducer<BeanDefiningAnnotationBuildItem> additionalBeanDefiningAnnotationRegistry) {
        additionalBeanProducer.produce(AdditionalBeanBuildItem
                .unremovableOf(QuarkusVaadinServlet.class));

        // Make and Route annotated Component a bean for injection
        additionalBeanDefiningAnnotationRegistry
                .produce(new BeanDefiningAnnotationBuildItem(ROUTE_ANNOTATION));
    }

    @BuildStep
    public void specifyErrorViewsBeans(CombinedIndexBuildItem item,
            BuildProducer<AdditionalBeanBuildItem> additionalBeanProducer) {
        Collection<ClassInfo> errors = item.getComputingIndex()
                .getAllKnownImplementors(DotName
                        .createSimple(HasErrorParameter.class.getName()));
        for (ClassInfo errorInfo : errors) {
            additionalBeanProducer.produce(AdditionalBeanBuildItem
                    .unremovableOf(errorInfo.name().toString()));
        }
    }

    @BuildStep
    void mapVaadinServletPaths(final BeanArchiveIndexBuildItem beanArchiveIndex,
            final BuildProducer<ServletBuildItem> servletProducer) {
        final IndexView indexView = beanArchiveIndex.getIndex();

        // Collect all VaadinServlet instances and remove QuarkusVaadinServlet
        // and VaadinServlet from the list.
        final Collection<ClassInfo> vaadinServlets = indexView
                .getAllKnownSubclasses(
                        DotName.createSimple(VaadinServlet.class.getName()))
                .stream()
                .filter(servlet -> !servlet.name().toString()
                        .equals(QuarkusVaadinServlet.class.getName())
                        && !servlet.name().toString()
                                .equals(VaadinServlet.class.getName()))
                .collect(Collectors.toList());

        // If no VaadinServlet instances found register QuarkusVaadinServlet
        if (vaadinServlets.isEmpty()) {
            servletProducer.produce(ServletBuildItem
                    .builder(QuarkusVaadinServlet.class.getName(),
                            QuarkusVaadinServlet.class.getName())
                    .addMapping("/*").setAsyncSupported(true).build());
        } else {
            registerUserServlets(servletProducer, vaadinServlets);
        }
    }

    @BuildStep
    ContextConfiguratorBuildItem registerVaadinServiceScopedContext(
            ContextRegistrationPhaseBuildItem phase) {
        return new ContextConfiguratorBuildItem(
                phase.getContext().configure(VaadinServiceScoped.class).normal()
                        .contextClass(VaadinServiceScopedContext.class));
    }

    @BuildStep
    CustomScopeBuildItem serviceScope() {
        return new CustomScopeBuildItem(VaadinServiceScoped.class);
    }

    @BuildStep
    ContextConfiguratorBuildItem registerVaadinSessionScopedContext(
            ContextRegistrationPhaseBuildItem phase) {
        return new ContextConfiguratorBuildItem(
                phase.getContext().configure(VaadinSessionScoped.class).normal()
                        .contextClass(VaadinSessionScopedContext.class));
    }

    @BuildStep
    CustomScopeBuildItem sessionScope() {
        return new CustomScopeBuildItem(VaadinSessionScoped.class);
    }

    @BuildStep
    ContextConfiguratorBuildItem registerUIScopedContext(
            ContextRegistrationPhaseBuildItem phase) {
        return new ContextConfiguratorBuildItem(
                phase.getContext().configure(NormalUIScoped.class).normal()
                        .contextClass(UIScopedContext.class));
    }

    @BuildStep
    ContextConfiguratorBuildItem registerPseudoUIScopedContext(
            ContextRegistrationPhaseBuildItem phase) {
        return new ContextConfiguratorBuildItem(
                phase.getContext().configure(UIScoped.class)
                        .contextClass(UIContextWrapper.class));
    }

    @BuildStep
    CustomScopeBuildItem normalUiScope() {
        return new CustomScopeBuildItem(NormalUIScoped.class);
    }

    @BuildStep
    CustomScopeBuildItem uiScope() {
        return new CustomScopeBuildItem(UIScoped.class);
    }

    @BuildStep
    ContextConfiguratorBuildItem registerRouteScopedContext(
            ContextRegistrationPhaseBuildItem phase) {
        return new ContextConfiguratorBuildItem(
                phase.getContext().configure(NormalRouteScoped.class).normal()
                        .contextClass(RouteScopedContext.class));
    }

    @BuildStep
    ContextConfiguratorBuildItem registerPseudoRouteScopedContext(
            ContextRegistrationPhaseBuildItem phase) {
        return new ContextConfiguratorBuildItem(
                phase.getContext().configure(RouteScoped.class)
                        .contextClass(RouteContextWrapper.class));
    }

    @BuildStep
    CustomScopeBuildItem normalRouteScope() {
        return new CustomScopeBuildItem(NormalRouteScoped.class);
    }

    @BuildStep
    CustomScopeBuildItem rRouteScope() {
        return new CustomScopeBuildItem(RouteScoped.class);
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void setupPush(ServletDeploymentManagerBuildItem deployment,
            WebSocketDeploymentInfoBuildItem webSocketDeploymentInfoBuildItem,
            ServerWebSocketContainerBuildItem serverWebSocketContainerBuildItem,
            BuildProducer<FilterBuildItem> filterProd,
            WebsocketHttpSessionAttachRecorder recorder) {

        filterProd.produce(new FilterBuildItem(recorder.createWebSocketHandler(
                webSocketDeploymentInfoBuildItem.getInfo(),
                serverWebSocketContainerBuildItem.getContainer(),
                deployment.getDeploymentManager()), 120));
    }

    private void registerUserServlets(
            BuildProducer<ServletBuildItem> servletProducer,
            Collection<ClassInfo> vaadinServlets) {
        // TODO: check that we don't register 2 of the same mapping
        for (ClassInfo info : vaadinServlets) {
            final AnnotationInstance webServletInstance = info.classAnnotation(
                    DotName.createSimple(WebServlet.class.getName()));
            if (webServletInstance == null) {
                LOG.warn("Found unexpected {} extends VaadinServlet without @WebServlet, skipping", info.name());
                continue;
            }

            String servletName = Optional
                    .ofNullable(webServletInstance.value("name"))
                    .map(AnnotationValue::asString)
                    .orElse(info.name().toString());
            final ServletBuildItem.Builder servletBuildItem = ServletBuildItem
                    .builder(servletName, info.name().toString());

            Stream.of(webServletInstance.value("value"),
                    webServletInstance.value("urlPatterns"))
                    .filter(Objects::nonNull)
                    .flatMap(value -> Stream.of(value.asStringArray()))
                    .forEach(servletBuildItem::addMapping);

            addWebInitParameters(webServletInstance, servletBuildItem);
            setAsyncSupportedIfDefined(webServletInstance, servletBuildItem);

            servletProducer.produce(servletBuildItem.build());
        }
    }

    private void addWebInitParameters(AnnotationInstance webServletInstance,
            ServletBuildItem.Builder servletBuildItem) {
        // Add WebInitParam parameters to registration
        AnnotationValue initParams = webServletInstance.value("initParams");
        if (initParams != null) {
            for (AnnotationInstance initParam : initParams.asNestedArray()) {
                servletBuildItem.addInitParam(
                        initParam.value("name").asString(),
                        initParam.value().asString());
            }
        }
    }

    private void setAsyncSupportedIfDefined(
            AnnotationInstance webServletInstance,
            ServletBuildItem.Builder servletBuildItem) {
        final AnnotationValue asyncSupported = webServletInstance
                .value("asyncSupported");
        if (asyncSupported != null) {
            servletBuildItem.setAsyncSupported(asyncSupported.asBoolean());
        }
    }
}
