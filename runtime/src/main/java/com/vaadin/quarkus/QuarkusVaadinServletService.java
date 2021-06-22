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
package com.vaadin.quarkus;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.util.Optional;
import java.util.Set;

import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.UsageStatistics;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.VaadinServletService;

public class QuarkusVaadinServletService extends VaadinServletService {

    private BeanManager beanManager;

    public QuarkusVaadinServletService(final QuarkusVaadinServlet servlet,
            final DeploymentConfiguration configuration,
            final BeanManager beanManager) {
        super(servlet, configuration);
        this.beanManager = beanManager;
        reportUsage();
    }

    private void reportUsage() {
        if(!getDeploymentConfiguration().isProductionMode()) {
            UsageStatistics.markAsUsed("flow/quarkus", null);
        }
    }

    @Override
    public Optional<Instantiator> loadInstantiators() throws ServiceException {
        final Set<Bean<?>> beans = beanManager
                .getBeans(Instantiator.class, BeanLookup.SERVICE);
        if (beans == null || beans.isEmpty()) {
            throw new ServiceException("Cannot init VaadinService "
                    + "because no CDI instantiator bean found.");
        }
        final Bean<Instantiator> bean;
        try {
            // noinspection unchecked
            bean = (Bean<Instantiator>) beanManager.resolve(beans);
        } catch (final AmbiguousResolutionException e) {
            throw new ServiceException(
                    "There are multiple eligible CDI " + Instantiator.class
                            .getSimpleName() + " beans.", e);
        }

        // Return the contextual instance (rather than CDI proxy) as it will be
        // stored inside VaadinService. Not relying on the proxy allows
        // accessing VaadinService::getInstantiator even when
        // VaadinServiceScopedContext is not active
        final CreationalContext<Instantiator> creationalContext = beanManager
                .createCreationalContext(bean);
        final Context context = beanManager
                .getContext(ApplicationScoped.class); // VaadinServiceScoped
        final Instantiator instantiator = context.get(bean, creationalContext);

        if (!instantiator.init(this)) {
            throw new ServiceException(
                    "Cannot init VaadinService because " + instantiator
                            .getClass().getName() + " CDI bean init()"
                            + " returned false.");
        }
        return Optional.of(instantiator);
    }

    @Override
    public QuarkusVaadinServlet getServlet() {
        return (QuarkusVaadinServlet) super.getServlet();
    }
}
