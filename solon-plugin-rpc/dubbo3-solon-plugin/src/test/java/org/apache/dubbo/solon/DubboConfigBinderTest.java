package org.apache.dubbo.solon;

import org.apache.dubbo.config.ConsumerConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.ProviderConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.noear.solon.core.Props;

import java.util.List;
import java.util.Properties;

/**
 * @author noear
 */
public class DubboConfigBinderTest {

    @Test
    public void bind_mapStyle_registries() {
        Properties p = new Properties();
        p.setProperty("dubbo.registries.reg1.address", "zookeeper://127.0.0.1:2181");
        p.setProperty("dubbo.registries.reg2.address", "nacos://127.0.0.1:8848");
        Props cfg = Props.from(p);

        List<RegistryConfig> list = DubboConfigBinder.bindMulti(cfg, "dubbo.registries", RegistryConfig.class);
        Assertions.assertEquals(2, list.size());

        RegistryConfig reg1 = list.stream().filter(r -> "reg1".equals(r.getId())).findFirst().orElse(null);
        RegistryConfig reg2 = list.stream().filter(r -> "reg2".equals(r.getId())).findFirst().orElse(null);
        Assertions.assertNotNull(reg1);
        Assertions.assertNotNull(reg2);
        Assertions.assertEquals("zookeeper://127.0.0.1:2181", reg1.getAddress());
        Assertions.assertEquals("nacos://127.0.0.1:8848", reg2.getAddress());
    }

    @Test
    public void bind_listStyle_protocols_orderStable() {
        // reverse insertion order intentionally
        Properties p = new Properties();
        p.setProperty("dubbo.protocols.1.name", "tri");
        p.setProperty("dubbo.protocols.1.port", "50051");
        p.setProperty("dubbo.protocols.0.name", "dubbo");
        p.setProperty("dubbo.protocols.0.port", "20880");
        Props cfg = Props.from(p);

        // run multiple times to catch HashMap iteration flakiness if sort is missing
        for (int i = 0; i < 20; i++) {
            List<ProtocolConfig> list = DubboConfigBinder.bindMulti(cfg, "dubbo.protocols", ProtocolConfig.class);
            Assertions.assertEquals(2, list.size());
            Assertions.assertEquals("dubbo", list.get(0).getName());
            Assertions.assertEquals(Integer.valueOf(20880), list.get(0).getPort());
            Assertions.assertEquals("tri", list.get(1).getName());
            Assertions.assertEquals(Integer.valueOf(50051), list.get(1).getPort());
        }
    }

    @Test
    public void bind_single_registry_fallback() {
        Properties p = new Properties();
        p.setProperty("dubbo.registry.address", "N/A");
        Props cfg = Props.from(p);

        List<RegistryConfig> list = DubboConfigBinder.bindMultiOrSingle(
                cfg, "dubbo.registries", "dubbo.registry", RegistryConfig.class, false, null);
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals("N/A", list.get(0).getAddress());
    }

    @Test
    public void bind_default_when_absent() {
        Props cfg = new Props();
        List<RegistryConfig> list = DubboConfigBinder.bindMultiOrSingle(
                cfg, "dubbo.registries", "dubbo.registry", RegistryConfig.class, true,
                r -> {
                    if (r.getAddress() == null) {
                        r.setAddress("N/A");
                    }
                });
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals("N/A", list.get(0).getAddress());
    }

    @Test
    public void bind_postProcess_fillsMissingDefaultsOnUserConfig() {
        Properties p = new Properties();
        // user only set port, name should be filled by postProcess
        p.setProperty("dubbo.protocol.port", "20880");
        Props cfg = Props.from(p);

        List<ProtocolConfig> list = DubboConfigBinder.bindMultiOrSingle(
                cfg, "dubbo.protocols", "dubbo.protocol", ProtocolConfig.class, true,
                protocol -> {
                    if (protocol.getName() == null) {
                        protocol.setName("dubbo");
                    }
                    if (protocol.getPort() == null) {
                        protocol.setPort(30000);
                    }
                });
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals("dubbo", list.get(0).getName());
        Assertions.assertEquals(Integer.valueOf(20880), list.get(0).getPort());
    }

    @Test
    public void bind_postProcess_fillsMissingRegistryAddress() {
        Properties p = new Properties();
        p.setProperty("dubbo.registry.username", "demo");
        Props cfg = Props.from(p);

        List<RegistryConfig> list = DubboConfigBinder.bindMultiOrSingle(
                cfg, "dubbo.registries", "dubbo.registry", RegistryConfig.class, true,
                r -> {
                    if (r.getAddress() == null) {
                        r.setAddress("N/A");
                    }
                });
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals("N/A", list.get(0).getAddress());
        Assertions.assertEquals("demo", list.get(0).getUsername());
    }

    @Test
    public void isIndexKey() {
        Assertions.assertTrue(DubboConfigBinder.isIndexKey("0"));
        Assertions.assertTrue(DubboConfigBinder.isIndexKey("[1]"));
        Assertions.assertFalse(DubboConfigBinder.isIndexKey("reg1"));
        Assertions.assertFalse(DubboConfigBinder.isIndexKey("dubbo"));
        Assertions.assertEquals(0, DubboConfigBinder.parseIndex("0"));
        Assertions.assertEquals(1, DubboConfigBinder.parseIndex("[1]"));
        Assertions.assertEquals(-1, DubboConfigBinder.parseIndex("reg1"));
    }

    @Test
    public void bind_mapStyle_providers_and_consumers() {
        Properties p = new Properties();
        p.setProperty("dubbo.providers.p1.group", "g1");
        p.setProperty("dubbo.providers.p1.timeout", "3000");
        p.setProperty("dubbo.providers.p2.group", "g2");
        p.setProperty("dubbo.providers.p2.timeout", "5000");
        p.setProperty("dubbo.consumers.c1.check", "false");
        p.setProperty("dubbo.consumers.c1.timeout", "2000");
        p.setProperty("dubbo.consumers.c2.check", "false");
        p.setProperty("dubbo.consumers.c2.timeout", "8000");
        Props cfg = Props.from(p);

        List<ProviderConfig> providers = DubboConfigBinder.bindMulti(cfg, "dubbo.providers", ProviderConfig.class);
        Assertions.assertEquals(2, providers.size());
        ProviderConfig p1 = providers.stream().filter(x -> "p1".equals(x.getId())).findFirst().orElse(null);
        ProviderConfig p2 = providers.stream().filter(x -> "p2".equals(x.getId())).findFirst().orElse(null);
        Assertions.assertNotNull(p1);
        Assertions.assertNotNull(p2);
        Assertions.assertEquals("g1", p1.getGroup());
        Assertions.assertEquals(Integer.valueOf(3000), p1.getTimeout());
        Assertions.assertEquals("g2", p2.getGroup());
        Assertions.assertEquals(Integer.valueOf(5000), p2.getTimeout());

        List<ConsumerConfig> consumers = DubboConfigBinder.bindMulti(cfg, "dubbo.consumers", ConsumerConfig.class);
        Assertions.assertEquals(2, consumers.size());
        ConsumerConfig c1 = consumers.stream().filter(x -> "c1".equals(x.getId())).findFirst().orElse(null);
        ConsumerConfig c2 = consumers.stream().filter(x -> "c2".equals(x.getId())).findFirst().orElse(null);
        Assertions.assertNotNull(c1);
        Assertions.assertNotNull(c2);
        Assertions.assertEquals(Boolean.FALSE, c1.isCheck());
        Assertions.assertEquals(Integer.valueOf(2000), c1.getTimeout());
        Assertions.assertEquals(Integer.valueOf(8000), c2.getTimeout());
    }

    @Test
    public void bind_single_provider_consumer_fallback() {
        Properties p = new Properties();
        p.setProperty("dubbo.provider.group", "demo");
        p.setProperty("dubbo.consumer.check", "false");
        p.setProperty("dubbo.consumer.timeout", "3000");
        Props cfg = Props.from(p);

        List<ProviderConfig> providers = DubboConfigBinder.bindMultiOrSingle(
                cfg, "dubbo.providers", "dubbo.provider", ProviderConfig.class, false, null);
        Assertions.assertEquals(1, providers.size());
        Assertions.assertEquals("demo", providers.get(0).getGroup());

        List<ConsumerConfig> consumers = DubboConfigBinder.bindMultiOrSingle(
                cfg, "dubbo.consumers", "dubbo.consumer", ConsumerConfig.class, false, null);
        Assertions.assertEquals(1, consumers.size());
        Assertions.assertEquals(Boolean.FALSE, consumers.get(0).isCheck());
        Assertions.assertEquals(Integer.valueOf(3000), consumers.get(0).getTimeout());
    }

    @Test
    public void bind_multi_providers_preferred_over_single() {
        Properties p = new Properties();
        p.setProperty("dubbo.provider.group", "single-group");
        p.setProperty("dubbo.providers.p1.group", "multi-group");
        Props cfg = Props.from(p);

        List<ProviderConfig> providers = DubboConfigBinder.bindMultiOrSingle(
                cfg, "dubbo.providers", "dubbo.provider", ProviderConfig.class, false, null);
        Assertions.assertEquals(1, providers.size());
        Assertions.assertEquals("p1", providers.get(0).getId());
        Assertions.assertEquals("multi-group", providers.get(0).getGroup());
    }
}
