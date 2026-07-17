package org.apache.dubbo.solon;

import org.apache.dubbo.config.ProtocolConfig;
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
}
