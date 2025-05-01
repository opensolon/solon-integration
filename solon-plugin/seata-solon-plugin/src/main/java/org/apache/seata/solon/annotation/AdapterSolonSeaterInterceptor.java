package org.apache.seata.solon.annotation;

import org.apache.seata.integration.tx.api.interceptor.InvocationWrapper;
import org.apache.seata.integration.tx.api.interceptor.handler.ProxyInvocationHandler;
import org.apache.seata.integration.tx.api.interceptor.parser.DefaultInterfaceParser;
import org.noear.solon.core.aspect.Invocation;
import org.noear.solon.core.aspect.MethodInterceptor;

import java.util.*;

public class AdapterSolonSeaterInterceptor implements MethodInterceptor {
    private static final Set<String> PROXYED_SET = new HashSet<>();
    private final DefaultInterfaceParser interfaceParser = DefaultInterfaceParser.get();
    private static final Map<String, ProxyInvocationHandler> handlerMap = new HashMap<>();

    @Override
    public Object doIntercept(Invocation inv) throws Throwable {
        InvocationWrapper invocationWrapper = new SolonInvocationWrapper(inv);
        Object target = inv.target();
        ProxyInvocationHandler proxyInvocationHandler = this.wrapIfNecessary(target);
        if (Objects.nonNull(proxyInvocationHandler)) {
            return proxyInvocationHandler.invoke(invocationWrapper);
        } else {
            return inv.invoke();
        }
    }

    /**
     * 处理 Bean，如果需要则创建代理
     *
     * @param bean bean 实例
     * @return 处理后的 bean（可能是原始 bean 或代理 bean）
     */
    public ProxyInvocationHandler wrapIfNecessary(Object bean) throws Exception {
        if (bean == null) {
            return null;
        }

        String canonicalName = bean.getClass().getCanonicalName();

        // 检查是否已经被代理
        synchronized (handlerMap) {
            ProxyInvocationHandler proxyInvocationHandler = handlerMap.get(canonicalName);
            if (Objects.nonNull(proxyInvocationHandler)) {
                return proxyInvocationHandler;
            }
            PROXYED_SET.add(canonicalName);

            // 解析接口并创建代理处理器
            proxyInvocationHandler = interfaceParser.parserInterfaceToProxy(bean, canonicalName);
            if (Objects.nonNull(proxyInvocationHandler)) {
                handlerMap.put(canonicalName, proxyInvocationHandler);
            }
            return proxyInvocationHandler;
        }
    }
}
