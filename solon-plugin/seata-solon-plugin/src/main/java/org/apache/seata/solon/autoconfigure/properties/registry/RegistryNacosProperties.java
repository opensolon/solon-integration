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

import static org.apache.seata.solon.autoconfigure.StarterConstants.REGISTRY_NACOS_PREFIX;

@Configuration
@Inject(value = "${" + REGISTRY_NACOS_PREFIX + "}",required = false)
public class RegistryNacosProperties {
    private String serverAddr = "localhost:8848";
    private String namespace;
    private String group = "SEATA_GROUP";
    private String cluster = "default";
    private String username;
    private String password;
    private String accessKey;
    private String secretKey;
    private String ramRoleName;
    private String application = "seata-server";
    private String slbPattern;
    private String contextPath;
    private String clientApplication;
    public String getServerAddr() {
        return serverAddr;
    }

    public RegistryNacosProperties setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
        return this;
    }

    public String getNamespace() {
        return namespace;
    }

    public RegistryNacosProperties setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getCluster() {
        return cluster;
    }

    public RegistryNacosProperties setCluster(String cluster) {
        this.cluster = cluster;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public RegistryNacosProperties setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public RegistryNacosProperties setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getApplication() {
        return application;
    }

    public RegistryNacosProperties setApplication(String application) {
        this.application = application;
        return this;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public RegistryNacosProperties setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public RegistryNacosProperties setSecretKey(String secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    public String getSlbPattern() {
        return slbPattern;
    }

    public RegistryNacosProperties setSlbPattern(String slbPattern) {
        this.slbPattern = slbPattern;
        return this;
    }

    public String getContextPath() {
        return contextPath;
    }

    public RegistryNacosProperties setContextPath(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }

    public String getClientApplication() {
        return clientApplication;
    }

    public RegistryNacosProperties setClientApplication(String clientApplication) {
        this.clientApplication = clientApplication;
        return this;
    }

    public String getRamRoleName() {
        return ramRoleName;
    }

    public RegistryNacosProperties setRamRoleName(String ramRoleName) {
        this.ramRoleName = ramRoleName;
        return this;
    }
}
