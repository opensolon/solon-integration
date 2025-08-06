package demo.server;

import demo.protocol.HelloService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Managed;
import org.noear.solon.extend.dubbo3.EnableDubbo;

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
