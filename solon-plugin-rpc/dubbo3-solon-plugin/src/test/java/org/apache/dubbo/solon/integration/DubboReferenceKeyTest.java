package org.apache.dubbo.solon.integration;

import org.apache.dubbo.config.ReferenceConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author noear
 */
public class DubboReferenceKeyTest {

    public interface DemoService {
        String hello();
    }

    @Test
    public void sameParams_sameKey() {
        ReferenceConfig<DemoService> a = new ReferenceConfig<>();
        a.setInterface(DemoService.class);
        a.setGroup("g1");
        a.setVersion("1.0");

        ReferenceConfig<DemoService> b = new ReferenceConfig<>();
        b.setInterface(DemoService.class);
        b.setGroup("g1");
        b.setVersion("1.0");

        Assertions.assertEquals(
                DubboSolonPlugin.buildReferenceKey(a),
                DubboSolonPlugin.buildReferenceKey(b));
    }

    @Test
    public void differentGroup_differentKey() {
        ReferenceConfig<DemoService> a = new ReferenceConfig<>();
        a.setInterface(DemoService.class);
        a.setGroup("g1");

        ReferenceConfig<DemoService> b = new ReferenceConfig<>();
        b.setInterface(DemoService.class);
        b.setGroup("g2");

        Assertions.assertNotEquals(
                DubboSolonPlugin.buildReferenceKey(a),
                DubboSolonPlugin.buildReferenceKey(b));
    }

    @Test
    public void differentFilter_differentKey() {
        ReferenceConfig<DemoService> a = new ReferenceConfig<>();
        a.setInterface(DemoService.class);
        a.setFilter("f1");

        ReferenceConfig<DemoService> b = new ReferenceConfig<>();
        b.setInterface(DemoService.class);
        b.setFilter("f2");

        Assertions.assertNotEquals(
                DubboSolonPlugin.buildReferenceKey(a),
                DubboSolonPlugin.buildReferenceKey(b));
    }

    @Test
    public void differentTag_differentKey() {
        ReferenceConfig<DemoService> a = new ReferenceConfig<>();
        a.setInterface(DemoService.class);
        a.setTag("gray");

        ReferenceConfig<DemoService> b = new ReferenceConfig<>();
        b.setInterface(DemoService.class);
        b.setTag("prod");

        Assertions.assertNotEquals(
                DubboSolonPlugin.buildReferenceKey(a),
                DubboSolonPlugin.buildReferenceKey(b));
    }
}
