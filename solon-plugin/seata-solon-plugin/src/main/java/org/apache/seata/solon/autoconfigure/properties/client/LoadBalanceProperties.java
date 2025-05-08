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
package org.apache.seata.solon.autoconfigure.properties.client;

import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

import static org.apache.seata.common.DefaultValues.DEFAULT_LOAD_BALANCE;
import static org.apache.seata.common.DefaultValues.VIRTUAL_NODES_DEFAULT;
import static org.apache.seata.solon.autoconfigure.StarterConstants.LOAD_BALANCE_PREFIX_KEBAB_STYLE;

@Configuration
@Inject(value = "${" + LOAD_BALANCE_PREFIX_KEBAB_STYLE + "}",required = false)
public class LoadBalanceProperties {
    /**
     * the load balance
     */
    private String type = DEFAULT_LOAD_BALANCE;
    /**
     * the load balance virtual nodes
     */
    private int virtualNodes = VIRTUAL_NODES_DEFAULT;


    public String getType() {
        return type;
    }

    public LoadBalanceProperties setType(String type) {
        this.type = type;
        return this;
    }

    public int getVirtualNodes() {
        return virtualNodes;
    }

    public LoadBalanceProperties setVirtualNodes(int virtualNodes) {
        this.virtualNodes = virtualNodes;
        return this;
    }
}
