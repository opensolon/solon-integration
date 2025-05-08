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
package org.apache.seata.solon.integration.intercept;

import org.apache.seata.common.util.StringUtils;
import org.apache.seata.core.context.RootContext;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Handler;
import org.noear.solon.core.route.RouterInterceptor;
import org.noear.solon.core.route.RouterInterceptorChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author noear 2024/10/25 created
 */
public class SeataSolonRouterInterceptor implements RouterInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeataSolonRouterInterceptor.class);

    @Override
    public void doIntercept(Context ctx, Handler mainHandler, RouterInterceptorChain chain) throws Throwable {
        String rpcXid = ctx.header(RootContext.KEY_XID);
        this.bindXid(rpcXid);

        try {
            chain.doIntercept(ctx, mainHandler);
        } finally {
            if (RootContext.inGlobalTransaction()) {
                this.cleanXid(rpcXid);
            }
        }
    }

    protected boolean bindXid(String rpcXid) {
        String xid = RootContext.getXID();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("xid in RootContext[{}] xid in HttpContext[{}]", xid, rpcXid);
        }
        if (StringUtils.isBlank(xid) && StringUtils.isNotBlank(rpcXid)) {
            RootContext.bind(rpcXid);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("bind[{}] to RootContext", rpcXid);
            }
        }

        return true;
    }

    protected void cleanXid(String rpcXid) {
        XidResource.cleanXid(rpcXid);
    }
}
