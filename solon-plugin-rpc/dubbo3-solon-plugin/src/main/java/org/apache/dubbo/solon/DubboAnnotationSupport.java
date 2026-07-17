package org.apache.dubbo.solon;

import org.apache.dubbo.config.AbstractInterfaceConfig;
import org.apache.dubbo.config.ConsumerConfig;
import org.apache.dubbo.config.MethodConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.ProviderConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.annotation.Method;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.apache.dubbo.config.context.ModuleConfigManager;
import org.noear.solon.Solon;
import org.noear.solon.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Align annotation extras with Spring's Dubbo handling:
 * <ul>
 *   <li>{@code parameters} string array → {@code Map} (supports key/value pairs and {@code k=v}/{@code k:v})</li>
 *   <li>{@code methods} → {@link MethodConfig} list</li>
 *   <li>{@code provider}/{@code consumer} name → named multi config (appendAnnotation cannot bind these)</li>
 *   <li>{@code registry[]} → {@code registryIds}; service {@code protocol[]} → {@code protocolIds}</li>
 * </ul>
 * Also resolves Solon config templates ({@code ${...}}) for parameter values and method string fields.
 *
 * @author noear
 * @since 4.0.4
 */
public final class DubboAnnotationSupport {
    private DubboAnnotationSupport() {
    }

    /**
     * Apply annotation {@code parameters}/{@code methods} onto a service or reference config.
     * Safe to call when arrays are empty (no-op).
     */
    public static void apply(AbstractInterfaceConfig config, String[] parameters, Method[] methods) {
        if (config == null) {
            return;
        }

        Map<String, String> map = convertParameters(parameters);
        if (!map.isEmpty()) {
            config.setParameters(map);
        }

        List<MethodConfig> methodConfigs = convertMethodConfigs(methods);
        if (!methodConfigs.isEmpty()) {
            config.setMethods(methodConfigs);
        }
    }

    /**
     * Resolve {@code @DubboService(provider="id")} like Spring:
     * annotation attribute {@code provider} → {@code ServiceConfig.providerIds},
     * then Dubbo {@code convertProviderIdToProvider()} looks it up at refresh time.
     */
    public static void applyProvider(ServiceConfig<?> config, String providerId) {
        if (config == null || Utils.isEmpty(providerId)) {
            return;
        }
        // Prefer providerIds (String). setProvider(ProviderConfig) is a different path.
        if (Utils.isEmpty(config.getProviderIds())) {
            config.setProviderIds(providerId);
        }
    }

    /**
     * Resolve {@code @DubboReference(consumer="id")} like Spring ReferenceCreator:
     * look up named {@link ConsumerConfig} from ModuleConfigManager and set it.
     * <p>
     * Note: Dubbo {@code appendAnnotation} cannot bind annotation {@code consumer} string
     * (no {@code setConsumer(String)}); ReferenceConfig also has no {@code consumerIds} field,
     * so lookup must happen here before refer/export.
     */
    public static void applyConsumer(ReferenceConfig<?> config, String consumerId) {
        if (config == null || Utils.isEmpty(consumerId)) {
            return;
        }
        if (config.getConsumer() != null) {
            return;
        }

        ModuleConfigManager manager = moduleConfigManager();
        Optional<ConsumerConfig> found = manager.getConsumer(consumerId);
        if (!found.isPresent()) {
            throw new IllegalStateException("Consumer config not found: " + consumerId
                    + " (define dubbo.consumers." + consumerId + ".* or dubbo.consumer with id)");
        }
        config.setConsumer(found.get());
    }

    /**
     * Optional helper: verify provider id exists early (fail-fast). Provider resolution itself
     * still goes through providerIds at ServiceConfig refresh.
     */
    public static void ensureProviderExists(String providerId) {
        if (Utils.isEmpty(providerId)) {
            return;
        }
        ModuleConfigManager manager = moduleConfigManager();
        Optional<ProviderConfig> found = manager.getProvider(providerId);
        if (!found.isPresent()) {
            throw new IllegalStateException("Provider config not found: " + providerId
                    + " (define dubbo.providers." + providerId + ".* or dubbo.provider with id)");
        }
    }

    /**
     * Resolve {@code @DubboService(registry={"reg1","reg2"})} / {@code @DubboReference(registry=...)
     * like Spring: annotation array → comma-joined {@code registryIds}.
     * {@code appendAnnotation} cannot bind {@code registry[]} (no {@code setRegistry(String[])}).
     */
    public static void applyRegistries(AbstractInterfaceConfig config, String[] registryIds) {
        if (config == null) {
            return;
        }
        if (Utils.isNotEmpty(config.getRegistryIds())) {
            return;
        }
        String joined = joinIds(registryIds);
        if (Utils.isEmpty(joined)) {
            return;
        }
        config.setRegistryIds(joined);
    }
    
    /**
     * Resolve {@code @DubboService(protocol={"dubbo","tri"})} like Spring:
     * annotation array → comma-joined {@code protocolIds}.
     * Only service side has {@code protocolIds}; reference uses string {@code protocol} instead.
     */
    public static void applyProtocols(ServiceConfig<?> config, String[] protocolIds) {
        if (config == null) {
            return;
        }
        if (Utils.isNotEmpty(config.getProtocolIds())) {
            return;
        }
        String joined = joinIds(protocolIds);
        if (Utils.isEmpty(joined)) {
            return;
        }
        config.setProtocolIds(joined);
    }
    
    /**
     * Fail-fast: each resolved registry id must exist in ConfigManager.
     */
    public static void ensureRegistriesExist(String[] registryIds) {
        if (registryIds == null || registryIds.length == 0) {
            return;
        }
        ModuleConfigManager manager = moduleConfigManager();
        for (String raw : registryIds) {
            if (raw == null) {
                continue;
            }
            String id = resolveTmpl(raw.trim());
            if (Utils.isEmpty(id)) {
                continue;
            }
            Optional<RegistryConfig> found = manager.getRegistry(id);
            if (!found.isPresent()) {
                throw new IllegalStateException("Registry config not found: " + id
                        + " (define dubbo.registries." + id + ".* or dubbo.registry with id)");
            }
        }
    }
    
    /**
     * Fail-fast: each resolved protocol id/name must exist in ConfigManager.
     */
    public static void ensureProtocolsExist(String[] protocolIds) {
        if (protocolIds == null || protocolIds.length == 0) {
            return;
        }
        ModuleConfigManager manager = moduleConfigManager();
        for (String raw : protocolIds) {
            if (raw == null) {
                continue;
            }
            String id = resolveTmpl(raw.trim());
            if (Utils.isEmpty(id)) {
                continue;
            }
            Optional<ProtocolConfig> found = manager.getProtocol(id);
            if (!found.isPresent()) {
                throw new IllegalStateException("Protocol config not found: " + id
                        + " (define dubbo.protocols." + id + ".* or dubbo.protocol with id)");
            }
        }
    }
    
    /**
     * Join annotation id arrays like Spring {@code StringUtils.join(ids, ',')},
     * resolving each element with Solon templates and skipping blanks.
     */
    public static String joinIds(String[] ids) {
        if (ids == null || ids.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String raw : ids) {
            if (raw == null) {
                continue;
            }
            String id = resolveTmpl(raw.trim());
            if (Utils.isEmpty(id)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(id);
        }
        return sb.length() == 0 ? null : sb.toString();
    }
    
    private static ModuleConfigManager moduleConfigManager() {
        return DubboBootstrap.getInstance()
                .getApplicationModel()
                .getDefaultModule()
                .getConfigManager();
    }

    /**
     * Convert annotation parameters to map.
     * Compatible with Spring {@code DubboAnnotationUtils.convertParameters}:
     * <pre>
     * ["a","b"]      → {a=b}
     * [" a "," b "]  → {a=b}
     * ["a=b"]        → {a=b}
     * ["a:b"]        → {a=b}
     * ["a=b","c","d"]→ {a=b,c=d}
     * ["a","a:b"]    → {a=a:b}
     * </pre>
     * Values (and keys) are resolved with {@code Solon.cfg().getByTmpl} when Solon is available.
     */
    public static Map<String, String> convertParameters(String[] parameters) {
        if (parameters == null || parameters.length == 0) {
            return Collections.emptyMap();
        }

        List<String> pairs = new ArrayList<>(parameters.length);
        for (String raw : parameters) {
            if (raw == null) {
                continue;
            }
            String parameter = raw.trim();
            if (parameter.isEmpty()) {
                // still keep position for pair alignment? Spring trims then may produce empty keys.
                // skip pure empty after trim to avoid garbage; pair-mode empty is rare.
            }

            // value slot (odd size so far): never split
            if (pairs.size() % 2 == 1) {
                pairs.add(parameter);
                continue;
            }

            // key slot: allow "k=v" / "k:v" only when split yields even non-empty parts of length 2+ even
            String[] byColon = parameter.split(":");
            if (byColon.length > 0 && byColon.length % 2 == 0) {
                for (String p : byColon) {
                    pairs.add(p.trim());
                }
                continue;
            }
            String[] byEq = parameter.split("=");
            if (byEq.length > 0 && byEq.length % 2 == 0) {
                for (String p : byEq) {
                    pairs.add(p.trim());
                }
                continue;
            }
            pairs.add(parameter);
        }

        if (pairs.isEmpty()) {
            return Collections.emptyMap();
        }
        if (pairs.size() % 2 != 0) {
            throw new IllegalArgumentException(
                    "dubbo annotation parameters must resolve to even key/value pairs, got: "
                            + pairs);
        }

        Map<String, String> map = new LinkedHashMap<>(pairs.size() / 2);
        for (int i = 0; i < pairs.size(); i += 2) {
            String key = resolveTmpl(pairs.get(i));
            String value = resolveTmpl(pairs.get(i + 1));
            map.put(key, value);
        }
        return map;
    }

    /**
     * Convert {@link Method} annotations to {@link MethodConfig} list.
     * String fields support Solon templates via {@link MethodAnno}.
     * Method-level {@code parameters} use the same flexible conversion as service/reference.
     */
    public static List<MethodConfig> convertMethodConfigs(Method[] methods) {
        if (methods == null || methods.length == 0) {
            return Collections.emptyList();
        }

        List<MethodConfig> list = new ArrayList<>(methods.length);
        for (Method method : methods) {
            if (method == null) {
                continue;
            }
            // MethodAnno resolves ${...} on string attributes; construct via MethodConfig(Method)
            MethodConfig methodConfig = new MethodConfig(new MethodAnno(method));
            // Override parameters with flexible converter (appendAnnotation only supports even pairs)
            Map<String, String> methodParams = convertParameters(method.parameters());
            if (!methodParams.isEmpty()) {
                methodConfig.setParameters(methodParams);
            }
            list.add(methodConfig);
        }
        return list;
    }

    private static String resolveTmpl(String text) {
        if (text == null) {
            return null;
        }
        if (Solon.cfg() == null) {
            return text;
        }
        return Solon.cfg().getByTmpl(text);
    }
}
