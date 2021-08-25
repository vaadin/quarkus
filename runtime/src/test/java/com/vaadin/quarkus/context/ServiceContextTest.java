/*
 * Copyright 2000-2018 Vaadin Ltd.
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

package com.vaadin.quarkus.context;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import io.quarkus.test.junit.QuarkusTest;

import com.vaadin.quarkus.annotation.VaadinServiceScoped;

@QuarkusTest
public class ServiceContextTest extends
        AbstractContextTest<ServiceContextTest.ServiceScopedTestBean, VaadinServiceScopedContext> {
    @Inject
    private BeanManager beanManager;

    @Override
    protected Class<ServiceScopedTestBean> getBeanType() {
        return ServiceScopedTestBean.class;
    }

    @Override
    protected UnderTestContext newContextUnderTest() {
        return new ServiceUnderTestContext(beanManager);
    }

    @Override
    protected Class<VaadinServiceScopedContext> getContextType() {
        return VaadinServiceScopedContext.class;
    }

    @VaadinServiceScoped
    public static class ServiceScopedTestBean extends TestBean {
    }

}
