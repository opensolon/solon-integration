package org.apache.dubbo.solon.integration;

import org.apache.dubbo.config.*;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.apache.dubbo.solon.Protocols;
import org.apache.dubbo.solon.ReferenceAnno;
import org.apache.dubbo.solon.Registries;
import org.apache.dubbo.solon.ServiceAnno;
import org.apache.dubbo.solon.annotation.EnableDubbo;
import org.noear.solon.Solon;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;


public class DubboSolonPlugin implements Plugin {
    private DubboBootstrap bootstrap;

    @Override
    public void start(AppContext context){
        if (Solon.app().source().getAnnotation(EnableDubbo.class) == null &&
                Solon.app().source().getAnnotation(org.noear.solon.extend.dubbo.EnableDubbo.class) == null) {
            return;
        }

        bootstrap = DubboBootstrap.getInstance();

        this.initialize();
        this.register(context);

        bootstrap.start();
        startBlock();
    }

    private void initialize() {
        // 应用配置
        ApplicationConfig application = Solon.cfg()
                .toBean("dubbo.application", ApplicationConfig.class);
        if (application == null) {
            application = new ApplicationConfig();
        }
        if (application.getName() == null) {
            application.setName(Solon.cfg().appGroup() + "-" + Solon.cfg().appName());
        }

        bootstrap.application(application);


        // 注册中心
        Registries registries = Solon.cfg()
                .toBean("dubbo.registries", Registries.class);
        if (registries != null && registries.size() > 0) {
            bootstrap.registries(registries);
        } else {
            RegistryConfig registry = Solon.cfg()
                    .toBean("dubbo.registry", RegistryConfig.class);
            if (registry == null) {
                registry = new RegistryConfig();
            }
            if (registry.getAddress() == null) {
                registry.setAddress("A/N");
            }

            bootstrap.registry(registry);
        }


        //提供者
        ProviderConfig provider = Solon.cfg()
                .toBean("dubbo.provider", ProviderConfig.class);
        if (provider != null) {
            bootstrap.provider(provider);
        }

        //协议
        Protocols protocols = Solon.cfg()
                .toBean("dubbo.protocols", Protocols.class);
        if (protocols != null && protocols.size() > 0) {
            bootstrap.protocols(protocols);
        } else {
            ProtocolConfig protocol = Solon.cfg()
                    .toBean("dubbo.protocol", ProtocolConfig.class);
            if (protocol == null) {
                protocol = new ProtocolConfig();
            }
            if (protocol.getName() == null) {
                protocol.setName("dubbo");
                int port = Solon.cfg().serverPort() + 20000;
                protocol.setPort(port);
            }


            bootstrap.protocol(protocol);
        }

        //消费者
        ConsumerConfig consumer = Solon.cfg()
                .toBean("dubbo.consumer", ConsumerConfig.class);
        if (consumer != null) {
            bootstrap.consumer(consumer);
        }
    }

    private void register(AppContext context) {
        context.beanBuilderAdd(Service.class, ((clz, bw, anno) -> {
            Class<?>[] interfaces = clz.getInterfaces();

            if (interfaces.length > 0) {
                ServiceConfig<?> config = new ServiceConfig<>(new ServiceAnno(anno));
                if (config.getInterface() == null) {
                    config.setInterface(interfaces[0]);
                }
                config.setRef(bw.get());
                config.export();

                bootstrap.service(config);
            }
        }));

        context.beanInjectorAdd(Reference.class, ((holder, anno) -> {
            holder.required(true);

            if (holder.getType().isInterface()) {
                ReferenceConfig<?> config = new ReferenceConfig<>(new ReferenceAnno(anno));
                config.setInterface(holder.getType());

                // 注册引用
                bootstrap.reference(config);

                holder.setValue(config.get());
            }
        }));
    }

    @Override
    public void prestop() throws Throwable {
        if (bootstrap != null) {
            bootstrap.stop();
            bootstrap = null;
        }
    }

    @Override
    public void stop() {
        stopBlock();
    }


    private Thread blockThread = null;

    public void startBlock() {
        if (blockThread == null) {
            blockThread = new Thread(() -> {
                try {
                    System.in.read();
                } catch (Exception ex) {
                }
            });
            blockThread.start();
        }
    }

    public void stopBlock() {
        if (blockThread != null) {
            blockThread.interrupt();
            blockThread = null;
        }
    }
}
