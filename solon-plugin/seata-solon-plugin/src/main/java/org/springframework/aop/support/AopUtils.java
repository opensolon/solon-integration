package org.springframework.aop.support;


import org.noear.solon.lang.Nullable;

import java.lang.reflect.Proxy;


/**
 * Utility methods for AOP support code.
 *
 * <p>Mainly for internal use within Spring's AOP support.
 *
 * <p>See {@link org.springframework.aop.framework.AopProxyUtils} for a
 * collection of framework-specific AOP utility methods which depend
 * on internals of Spring's AOP framework implementation.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @see org.springframework.aop.framework.AopProxyUtils
 */
public abstract class AopUtils {

    public static final String CGLIB_CLASS_SEPARATOR = "$$";

    /**
     * Check whether the given object is a JDK dynamic proxy or a CGLIB proxy.
     * <p>This method additionally checks if the given object is an instance
     * of {@link SpringProxy}.
     *
     * @param object the object to check
     * @see #isJdkDynamicProxy
     * @see #isCglibProxy
     */
    public static boolean isAopProxy(@Nullable Object object) {
        return Proxy.isProxyClass(object.getClass()) ||
                object.getClass().getName().contains(CGLIB_CLASS_SEPARATOR);
    }

    public static boolean isJdkDynamicProxy(@Nullable Object object) {
        return Proxy.isProxyClass(object.getClass());
    }

}
