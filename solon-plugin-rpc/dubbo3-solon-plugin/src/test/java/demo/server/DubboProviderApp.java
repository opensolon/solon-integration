package demo.server;

import demo.protocol.HelloService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.solon.annotation.EnableDubbo;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Managed;

@EnableDubbo
@Managed
public class DubboProviderApp {
    public static void main(String[] args) throws InterruptedException{
        Solon.start(DubboProviderApp.class, args);

        DubboProviderApp dubboProviderApp = Solon.app().context().getBean(DubboProviderApp.class);
        String rst = dubboProviderApp.helloService.sayHello("world");

        System.out.println(rst);
    }

    @DubboReference(group = "demo")
    HelloService helloService;
}
