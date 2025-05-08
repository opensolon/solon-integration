/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.seata.solon.annotation;


import org.apache.seata.integration.tx.api.interceptor.InvocationWrapper;
import org.apache.seata.integration.tx.api.interceptor.handler.ProxyInvocationHandler;
import org.apache.seata.rm.tcc.interceptor.TccActionInterceptorHandler;
import org.apache.seata.spring.annotation.AdapterSpringSeataInterceptor;
import org.noear.solon.core.aspect.Invocation;
import org.noear.solon.core.aspect.MethodInterceptor;

import java.util.HashSet;
import java.util.Set;

import static org.apache.seata.common.DefaultValues.DEFAULT_DISABLE_GLOBAL_TRANSACTION;

/**
 * GlobalTransactional annotation interceptor
 *
 * @author noear 2024/10/25 created
 */
public class TccActionInterceptor implements MethodInterceptor {
    private final Set<String> methodsToProxy = new HashSet<>();

    @Override
    public Object doIntercept(Invocation inv) throws Throwable {
        InvocationWrapper invocationWrapper = new SolonInvocationWrapper(inv);
        Object target = inv.target();
        return this.createProxyInvocationHandler(target).invoke(invocationWrapper);
    }

    protected ProxyInvocationHandler createProxyInvocationHandler(Object targetBean) {
        return new TccActionInterceptorHandler(targetBean, methodsToProxy);
    }
}
