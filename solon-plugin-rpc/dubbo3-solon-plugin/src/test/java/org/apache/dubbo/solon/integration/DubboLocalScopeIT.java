package org.apache.dubbo.solon.integration;

import demo.protocol.HelloService;
import demo.server.HelloServiceImpl;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.apache.dubbo.solon.annotation.EnableDubbo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.noear.solon.Solon;
import org.noear.solon.SolonApp;
import org.noear.solon.annotation.Managed;
import org.noear.solon.core.event.AppPluginLoadEndEvent;

/**
 * Integration: local-scope provider + consumer in one process.
 * Verifies delayed export (bootstrap.start after beanScan) and reference inject.
 *
 * @author noear
 */
public class DubboLocalScopeIT {

    @AfterEach
    public void tearDown() {
        try {
            if (Solon.app() != null) {
                Solon.stopBlock(false, 0);
            }
        } catch (Throwable ignore) {
            // ignore stop noise between tests
        }
        try {
            DubboBootstrap.reset();
        } catch (Throwable ignore) {
            try {
                DubboBootstrap.getInstance().stop();
            } catch (Throwable ignored) {
            }
        }
    }

    @EnableDubbo
    @Managed
    public static class LocalApp {
        @DubboReference(group = "demo", check = false)
        HelloService helloService;
    }

    private static void prepareLocalApp(SolonApp app, String appName) {
        app.enableHttp(false);
        // config must be ready before plugin.start / initialize()
        app.cfg().setProperty("solon.app.group", "it");
        app.cfg().setProperty("solon.app.name", appName);
        app.cfg().setProperty("dubbo.application.name", appName);
        app.cfg().setProperty("dubbo.registry.address", "N/A");
        app.cfg().setProperty("dubbo.protocol.name", "dubbo");
        // random port to avoid bind conflicts across tests
        app.cfg().setProperty("dubbo.protocol.port", "-1");
        app.cfg().setProperty("dubbo.consumer.scope", "local");
        app.cfg().setProperty("dubbo.consumer.check", "false");
        app.cfg().setProperty("dubbo.solon.block", "false");

        // HelloServiceImpl lives in demo.server (outside LocalApp package scan).
        // beanMake after plugin.start so @DubboService beanBuilder is registered.
        app.onEvent(AppPluginLoadEndEvent.class, e -> app.context().beanMake(HelloServiceImpl.class));
    }

    @Test
    public void localScope_providerAndConsumer_ok() {
        Solon.start(LocalApp.class, new String[]{"--server.port=0"}, app -> prepareLocalApp(app, "dubbo-local-it"));

        LocalApp localApp = Solon.context().getBean(LocalApp.class);
        Assertions.assertNotNull(localApp, "LocalApp bean missing");
        Assertions.assertNotNull(localApp.helloService, "HelloService reference not injected");

        String rst = localApp.helloService.sayHello("world");
        Assertions.assertEquals("hello, world", rst);
    }

    @Test
    public void localScope_repeatedCall_stable() {
        Solon.start(LocalApp.class, new String[]{"--server.port=0"}, app -> prepareLocalApp(app, "dubbo-local-it2"));

        LocalApp a = Solon.context().getBean(LocalApp.class);
        Assertions.assertEquals("hello, a", a.helloService.sayHello("a"));
        Assertions.assertEquals("hello, b", a.helloService.sayHello("b"));
    }
}
