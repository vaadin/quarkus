package com.vaadin.quarkus;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InterceptorBinding;
import jakarta.interceptor.InvocationContext;
import jakarta.servlet.ServletException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Locale;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.quarkus.annotation.VaadinServiceEnabled;
import com.vaadin.quarkus.context.ServiceUnderTestContext;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class QuarkusInstantiatorTest {

    @Singleton
    public static class SomeCdiBean {
    }

    @ApplicationScoped
    public static class ScopedCdiBean {
    }

    @Interceptor
    @MyBinding
    public static class MyInterceptor {

        @AroundInvoke
        public Object intercept(InvocationContext ctx) throws Exception {
            return ctx.proceed();
        }
    }

    @InterceptorBinding
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.METHOD })
    public @interface MyBinding {

    }

    @Singleton
    public static class InterceptedCdiBean {

        @MyBinding
        public void exec() {

        }
    }

    public static class ParentBean {

        @Inject
        private BeanManager bm;

        public BeanManager getBm() {
            return bm;
        }

    }

    @Singleton
    @Tag("div")
    public static class SingletonComponent extends Component {
        @Inject
        BeanManager beanManager;
    }

    @Vetoed
    @Singleton
    public static class NotACdiBean extends ParentBean {
    }

    public static class Ambiguous extends ParentBean {
    }

    @VaadinServiceEnabled
    @Singleton
    public static class I18NTestProvider implements I18NProvider {

        @Override
        public List<Locale> getProvidedLocales() {
            return null;
        }

        @Override
        public String getTranslation(String key, Locale locale,
                Object... params) {
            return null;
        }

    }

    @Singleton
    public static class ServiceInitObserver {

        ServiceInitEvent event;

        public void serviceInit(@Observes ServiceInitEvent event) {
            this.event = event;
        }

        public ServiceInitEvent getEvent() {
            return event;
        }

    }

    @Inject
    private BeanManager beanManager;

    @Inject
    @VaadinServiceEnabled
    private QuarkusInstantiatorFactory instantiatorFactory;

    private Instantiator instantiator;

    @Inject
    private SomeCdiBean singleton;

    @Inject
    private ServiceInitObserver serviceInitObserver;

    private ServiceUnderTestContext serviceUnderTestContext;

    @BeforeEach
    public void setUp() {
        serviceUnderTestContext = new ServiceUnderTestContext(beanManager);
        serviceUnderTestContext.activate();
        instantiator = instantiatorFactory
                .createInstantitor(VaadinService.getCurrent());
    }

    @AfterEach
    public void tearDown() {
        serviceUnderTestContext.tearDownAll();
    }

    @Test
    public void getI18NProvider_beanEnabled_instanceReturned() {
        I18NProvider i18NProvider = instantiator.getI18NProvider();
        Assertions.assertNotNull(i18NProvider);
        Assertions.assertTrue((i18NProvider instanceof I18NTestProvider));
    }

    /*
     * @Test public void
     * getServiceInitListeners_javaSPIListenerExists_containsJavaSPIListener() {
     * Assertions.assertTrue(instantiator.getServiceInitListeners().anyMatch(
     * listener -> listener instanceof JavaSPIVaadinServiceInitListener)); }
     */

    @Test
    public void getServiceInitListeners_eventFired_cdiObserverCalled() {
        VaadinService service = Mockito.mock(VaadinService.class);
        ServiceInitEvent event = new ServiceInitEvent(service);
        instantiator.getServiceInitListeners()
                .filter(listener -> listener.getClass().getPackage()
                        .equals(QuarkusInstantiator.class.getPackage()))
                .forEach(listener -> listener.serviceInit(event));
        Assertions.assertSame(event, serviceInitObserver.getEvent());
    }

    @Test
    public void getOrCreate_beanSingleton_sameInstanceReturned() {
        Assertions.assertSame(singleton,
                instantiator.getOrCreate(SomeCdiBean.class));
    }

    @Test
    @Disabled("Unmanaged instance are currently not supported in Quarkus add-on, see comments in QuarkusInstantiator.getOrCreate")
    public void getOrCreate_beanUnsatisfied_instantiatedAndInjectionOccurred() {
        NotACdiBean instance = instantiator.getOrCreate(NotACdiBean.class);
        Assertions.assertNotNull(instance);
        Assertions.assertNotNull(instance.getBm());
    }

    @Test
    @Disabled("Unmanaged instance are currently not supported in Quarkus add-on, see comments in QuarkusInstantiator.getOrCreate")
    public void getOrCreate_beanAmbiguous_instantiatedAndInjectionOccurred() {
        ParentBean instance = instantiator.getOrCreate(ParentBean.class);
        Assertions.assertNotNull(instance);
        Assertions.assertNotNull(instance.getBm());
    }

    @Test
    @Disabled("Unmanaged instance are currently not supported in Quarkus add-on, see comments in QuarkusInstantiator.getOrCreate")
    public void createComponent_componentIsCreated() {
        SingletonComponent component = instantiator
                .createComponent(SingletonComponent.class);
        Assertions.assertNotNull(component);
        Assertions.assertNotNull(component.beanManager);
    }

    @Test
    public void createComponent_componentIsCreatedOnEveryCall()
            throws ServletException {
        SingletonComponent component = instantiator
                .createComponent(SingletonComponent.class);
        Assertions.assertNotNull(component);

        SingletonComponent anotherComponent = instantiator
                .createComponent(SingletonComponent.class);
        Assertions.assertNotEquals(component, anotherComponent);
    }

    @Test
    public void getApplicationClass_regularClass_getsSameClass()
            throws ServletException {
        SomeCdiBean instance = instantiator.getOrCreate(SomeCdiBean.class);
        Assertions.assertSame(SomeCdiBean.class,
                instantiator.getApplicationClass(instance));
        Assertions.assertSame(SomeCdiBean.class,
                instantiator.getApplicationClass(instance.getClass()));
    }

    @Test
    public void getApplicationClass_scopedBean_getsApplicationClass()
            throws ServletException {
        ScopedCdiBean instance = instantiator.getOrCreate(ScopedCdiBean.class);
        Assertions.assertSame(ScopedCdiBean.class,
                instantiator.getApplicationClass(instance));
        Assertions.assertSame(ScopedCdiBean.class,
                instantiator.getApplicationClass(instance.getClass()));
    }

    @Test
    public void getApplicationClass_proxiedBean_getsApplicationClass()
            throws ServletException {
        InterceptedCdiBean instance = instantiator
                .getOrCreate(InterceptedCdiBean.class);
        instance.exec();
        Assertions.assertNotSame(InterceptedCdiBean.class, instance.getClass());
        Assertions.assertSame(InterceptedCdiBean.class,
                instantiator.getApplicationClass(instance));
        Assertions.assertSame(InterceptedCdiBean.class,
                instantiator.getApplicationClass(instance.getClass()));
    }

}
