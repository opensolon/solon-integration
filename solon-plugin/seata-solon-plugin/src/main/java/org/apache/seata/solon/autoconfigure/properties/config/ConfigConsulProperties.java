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
package org.apache.seata.solon.autoconfigure.properties.config;

import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

import static org.apache.seata.solon.autoconfigure.StarterConstants.CONFIG_CONSUL_PREFIX;

@Configuration
@Inject(value = "${" + CONFIG_CONSUL_PREFIX + "}",required = false)
public class ConfigConsulProperties {
    private String serverAddr;
    private String key = "seata.properties";
    private String aclToken;

    public String getServerAddr() {
        return serverAddr;
    }

    public ConfigConsulProperties setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
        return this;
    }

    public String getKey() {
        return key;
    }

    public ConfigConsulProperties setKey(String key) {
        this.key = key;
        return this;
    }
    public String getAclToken() {
        return aclToken;
    }

    public ConfigConsulProperties setAclToken(String aclToken) {
        this.aclToken = aclToken;
        return this;
    }
}
