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

package com.vaadin.flow.quarkus.it;

import java.io.IOException;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.By;

import com.vaadin.flow.quarkus.it.routecontext.ApartBean;
import com.vaadin.flow.quarkus.it.routecontext.AssignedBean;
import com.vaadin.flow.quarkus.it.routecontext.BeanNoOwner;
import com.vaadin.flow.quarkus.it.routecontext.DetailApartView;
import com.vaadin.flow.quarkus.it.routecontext.DetailAssignedView;
import com.vaadin.flow.quarkus.it.routecontext.MasterView;
import com.vaadin.flow.quarkus.it.routecontext.PreserveOnRefreshBean;
import com.vaadin.flow.quarkus.it.routecontext.RootView;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RouteContextTest extends AbstractCdiTest {

    private String uiId;

    @Override
    protected String getTestPath() {
        return "/route";
    }

    @BeforeEach
    public void setUp() throws Exception {
        resetCounts();
        open("");
        uiId = getText(MainLayout.UIID);
        assertConstructed(AssignedBean.class, 0);
        assertConstructed(ApartBean.class, 0);
    }

    @Test
    public void navigateFromRootToMasterReleasesRootInjectsEmptyBeans()
            throws IOException {
        follow(RootView.MASTER);
        assertTextEquals("", MasterView.ASSIGNED_BEAN_LABEL);

        assertConstructed(AssignedBean.class, 1);
        assertDestroyed(AssignedBean.class, 0);
        assertConstructed(ApartBean.class, 0);
        assertDestroyed(ApartBean.class, 0);
    }

    @Test
    public void navigationFromAssignedToMasterHoldsGroup() throws IOException {
        follow(RootView.MASTER);
        follow(MasterView.ASSIGNED);
        assertTextEquals("ASSIGNED", DetailAssignedView.BEAN_LABEL);

        follow(DetailAssignedView.MASTER);

        assertTextEquals("ASSIGNED", MasterView.ASSIGNED_BEAN_LABEL);
    }

    @Test
    public void navigationFromApartToMasterReleasesGroup() throws IOException {
        follow(RootView.MASTER);
        follow(MasterView.APART);
        assertTextEquals("", MasterView.ASSIGNED_BEAN_LABEL);
        assertTextEquals("APART", DetailApartView.BEAN_LABEL);

        follow(DetailApartView.MASTER);

        assertTextEquals("", MasterView.ASSIGNED_BEAN_LABEL);
    }

    @Test
    public void beansWithNoOwner_preservedWithinTheSameRouteTarget_notPreservedAfterNavigation()
            throws IOException {
        follow(MainLayout.PARENT_NO_OWNER);

        assertConstructed(BeanNoOwner.class, 1);
        assertDestroyed(BeanNoOwner.class, 0);

        follow("child");

        assertDestroyed(BeanNoOwner.class, 0);

        follow("parent");

        assertConstructed(BeanNoOwner.class, 2);
        assertDestroyed(BeanNoOwner.class, 1);
    }

    @Test
    public void beanWithNoOwner_preservedWithinTheSameRoutingChain()
            throws IOException {
        follow(MainLayout.CHILD_NO_OWNER);

        assertConstructed(BeanNoOwner.class, 1);
        assertDestroyed(BeanNoOwner.class, 0);

        findElement(By.id("reset")).click();

        assertDestroyed(BeanNoOwner.class, 0);
    }

    @Test
    public void routeScopedBeanIsDestroyedOnNavigationOutOfViewAfterPreserveOnRefresh()
            throws IOException {
        follow(MainLayout.PRESERVE);

        assertConstructed(PreserveOnRefreshBean.class, 1);
        assertDestroyed(PreserveOnRefreshBean.class, 0);

        // refresh
        getDriver().get(getDriver().getCurrentUrl());

        // UI ID has to be updated: all bean creations/removals will be done
        // now within the new UI
        uiId = getText(MainLayout.UIID);

        // navigate out of the preserved view
        follow(MainLayout.PARENT_NO_OWNER);

        assertDestroyed(PreserveOnRefreshBean.class, 1);
    }

    @Test
    public void preserveOnRefresh_beanIsNotDestroyed() throws IOException {
        follow(MainLayout.PRESERVE);

        assertConstructed(PreserveOnRefreshBean.class, 1);
        assertDestroyed(PreserveOnRefreshBean.class, 0);

        String beanData = findElement(By.id("preserve-on-refresh")).getText();

        // refresh
        getDriver().get(getDriver().getCurrentUrl());

        // check that the bean has not been removed in the previous UI
        assertDestroyed(PreserveOnRefreshBean.class, 0);

        // UI ID has to be updated: all bean creations/removals will be done
        // now within the new UI
        uiId = getText(MainLayout.UIID);

        // the bean should not be destroyed with the new UI as well
        assertDestroyed(PreserveOnRefreshBean.class, 0);

        Assertions.assertEquals(beanData,
                findElement(By.id("preserve-on-refresh")).getText());
    }

    private void assertRootViewIsDisplayed() {
        assertTextEquals(uiId, MainLayout.UIID);
    }

    private void assertConstructed(Class beanClass, int count)
            throws IOException {
        Assertions.assertEquals(count,
                getCount(beanClass.getSimpleName() + "C" + uiId));
    }

    private void assertDestroyed(Class beanClass, int count)
            throws IOException {
        Assertions.assertEquals(count,
                getCount(beanClass.getSimpleName() + "D" + uiId));
    }

}
