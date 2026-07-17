package org.apache.dubbo.solon;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ConsumerConfig;
import org.apache.dubbo.config.MethodConfig;
import org.apache.dubbo.config.ProviderConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.annotation.Method;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author noear
 */
public class DubboAnnotationSupportTest {

    @Test
    public void convertParameters_pairAndKvForms() {
        Map<String, String> expected = new HashMap<>();
        expected.put("a", "b");

        Assertions.assertEquals(expected, DubboAnnotationSupport.convertParameters(new String[]{"a", "b"}));
        Assertions.assertEquals(expected, DubboAnnotationSupport.convertParameters(new String[]{" a ", " b "}));
        Assertions.assertEquals(expected, DubboAnnotationSupport.convertParameters(new String[]{"a=b"}));
        Assertions.assertEquals(expected, DubboAnnotationSupport.convertParameters(new String[]{"a:b"}));

        expected.put("c", "d");
        Assertions.assertEquals(expected, DubboAnnotationSupport.convertParameters(new String[]{"a=b", "c", "d"}));
        Assertions.assertEquals(expected, DubboAnnotationSupport.convertParameters(new String[]{"a:b", "c=d"}));

        expected.clear();
        expected.put("a", "a:b");
        Assertions.assertEquals(expected, DubboAnnotationSupport.convertParameters(new String[]{"a", "a:b"}));

        expected.clear();
        expected.put("a", "0,100");
        Assertions.assertEquals(expected, DubboAnnotationSupport.convertParameters(new String[]{"a", "0,100"}));
    }

    @Test
    public void convertParameters_emptyAndOdd() {
        Assertions.assertTrue(DubboAnnotationSupport.convertParameters(null).isEmpty());
        Assertions.assertTrue(DubboAnnotationSupport.convertParameters(new String[0]).isEmpty());
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> DubboAnnotationSupport.convertParameters(new String[]{"only-key"}));
    }

    @Test
    public void convertMethodConfigs_basic() {
        Method method = method("sayHello", 1000, 0, "leastactive",
                new String[]{"token", "abc", "route=gray"});

        List<MethodConfig> list = DubboAnnotationSupport.convertMethodConfigs(new Method[]{method});
        Assertions.assertEquals(1, list.size());

        MethodConfig mc = list.get(0);
        Assertions.assertEquals("sayHello", mc.getName());
        Assertions.assertEquals(Integer.valueOf(1000), mc.getTimeout());
        Assertions.assertEquals(Integer.valueOf(0), mc.getRetries());
        Assertions.assertEquals("leastactive", mc.getLoadbalance());
        Assertions.assertNotNull(mc.getParameters());
        Assertions.assertEquals("abc", mc.getParameters().get("token"));
        Assertions.assertEquals("gray", mc.getParameters().get("route"));
    }

    @Test
    public void apply_serviceAndReference() {
        Method method = method("echo", 500, -1, "", new String[0]);

        ServiceConfig<?> service = new ServiceConfig<>();
        DubboAnnotationSupport.apply(service,
                new String[]{"k1", "v1", "k2=v2"},
                new Method[]{method});

        Assertions.assertEquals("v1", service.getParameters().get("k1"));
        Assertions.assertEquals("v2", service.getParameters().get("k2"));
        Assertions.assertEquals(1, service.getMethods().size());
        Assertions.assertEquals("echo", service.getMethods().get(0).getName());
        Assertions.assertEquals(Integer.valueOf(500), service.getMethods().get(0).getTimeout());

        ReferenceConfig<?> reference = new ReferenceConfig<>();
        DubboAnnotationSupport.apply(reference,
                new String[]{"tag:gray"},
                new Method[]{method});
        Assertions.assertEquals("gray", reference.getParameters().get("tag"));
        Assertions.assertEquals(1, reference.getMethods().size());
    }

    /**
     * Full construction path must survive k=v / k:v parameters.
     * Without empty parameters()/methods() on Anno wrappers, AbstractConfig.appendAnnotation
     * would call toStringMap and throw "pairs must be even".
     */
    @AfterEach
    public void resetBootstrap() {
        try {
            DubboBootstrap.reset();
        } catch (Throwable ignore) {
            try {
                DubboBootstrap.getInstance().stop();
            } catch (Throwable ignored) {
            }
        }
    }

    @Test
    public void applyProvider_setsProviderIds() {
        ServiceConfig<?> service = new ServiceConfig<>();
        DubboAnnotationSupport.applyProvider(service, "p1");
        Assertions.assertEquals("p1", service.getProviderIds());

        // empty / already set should no-op override
        DubboAnnotationSupport.applyProvider(service, "");
        Assertions.assertEquals("p1", service.getProviderIds());
        DubboAnnotationSupport.applyProvider(service, "p2");
        Assertions.assertEquals("p1", service.getProviderIds());
    }

    @Test
    public void applyConsumer_looksUpNamedConfig() {
        DubboBootstrap bootstrap = DubboBootstrap.getInstance();
        ApplicationConfig application = new ApplicationConfig();
        application.setName("anno-consumer-lookup");
        bootstrap.application(application);

        ConsumerConfig c1 = new ConsumerConfig();
        c1.setId("c1");
        c1.setTimeout(2000);
        c1.setCheck(false);
        bootstrap.consumer(c1);

        ConsumerConfig c2 = new ConsumerConfig();
        c2.setId("c2");
        c2.setTimeout(8000);
        c2.setCheck(false);
        bootstrap.consumer(c2);

        ReferenceConfig<?> ref1 = new ReferenceConfig<>();
        DubboAnnotationSupport.applyConsumer(ref1, "c1");
        Assertions.assertNotNull(ref1.getConsumer());
        Assertions.assertEquals("c1", ref1.getConsumer().getId());
        Assertions.assertEquals(Integer.valueOf(2000), ref1.getConsumer().getTimeout());

        ReferenceConfig<?> ref2 = new ReferenceConfig<>();
        DubboAnnotationSupport.applyConsumer(ref2, "c2");
        Assertions.assertEquals("c2", ref2.getConsumer().getId());
        Assertions.assertEquals(Integer.valueOf(8000), ref2.getConsumer().getTimeout());

        Assertions.assertThrows(IllegalStateException.class,
                () -> DubboAnnotationSupport.applyConsumer(new ReferenceConfig<>(), "missing"));
    }

    @Test
    public void ensureProviderExists_checksNamedConfig() {
        DubboBootstrap bootstrap = DubboBootstrap.getInstance();
        ApplicationConfig application = new ApplicationConfig();
        application.setName("anno-provider-lookup");
        bootstrap.application(application);

        ProviderConfig p1 = new ProviderConfig();
        p1.setId("p1");
        p1.setGroup("g1");
        bootstrap.provider(p1);

        // exists: no throw
        DubboAnnotationSupport.ensureProviderExists("p1");
        // empty: no throw
        DubboAnnotationSupport.ensureProviderExists("");
        Assertions.assertThrows(IllegalStateException.class,
                () -> DubboAnnotationSupport.ensureProviderExists("missing"));
    }

    @Test
    public void construct_viaAnnoWrappers_bypassesStrictToStringMap() {
        Method method = method("sayHello", 1000, 0, "leastactive",
                new String[]{"token", "abc", "route=gray"});

        // Simulate raw annotation values (plugin reads these from real annotation, not from wrapper).
        String[] rawParams = new String[]{"token=abc", "route:gray"};
        Method[] rawMethods = new Method[]{method};

        // Direct construction with raw params would fail in appendAnnotation.toStringMap.
        // Our wrappers must expose empty parameters/methods so construction is safe.
        Assertions.assertArrayEquals(new String[0], new MethodAnno(method).parameters());

        ServiceConfig<?> service = new ServiceConfig<>();
        // apply uses raw annotation arrays (as DubboSolonPlugin does)
        DubboAnnotationSupport.apply(service, rawParams, rawMethods);

        Assertions.assertEquals("abc", service.getParameters().get("token"));
        Assertions.assertEquals("gray", service.getParameters().get("route"));
        Assertions.assertEquals(1, service.getMethods().size());
        Assertions.assertEquals("sayHello", service.getMethods().get(0).getName());
        Assertions.assertEquals(Integer.valueOf(1000), service.getMethods().get(0).getTimeout());
        Assertions.assertEquals("abc", service.getMethods().get(0).getParameters().get("token"));
        Assertions.assertEquals("gray", service.getMethods().get(0).getParameters().get("route"));

        ReferenceConfig<?> reference = new ReferenceConfig<>();
        DubboAnnotationSupport.apply(reference, new String[]{"tag:gray"}, rawMethods);
        Assertions.assertEquals("gray", reference.getParameters().get("tag"));
        Assertions.assertEquals(1, reference.getMethods().size());
        Assertions.assertEquals("sayHello", reference.getMethods().get(0).getName());
    }

    private static Method method(String name, int timeout, int retries, String loadbalance, String[] parameters) {
        return new Method() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public int timeout() {
                return timeout;
            }

            @Override
            public int retries() {
                return retries;
            }

            @Override
            public String loadbalance() {
                return loadbalance;
            }

            @Override
            public boolean async() {
                return false;
            }

            @Override
            public boolean sent() {
                return true;
            }

            @Override
            public int actives() {
                return -1;
            }

            @Override
            public int executes() {
                return -1;
            }

            @Override
            public boolean deprecated() {
                return false;
            }

            @Override
            public boolean sticky() {
                return false;
            }

            @Override
            public boolean isReturn() {
                return true;
            }

            @Override
            public String oninvoke() {
                return "";
            }

            @Override
            public String onreturn() {
                return "";
            }

            @Override
            public String onthrow() {
                return "";
            }

            @Override
            public String cache() {
                return "";
            }

            @Override
            public String validation() {
                return "";
            }

            @Override
            public String merger() {
                return "";
            }

            @Override
            public org.apache.dubbo.config.annotation.Argument[] arguments() {
                return new org.apache.dubbo.config.annotation.Argument[0];
            }

            @Override
            public String[] parameters() {
                return parameters;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return Method.class;
            }
        };
    }
}
