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

import static org.apache.seata.solon.autoconfigure.StarterConstants.REGISTRY_PREFIX;

@Configuration
@Inject(value = "${" + REGISTRY_PREFIX + "}",required = false)
public class RegistryProperties {
    /**
     * file, nacos, eureka, redis, zk, consul, etcd3, sofa
     */
    private String type = "file";

    private String preferredNetworks;

    public String getType() {
        return type;
    }

    public RegistryProperties setType(String type) {
        this.type = type;
        return this;
    }

    public String getPreferredNetworks() {
        return preferredNetworks;
    }

    public RegistryProperties setPreferredNetworks(String preferredNetworks) {
        this.preferredNetworks = preferredNetworks;
        return this;
    }
}
