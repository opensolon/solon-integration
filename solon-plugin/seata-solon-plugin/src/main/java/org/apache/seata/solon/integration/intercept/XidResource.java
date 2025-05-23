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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Xid handler.
 *
 */
public class XidResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(XidResource.class);


    public static void cleanXid(String rpcXid) {
        String xid = RootContext.getXID();
        if (StringUtils.isNotBlank(xid)) {
            String unbindXid = RootContext.unbind();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("unbind[{}] from RootContext", unbindXid);
            }

            if (!StringUtils.equalsIgnoreCase(rpcXid, unbindXid)) {
                LOGGER.warn("xid in change during RPC from {} to {}", rpcXid, unbindXid);
                if (StringUtils.isNotBlank(unbindXid)) {
                    RootContext.bind(unbindXid);
                    LOGGER.warn("bind [{}] back to RootContext", unbindXid);
                }
            }
        }
    }
}
