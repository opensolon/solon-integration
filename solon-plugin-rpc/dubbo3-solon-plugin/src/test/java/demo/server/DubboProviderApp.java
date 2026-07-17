package demo.server;

import org.apache.dubbo.solon.annotation.EnableDubbo;
import org.noear.solon.Solon;
import org.noear.solon.core.util.MultiMap;

/**
 * Dubbo Provider demo.
 * <p>
 * Starts pure Dubbo service process (no HTTP) and keeps it alive so
 * {@link demo.client.DubboConsumeApp} can call it in another JVM.
 * <p>
 * Config: {@code classpath:demo-server.yml}
 * <p>
 * Run order:
 * <ol>
 *   <li>Start this class first</li>
 *   <li>Then start {@code DubboConsumeApp}</li>
 * </ol>
 */
@EnableDubbo
public class DubboProviderApp {
    public static void main(String[] args) {
        Solon.start(DubboProviderApp.class, MultiMap.from(args).then(e -> {
            e.put("cfg", "demo-server.yml");
        }), app -> {
            // pure provider: disable HTTP so plugin keep-alive thread holds the process
            app.enableHttp(false);
        });

        System.out.println("==================================================");
        System.out.println(" Dubbo provider is running");
        System.out.println(" config : classpath:demo-server.yml");
        System.out.println(" export : dubbo://127.0.0.1:20880");
        System.out.println(" service: demo.protocol.HelloService (group=demo)");
        System.out.println(" next   : start demo.client.DubboConsumeApp");
        System.out.println("==================================================");
    }
}
