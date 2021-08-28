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

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class RouteContextPseudoTest
        extends AbstractContextTest<RouteScopedContext> {

    @Override
    protected UnderTestContext newContextUnderTest() {
        // Intentionally UI Under Test Context. Nothing else needed.
        UIUnderTestContext context = new UIUnderTestContext() {

            @Override
            public void activate() {
                super.activate();

                // NavigationData data = new NavigationData(
                // TestNavigationTarget.class, Collections.emptyList());
                // ComponentUtil.setData(getUi(), NavigationData.class, data);
            }
        };

        return context;
    }

    @Override
    protected Class<RouteScopedContext> getContextType() {
        // TODO Auto-generated method stub
        return null;
    }

}
