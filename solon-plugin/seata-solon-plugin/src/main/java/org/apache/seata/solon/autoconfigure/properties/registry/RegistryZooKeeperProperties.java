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

import static org.apache.seata.solon.autoconfigure.StarterConstants.REGISTRY_ZK_PREFIX;

@Configuration
@Inject(value = "${" + REGISTRY_ZK_PREFIX + "}",required = false)
public class RegistryZooKeeperProperties {
    private String cluster = "default";
    private String serverAddr = "127.0.0.1:2181";
    private int sessionTimeout = 6000;
    private int connectTimeout = 2000;
    private String username;
    private String password;

    public String getCluster() {
        return cluster;
    }

    public RegistryZooKeeperProperties setCluster(String cluster) {
        this.cluster = cluster;
        return this;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public RegistryZooKeeperProperties setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
        return this;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public RegistryZooKeeperProperties setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
        return this;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public RegistryZooKeeperProperties setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }
    public String getUsername() {
        return username;
    }

    public RegistryZooKeeperProperties setUsername(String username) {
        this.username = username;
        return this;
    }
    public String getPassword() {
        return password;
    }

    public RegistryZooKeeperProperties setPassword(String password) {
        this.password = password;
        return this;
    }
}
