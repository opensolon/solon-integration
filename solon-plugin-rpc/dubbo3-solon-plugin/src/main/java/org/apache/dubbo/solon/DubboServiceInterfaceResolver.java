package org.apache.dubbo.solon;

import org.noear.solon.Utils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Resolve the service interface for {@code @DubboService}/{@code @Service}.
 * <p>
 * Priority:
 * <ol>
 *   <li>{@code interfaceName}</li>
 *   <li>{@code interfaceClass}</li>
 *   <li>unique business interface declared on the implementation class hierarchy
 *       (direct interfaces only; parent interfaces are not expanded)</li>
 * </ol>
 *
 * @author noear
 * @since 2.9
 */
public final class DubboServiceInterfaceResolver {
    private DubboServiceInterfaceResolver() {
    }

    /**
     * Resolve service interface class.
     *
     * @param interfaceClass annotation interfaceClass (may be void.class)
     * @param interfaceName  annotation interfaceName (may be empty)
     * @param implClass      service implementation class
     */
    public static Class<?> resolve(Class<?> interfaceClass, String interfaceName, Class<?> implClass) {
        if (Utils.isNotEmpty(interfaceName)) {
            try {
                return Class.forName(interfaceName, true, implClass.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Cannot load Dubbo service interfaceName: " + interfaceName
                        + " for " + implClass.getName(), e);
            }
        }

        if (interfaceClass != null
                && interfaceClass != void.class
                && interfaceClass != Void.class) {
            return interfaceClass;
        }

        List<Class<?>> interfaces = collectBusinessInterfaces(implClass);
        if (interfaces.isEmpty()) {
            throw new IllegalStateException("No service interface found for " + implClass.getName()
                    + ". Please specify interfaceClass/interfaceName on @DubboService.");
        }
        if (interfaces.size() > 1) {
            throw new IllegalStateException("Multiple service interfaces found for " + implClass.getName()
                    + ": " + interfaces + ". Please specify interfaceClass/interfaceName on @DubboService.");
        }
        return interfaces.get(0);
    }

    /**
     * Collect direct interfaces from the class hierarchy (order-preserving, de-duplicated).
     * Does <b>not</b> recursively expand parent interfaces of those interfaces, so
     * {@code HelloService extends BaseService} still yields a single candidate.
     * Filters out well-known non-business JDK interfaces.
     */
    static List<Class<?>> collectBusinessInterfaces(Class<?> implClass) {
        Set<Class<?>> set = new LinkedHashSet<>();
        Class<?> current = implClass;
        while (current != null && current != Object.class) {
            for (Class<?> iface : current.getInterfaces()) {
                if (iface != null) {
                    set.add(iface);
                }
            }
            current = current.getSuperclass();
        }

        // Keep only most-specific interfaces (drop a parent if a sub-interface is also present).
        // This covers the rare case that both Super and Sub appear as direct interfaces.
        List<Class<?>> candidates = new ArrayList<>();
        for (Class<?> iface : set) {
            if (isBusinessInterface(iface)) {
                candidates.add(iface);
            }
        }

        List<Class<?>> result = new ArrayList<>();
        for (Class<?> iface : candidates) {
            if (!isSuperOfAny(iface, candidates)) {
                result.add(iface);
            }
        }
        return result;
    }

    /**
     * Whether {@code iface} is a super-interface of any other candidate (strict).
     */
    private static boolean isSuperOfAny(Class<?> iface, List<Class<?>> candidates) {
        for (Class<?> other : candidates) {
            if (other != iface && iface.isAssignableFrom(other)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBusinessInterface(Class<?> iface) {
        if (!iface.isInterface()) {
            return false;
        }
        String name = iface.getName();
        // skip common JDK marker / utility interfaces
        if (name.startsWith("java.")) {
            return false;
        }
        if (name.startsWith("javax.") || name.startsWith("jakarta.")) {
            return false;
        }
        return true;
    }
}
