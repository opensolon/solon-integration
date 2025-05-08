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
import org.noear.solon.core.aspect.Invocation;

import java.lang.reflect.Method;

/**
 * Solon invocation wrapper
 *
 * @author noear 2024/10/25 created
 */
public class SolonInvocationWrapper implements InvocationWrapper {
    private final Invocation inv;

    public SolonInvocationWrapper(Invocation inv) {
        this.inv = inv;
    }

    @Override
    public Method getMethod() {
        return inv.method().getMethod();
    }

    @Override
    public Object getProxy() {
        return inv.target();
    }

    @Override
    public Object getTarget() {
        return inv.getTargetClz();
    }

    @Override
    public Object[] getArguments() {
        return inv.args();
    }

    @Override
    public Object proceed() throws Throwable {
        return inv.invoke();
    }
}
