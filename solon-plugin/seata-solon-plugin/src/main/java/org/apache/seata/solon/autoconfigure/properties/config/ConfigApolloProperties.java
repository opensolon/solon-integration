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
import static org.apache.seata.solon.autoconfigure.StarterConstants.CONFIG_APOLLO_PREFIX;

@Configuration
@Inject(value = "${" + CONFIG_APOLLO_PREFIX + "}",required = false)
public class ConfigApolloProperties {
    private String appId = "seata-server";
    private String apolloMeta;
    private String namespace = "application";
    private String apolloAccessKeySecret;
    private String apolloConfigService;
    private String cluster;

    public String getAppId() {
        return appId;
    }

    public ConfigApolloProperties setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public String getApolloMeta() {
        return apolloMeta;
    }

    public ConfigApolloProperties setApolloMeta(String apolloMeta) {
        this.apolloMeta = apolloMeta;
        return this;
    }

    public String getNamespace() {
        return namespace;
    }

    public ConfigApolloProperties setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public String getApolloAccessKeySecret() {
        return apolloAccessKeySecret;
    }

    public ConfigApolloProperties setApolloAccessKeySecret(String apolloAccessKeySecret) {
        this.apolloAccessKeySecret = apolloAccessKeySecret;
        return this;
    }

    public String getApolloConfigService() {
        return apolloConfigService;
    }

    public ConfigApolloProperties setApolloConfigService(String apolloConfigService) {
        this.apolloConfigService = apolloConfigService;
        return this;
    }

    public String getCluster() {
        return cluster;
    }

    public ConfigApolloProperties setCluster(String cluster) {
        this.cluster = cluster;
        return this;
    }
}
