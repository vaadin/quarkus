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

import io.quarkus.arc.InjectableContext;
import io.quarkus.test.junit.QuarkusTest;

import com.vaadin.quarkus.context.UiContextNormalTest.TestNormalUIContextWrapper;
import com.vaadin.quarkus.context.UiContextPseudoTest.TestUIScopedContext;

@QuarkusTest
public class UiContextNormalTest
        extends InjectableContextTest<TestNormalUIContextWrapper> {

    @Override
    protected UnderTestContext newContextUnderTest() {
        return new UIUnderTestContext();
    }

    @Override
    protected Class<TestNormalUIContextWrapper> getContextType() {
        return TestNormalUIContextWrapper.class;
    }

    public static class TestNormalUIContextWrapper
            extends NormalUIContextWrapper {
        @Override
        InjectableContext getContext() {
            return new TestUIScopedContext();
        }
    }
}