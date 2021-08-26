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

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.quarkus.test.junit.QuarkusTest;
import org.mockito.Mockito;

import com.vaadin.flow.server.VaadinSession;
import com.vaadin.quarkus.context.UiContextPseudoTest.TestUIScopedContext;

@QuarkusTest
public class UiContextPseudoTest
        extends AbstractContextTest<TestUIScopedContext> {

    @Override
    protected UnderTestContext newContextUnderTest() {
        return new UIUnderTestContext();
    }

    @Override
    protected Class<TestUIScopedContext> getContextType() {
        return TestUIScopedContext.class;
    }

    public static class TestUIScopedContext extends UIScopedContext {

        @Override
        BeanManager getBeanManager() {
            BeanManager beanManager = super.getBeanManager();
            BeanManager proxy = (BeanManager) Proxy.newProxyInstance(
                    Thread.currentThread().getContextClassLoader(),
                    new Class<?>[] { BeanManager.class },
                    new BeanManagerProxy(beanManager));
            return proxy;
        }
    }

    private static class BeanManagerProxy implements InvocationHandler {
        private static final String SCOPE_ATTRIBUTE = "test-"
                + UIScopedContext.ContextualStorageManager.class;

        private BeanManager delegate;

        private final Bean<?> fakeBean = Mockito.mock(Bean.class);

        private final CreationalContext<?> fakeContext = Mockito
                .mock(CreationalContext.class);

        BeanManagerProxy(BeanManager delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            if (method.getName().equals("getBeans") && args[0]
                    .equals(UIScopedContext.ContextualStorageManager.class)) {
                Set<Bean<?>> set = Collections.singleton(fakeBean);
                return set;
            }
            if (method.getName().equals("resolve")
                    && args[0].equals(Collections.singleton(fakeBean))) {
                return fakeBean;
            }
            if (method.getName().equals("createCreationalContext")
                    && args[0].equals(fakeBean)) {
                return fakeContext;
            }
            if (method.getName().equals("getReference")
                    && args[0].equals(fakeBean)
                    && args[1].equals(
                            UIScopedContext.ContextualStorageManager.class)
                    && args[2].equals(fakeContext)) {
                VaadinSession session = VaadinSession.getCurrent();
                Object value = session.getAttribute(SCOPE_ATTRIBUTE);
                if (value == null) {
                    value = new UIScopedContext.ContextualStorageManager();
                    session.setAttribute(SCOPE_ATTRIBUTE, value);
                }
                return value;
            }

            return delegate(method, args);
        }

        private Object delegate(Method method, Object[] args) throws Throwable {
            Method[] methods = delegate.getClass().getDeclaredMethods();
            List<Method> filtered = Stream.of(methods).filter(
                    origMethod -> origMethod.getName().equals(method.getName()))
                    .collect(Collectors.toList());

            Method found = null;
            if (filtered.size() == 1) {
                found = filtered.get(0);
            } else {
                for (Method overloaded : filtered) {
                    if (overloaded.getParameterCount() == method
                            .getParameterCount()) {
                        found = overloaded;
                        break;
                    }
                }
            }
            return found.invoke(delegate, args);
        }

    }
}