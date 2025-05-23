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

import static org.apache.seata.solon.autoconfigure.StarterConstants.LOAD_BALANCE_PREFIX_KEBAB_STYLE;

@Configuration
@Inject(value = "${" + LOAD_BALANCE_PREFIX_KEBAB_STYLE + "}",required = false)
public class ConfigNacosProperties {
    private String serverAddr;
    private String namespace;
    private String group = "SEATA_GROUP";
    private String username;
    private String password;
    private String accessKey;
    private String secretKey;
    private String ramRoleName;
    private String dataId = "seata.properties";
    private String contextPath;

    public String getServerAddr() {
        return serverAddr;
    }

    public ConfigNacosProperties setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
        return this;
    }

    public String getNamespace() {
        return namespace;
    }

    public ConfigNacosProperties setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public ConfigNacosProperties setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public ConfigNacosProperties setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public ConfigNacosProperties setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getDataId() {
        return dataId;
    }

    public ConfigNacosProperties setDataId(String dataId) {
        this.dataId = dataId;
        return this;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public ConfigNacosProperties setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public ConfigNacosProperties setSecretKey(String secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    public String getContextPath() {
        return contextPath;
    }

    public ConfigNacosProperties setContextPath(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }

    public String getRamRoleName() {
        return ramRoleName;
    }

    public ConfigNacosProperties setRamRoleName(String ramRoleName) {
        this.ramRoleName = ramRoleName;
        return this;
    }
}
