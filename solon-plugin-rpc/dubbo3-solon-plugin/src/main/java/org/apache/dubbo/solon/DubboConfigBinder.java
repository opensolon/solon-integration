package org.apache.dubbo.solon;

import org.apache.dubbo.config.AbstractConfig;
import org.noear.solon.core.Props;
import org.noear.solon.core.util.ClassUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Bind Dubbo multi/single configs from Solon Props.
 * <p>
 * Supports Spring Boot style map multi-config:
 * <pre>
 * dubbo.registries.reg1.address=...
 * dubbo.protocols.dubbo.port=20880
 * </pre>
 * and list style:
 * <pre>
 * dubbo.registries[0].address=...
 * dubbo.registries.0.address=...
 * </pre>
 * as well as single config:
 * <pre>
 * dubbo.registry.address=...
 * </pre>
 *
 * @author noear
 * @since 2.9
 */
public final class DubboConfigBinder {
    private DubboConfigBinder() {
    }

    /**
     * Bind multi configs first; if absent, bind single; optionally create a default when both absent.
     * {@code postProcess} is applied to <b>every</b> returned config (including user-bound ones).
     *
     * @param cfg            config source
     * @param multiKey       e.g. dubbo.registries
     * @param singleKey      e.g. dubbo.registry
     * @param type           config type
     * @param createIfAbsent create one empty instance when both keys are empty
     * @param postProcess    applied to each bound/created config (may be null)
     * @return list of configs (may be empty only when createIfAbsent is false and nothing bound)
     */
    public static <T extends AbstractConfig> List<T> bindMultiOrSingle(
            Props cfg,
            String multiKey,
            String singleKey,
            Class<T> type,
            boolean createIfAbsent,
            Consumer<T> postProcess) {

        List<T> list = bindMulti(cfg, multiKey, type);
        if (list.isEmpty()) {
            T single = bindSingle(cfg, singleKey, type);
            if (single != null) {
                list = new ArrayList<>(1);
                list.add(single);
            }
        }

        if (list.isEmpty() && createIfAbsent) {
            list = new ArrayList<>(1);
            list.add(ClassUtil.newInstance(type));
        }

        if (postProcess != null) {
            for (T item : list) {
                postProcess.accept(item);
            }
        }
        return list;
    }

    /**
     * Backward-compatible overload: {@code defaultInit} is used both as create-if-absent marker
     * and as post-process for the created default only. Prefer the full overload for post-process
     * on all configs.
     *
     * @deprecated use {@link #bindMultiOrSingle(Props, String, String, Class, boolean, Consumer)}
     */
    @Deprecated
    public static <T extends AbstractConfig> List<T> bindMultiOrSingle(
            Props cfg,
            String multiKey,
            String singleKey,
            Class<T> type,
            Consumer<T> defaultInit) {
        return bindMultiOrSingle(cfg, multiKey, singleKey, type, defaultInit != null, defaultInit);
    }

    /**
     * Bind named/list multi configs under {@code multiKey}.
     * <p>
     * Numeric list keys ({@code 0}/{@code [0]}) are sorted by index so order is stable.
     * Named map keys keep lexicographic order after index keys.
     * Map key is written to {@link AbstractConfig#setId(String)} when id is empty and key is not numeric.
     */
    public static <T extends AbstractConfig> List<T> bindMulti(Props cfg, String multiKey, Class<T> type) {
        List<T> result = new ArrayList<>();
        if (cfg == null || multiKey == null) {
            return result;
        }

        Map<String, Props> grouped = cfg.getGroupedProp(multiKey);
        if (grouped == null || grouped.isEmpty()) {
            return result;
        }

        List<Map.Entry<String, Props>> entries = new ArrayList<>(grouped.entrySet());
        entries.sort(ENTRY_COMPARATOR);

        for (Map.Entry<String, Props> entry : entries) {
            Props child = entry.getValue();
            if (child == null || child.isEmpty()) {
                continue;
            }

            T config = child.toBean(type);
            if (config == null) {
                continue;
            }

            String groupKey = entry.getKey();
            if (isEmpty(config.getId()) && !isIndexKey(groupKey)) {
                config.setId(groupKey);
            }
            result.add(config);
        }
        return result;
    }

    /**
     * Bind a single config under {@code singleKey}; returns null if empty.
     */
    public static <T extends AbstractConfig> T bindSingle(Props cfg, String singleKey, Class<T> type) {
        if (cfg == null || singleKey == null) {
            return null;
        }
        Props child = cfg.getProp(singleKey);
        if (child == null || child.isEmpty()) {
            return null;
        }
        return child.toBean(type);
    }

    /**
     * Bind optional single config; returns null when absent.
     */
    public static <T extends AbstractConfig> T bindOptional(Props cfg, String key, Class<T> type) {
        return bindSingle(cfg, key, type);
    }

    static boolean isIndexKey(String key) {
        return parseIndex(key) >= 0;
    }

    /**
     * Parse list index from key forms {@code 0} / {@code [0]}. Returns -1 if not an index key.
     */
    static int parseIndex(String key) {
        if (key == null || key.isEmpty()) {
            return -1;
        }
        String raw = key;
        if (raw.charAt(0) == '[' && raw.charAt(raw.length() - 1) == ']') {
            raw = raw.substring(1, raw.length() - 1);
        }
        if (raw.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < raw.length(); i++) {
            if (!Character.isDigit(raw.charAt(i))) {
                return -1;
            }
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private static final Comparator<Map.Entry<String, Props>> ENTRY_COMPARATOR = (a, b) -> {
        int ai = parseIndex(a.getKey());
        int bi = parseIndex(b.getKey());
        boolean aIdx = ai >= 0;
        boolean bIdx = bi >= 0;
        if (aIdx && bIdx) {
            return Integer.compare(ai, bi);
        }
        if (aIdx != bIdx) {
            // keep list-style entries before named map entries for stable mixed cases
            return aIdx ? -1 : 1;
        }
        String ak = a.getKey() == null ? "" : a.getKey();
        String bk = b.getKey() == null ? "" : b.getKey();
        return ak.compareTo(bk);
    };

    private static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
