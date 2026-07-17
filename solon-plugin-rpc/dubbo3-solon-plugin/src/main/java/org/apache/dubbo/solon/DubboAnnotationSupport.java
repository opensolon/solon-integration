package org.apache.dubbo.solon;

import org.apache.dubbo.config.AbstractInterfaceConfig;
import org.apache.dubbo.config.MethodConfig;
import org.apache.dubbo.config.annotation.Method;
import org.noear.solon.Solon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Align annotation extras with Spring's Dubbo handling:
 * <ul>
 *   <li>{@code parameters} string array → {@code Map} (supports key/value pairs and {@code k=v}/{@code k:v})</li>
 *   <li>{@code methods} → {@link MethodConfig} list</li>
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
