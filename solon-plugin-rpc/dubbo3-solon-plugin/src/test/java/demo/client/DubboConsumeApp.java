package demo.client;

import demo.protocol.HelloService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.solon.annotation.EnableDubbo;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Managed;
import org.noear.solon.core.util.MultiMap;

/**
 * Dubbo Consumer demo.
 * <p>
 * Connects to {@link demo.server.DubboProviderApp} via direct URL
 * (no registry required). Start the provider first.
 * <p>
 * Config: {@code classpath:demo-client.yml}
 */
@EnableDubbo
@Managed
public class DubboConsumeApp {

    /**
     * Direct connect to provider. group/url come from demo-client.yml.
     */
    @DubboReference(group = "${demo.hello.group}", url = "${demo.hello.url}", check = false)
    HelloService helloService;

    public static void main(String[] args) {
        Solon.start(DubboConsumeApp.class, MultiMap.from(args).then(e -> {
            e.put("cfg", "demo-client.yml");
        }), app -> {
            app.enableHttp(false);
        });

        DubboConsumeApp app = Solon.context().getBean(DubboConsumeApp.class);
        if (app == null || app.helloService == null) {
            throw new IllegalStateException("HelloService reference was not injected. Is provider running?");
        }

        String rst = app.helloService.sayHello("noear");
        System.out.println("==================================================");
        System.out.println(" config : classpath:demo-client.yml");
        System.out.println(" call HelloService.sayHello(\"noear\")");
        System.out.println(" result = " + rst);
        System.out.println("==================================================");

        // exit cleanly after one call
        Solon.stopBlock(false, 0);
    }
}