package org.apache.dubbo.solon.integration;

import org.apache.dubbo.config.*;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;

import org.apache.dubbo.solon.*;
import org.apache.dubbo.solon.annotation.EnableDubbo;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;
import org.noear.solon.core.Props;
import org.noear.solon.core.runtime.NativeDetector;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Dubbo3 plugin for Solon.
 *
 * @author iYing
 * @author noear
 * @since 1.9
 */
public class DubboSolonPlugin implements Plugin {
    private DubboBootstrap bootstrap;
    private final Map<String, Object> referenceCache = new ConcurrentHashMap<>();

    private Thread blockThread;
    private CountDownLatch blockLatch;

    @Override
    public void start(AppContext context) {
        if (Solon.app().source().getAnnotation(EnableDubbo.class) == null) {
            return;
        }

        bootstrap = DubboBootstrap.getInstance();

        this.initialize(context);
        this.register(context);

        // Solon order: plugin.start -> beanScan (service beans registered) -> context.start (lifecycle + inject).
        // Export must happen after beanScan, so bootstrap.start is deferred to lifecycle (before inject review).
        context.lifecycle(() -> {
            bootstrap.start();
            startBlock();
        });
    }

    private void initialize(AppContext context) {
        Props cfg = Solon.cfg();

        // application
        ApplicationConfig application = DubboConfigBinder.bindOptional(cfg, "dubbo.application", ApplicationConfig.class);
        if (application == null) {
            application = new ApplicationConfig();
        }
        if (application.getName() == null) {
            application.setName(Solon.cfg().appGroup() + "-" + Solon.cfg().appName());
        }
        bootstrap.application(application);

        // registries (map / list / single) — post-process fills address=N/A when missing
        List<RegistryConfig> registries = DubboConfigBinder.bindMultiOrSingle(
                cfg,
                "dubbo.registries",
                "dubbo.registry",
                RegistryConfig.class,
                true,
                registry -> {
                    if (registry.getAddress() == null) {
                        registry.setAddress("N/A");
                    }
                });
        if (registries.size() == 1) {
            bootstrap.registry(registries.get(0));
        } else if (registries.size() > 1) {
            bootstrap.registries(registries);
        }

        // providers (map / list / single) — map key becomes id for @DubboService(provider="...")
        List<ProviderConfig> providers = DubboConfigBinder.bindMultiOrSingle(
                cfg,
                "dubbo.providers",
                "dubbo.provider",
                ProviderConfig.class,
                false,
                null);
        if (providers.size() == 1) {
            bootstrap.provider(providers.get(0));
        } else if (providers.size() > 1) {
            bootstrap.providers(providers);
        }

        // protocols (map / list / single) — post-process fills name/port when missing
        List<ProtocolConfig> protocols = DubboConfigBinder.bindMultiOrSingle(
                cfg,
                "dubbo.protocols",
                "dubbo.protocol",
                ProtocolConfig.class,
                true,
                protocol -> {
                    if (protocol.getName() == null) {
                        protocol.setName("dubbo");
                    }
                    if (protocol.getPort() == null) {
                        protocol.setPort(Solon.cfg().serverPort() + 20000);
                    }
                });
        if (protocols.size() == 1) {
            bootstrap.protocol(protocols.get(0));
        } else if (protocols.size() > 1) {
            bootstrap.protocols(protocols);
        }

        // consumers (map / list / single) — map key becomes id for @DubboReference(consumer="...")
        List<ConsumerConfig> consumers = DubboConfigBinder.bindMultiOrSingle(
                cfg,
                "dubbo.consumers",
                "dubbo.consumer",
                ConsumerConfig.class,
                false,
                null);
        if (consumers.size() == 1) {
            bootstrap.consumer(consumers.get(0));
        } else if (consumers.size() > 1) {
            bootstrap.consumers(consumers);
        }

        // monitor (optional)
        MonitorConfig monitor = DubboConfigBinder.bindOptional(cfg, "dubbo.monitor", MonitorConfig.class);
        if (monitor != null) {
            bootstrap.monitor(monitor);
        }

        // config-center (optional)
        ConfigCenterConfig configCenter = DubboConfigBinder.bindOptional(cfg, "dubbo.config-center", ConfigCenterConfig.class);
        if (configCenter != null) {
            bootstrap.configCenter(configCenter);
        }

        // metadata-report (optional)
        MetadataReportConfig metadataReport = DubboConfigBinder.bindOptional(cfg, "dubbo.metadata-report", MetadataReportConfig.class);
        if (metadataReport != null) {
            bootstrap.metadataReport(metadataReport);
        }

        // ssl (optional)
        SslConfig ssl = DubboConfigBinder.bindOptional(cfg, "dubbo.ssl", SslConfig.class);
        if (ssl != null) {
            bootstrap.ssl(ssl);
        }
    }

    private void register(AppContext context) {
        context.beanBuilderAdd(DubboService.class, ((clz, bw, anno) -> {
            DubboServiceAnno serviceAnno = new DubboServiceAnno(anno);
            ServiceConfig<?> config = new ServiceConfig<>(serviceAnno);
            ensureServiceInterface(config, anno.interfaceClass(), anno.interfaceName(), clz);
            // parameters / methods: align with Spring (appendAnnotation alone is not enough)
            DubboAnnotationSupport.apply(config, anno.parameters(), anno.methods());
            // provider="id" → providerIds (appendAnnotation cannot bind String provider)
            String providerId = serviceAnno.provider();
            DubboAnnotationSupport.ensureProviderExists(providerId);
            DubboAnnotationSupport.applyProvider(config, providerId);
            // registry[] / protocol[] → registryIds / protocolIds
            DubboAnnotationSupport.ensureRegistriesExist(anno.registry());
            DubboAnnotationSupport.applyRegistries(config, anno.registry());
            DubboAnnotationSupport.ensureProtocolsExist(anno.protocol());
            DubboAnnotationSupport.applyProtocols(config, anno.protocol());
            config.setRef(bw.get());
            // do NOT export here; bootstrap.start() will export
            bootstrap.service(config);
        }));

        context.beanInjectorAdd(DubboReference.class, ((holder, anno) -> {
            holder.required(true);

            if (holder.getType().isInterface()) {
                if (NativeDetector.isAotRuntime()) {
                    for (Method m : holder.getType().getMethods()) {
                        holder.context().methodWrap(holder.getType(), m);
                    }
                }

                DubboReferenceAnno referenceAnno = new DubboReferenceAnno(anno);
                ReferenceConfig<?> config = new ReferenceConfig<>(referenceAnno);
                config.setInterface(holder.getType());
                DubboAnnotationSupport.apply(config, anno.parameters(), anno.methods());
                // consumer="id" → lookup ConsumerConfig (no setConsumer(String) in Dubbo)
                DubboAnnotationSupport.applyConsumer(config, referenceAnno.consumer());
                // registry[] → registryIds
                DubboAnnotationSupport.ensureRegistriesExist(anno.registry());
                DubboAnnotationSupport.applyRegistries(config, anno.registry());
                holder.setValue(refer(config));
            }
        }));

        // compatible with legacy annotations
        context.beanBuilderAdd(Service.class, ((clz, bw, anno) -> {
            ServiceAnno serviceAnno = new ServiceAnno(anno);
            ServiceConfig<?> config = new ServiceConfig<>(serviceAnno);
            ensureServiceInterface(config, anno.interfaceClass(), anno.interfaceName(), clz);
            DubboAnnotationSupport.apply(config, anno.parameters(), anno.methods());
            String providerId = serviceAnno.provider();
            DubboAnnotationSupport.ensureProviderExists(providerId);
            DubboAnnotationSupport.applyProvider(config, providerId);
            DubboAnnotationSupport.ensureRegistriesExist(anno.registry());
            DubboAnnotationSupport.applyRegistries(config, anno.registry());
            DubboAnnotationSupport.ensureProtocolsExist(anno.protocol());
            DubboAnnotationSupport.applyProtocols(config, anno.protocol());
            config.setRef(bw.get());
            bootstrap.service(config);
        }));

        context.beanInjectorAdd(Reference.class, ((holder, anno) -> {
            holder.required(true);

            if (holder.getType().isInterface()) {
                if (NativeDetector.isAotRuntime()) {
                    for (Method m : holder.getType().getMethods()) {
                        holder.context().methodWrap(holder.getType(), m);
                    }
                }

                ReferenceAnno referenceAnno = new ReferenceAnno(anno);
                ReferenceConfig<?> config = new ReferenceConfig<>(referenceAnno);
                config.setInterface(holder.getType());
                DubboAnnotationSupport.apply(config, anno.parameters(), anno.methods());
                DubboAnnotationSupport.applyConsumer(config, referenceAnno.consumer());
                DubboAnnotationSupport.ensureRegistriesExist(anno.registry());
                DubboAnnotationSupport.applyRegistries(config, anno.registry());
                holder.setValue(refer(config));
            }
        }));
    }

    private void ensureServiceInterface(ServiceConfig<?> config,
                                        Class<?> interfaceClass,
                                        String interfaceName,
                                        Class<?> implClass) {
        // Prefer explicit annotation resolution so interfaceClass/interfaceName always win.
        Class<?> iface = DubboServiceInterfaceResolver.resolve(interfaceClass, interfaceName, implClass);
        // Only set when ServiceConfig did not already get a concrete interface from annotation append.
        if (Utils.isEmpty(config.getInterface())) {
            config.setInterface(iface);
        }
    }

    private Object refer(ReferenceConfig<?> config) {
        String key = buildReferenceKey(config);
        return referenceCache.computeIfAbsent(key, k -> {
            bootstrap.reference(config);
            return config.get();
        });
    }

    static String buildReferenceKey(ReferenceConfig<?> config) {
        String consumerId = null;
        if (config.getConsumer() != null) {
            consumerId = config.getConsumer().getId();
        }

        StringBuilder sb = new StringBuilder(160);
        sb.append(nullToEmpty(config.getInterface())).append('|')
                .append(nullToEmpty(config.getGroup())).append('|')
                .append(nullToEmpty(config.getVersion())).append('|')
                .append(nullToEmpty(config.getUrl())).append('|')
                .append(nullToEmpty(config.getProtocol())).append('|')
                .append(nullToEmpty(config.getScope())).append('|')
                .append(nullToEmpty(consumerId)).append('|')
                .append(nullToEmpty(config.getStub())).append('|')
                .append(nullToEmpty(config.getMock())).append('|')
                .append(config.isCheck()).append('|')
                .append(nullToEmpty(config.getTimeout())).append('|')
                .append(nullToEmpty(config.getRegistryIds())).append('|')
                .append(nullToEmpty(config.getTag())).append('|')
                .append(nullToEmpty(config.getFilter())).append('|')
                .append(nullToEmpty(config.getListener())).append('|')
                .append(nullToEmpty(config.getLoadbalance())).append('|')
                .append(nullToEmpty(config.getCluster())).append('|')
                .append(nullToEmpty(config.getRetries()));
        return sb.toString();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String nullToEmpty(Integer i) {
        return i == null ? "" : String.valueOf(i);
    }

    @Override
    public void preStop() throws Throwable {
        referenceCache.clear();
        if (bootstrap != null) {
            bootstrap.stop();
            bootstrap = null;
        }
    }

    @Override
    public void stop() {
        stopBlock();
    }

    /**
     * Keep process alive for pure Dubbo provider processes that do not start an HTTP server.
     * <p>
     * Decision order:
     * <ol>
     *   <li>{@code dubbo.solon.block=true/false} explicit override</li>
     *   <li>if HTTP is disabled ({@code enableHttp=false}) → block</li>
     *   <li>otherwise do not block (HTTP server thread keeps the process alive)</li>
     * </ol>
     * Note: Solon defaults {@code enableHttp=true}, so pure providers should either set
     * {@code app.enableHttp(false)} / {@code solon.app.enableHttp=false}, or set
     * {@code dubbo.solon.block=true}.
     */
    public void startBlock() {
        if (!shouldBlock()) {
            return;
        }
        if (blockThread == null) {
            blockLatch = new CountDownLatch(1);
            final CountDownLatch latch = blockLatch;
            blockThread = new Thread(() -> {
                try {
                    latch.await();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }, "dubbo-solon-block");
            blockThread.setDaemon(false);
            blockThread.start();
        }
    }

    static boolean shouldBlock() {
        // explicit override: only when key is present
        String configured = Solon.cfg().get("dubbo.solon.block");
        if (configured != null && !configured.isEmpty()) {
            return Boolean.parseBoolean(configured);
        }
        // when HTTP is explicitly disabled, pure provider needs a keep-alive thread
        if (Solon.app() != null && !Solon.app().enableHttp()) {
            return true;
        }
        // HTTP enabled (default): rely on HTTP server thread
        return false;
    }

    public void stopBlock() {
        if (blockLatch != null) {
            blockLatch.countDown();
            blockLatch = null;
        }
        if (blockThread != null) {
            blockThread.interrupt();
            blockThread = null;
        }
    }
}
