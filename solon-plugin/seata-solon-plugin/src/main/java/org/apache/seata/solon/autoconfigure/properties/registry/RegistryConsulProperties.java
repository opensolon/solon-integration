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
package org.apache.seata.solon.autoconfigure.properties.registry;

import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

import static org.apache.seata.solon.autoconfigure.StarterConstants.REGISTRY_CONSUL_PREFIX;

@Configuration
@Inject(value = "${" + REGISTRY_CONSUL_PREFIX + "}",required = false)
public class RegistryConsulProperties {
    private String cluster = "default";
    private String serverAddr = "127.0.0.1:8500";
    private String aclToken;

    public String getCluster() {
        return cluster;
    }

    public RegistryConsulProperties setCluster(String cluster) {
        this.cluster = cluster;
        return this;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public RegistryConsulProperties setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
        return this;
    }

    public String getAclToken() {
        return aclToken;
    }

    public RegistryConsulProperties setAclToken(String aclToken) {
        this.aclToken = aclToken;
        return this;
    }
}
