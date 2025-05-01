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
package org.apache.seata.solon.autoconfigure.properties;

import org.apache.seata.solon.autoconfigure.properties.client.*;
import org.apache.seata.solon.autoconfigure.properties.config.*;
import org.apache.seata.solon.autoconfigure.properties.core.LogProperties;
import org.apache.seata.solon.autoconfigure.properties.core.ShutdownProperties;
import org.apache.seata.solon.autoconfigure.properties.core.ThreadFactoryProperties;
import org.apache.seata.solon.autoconfigure.properties.core.TransportProperties;
import org.apache.seata.solon.autoconfigure.properties.registry.*;

import static org.apache.seata.solon.autoconfigure.StarterConstants.*;

public class PropertiesHelper {
    public static void initBeanMap() {
        PROPERTY_BEAN_MAP.put(CONFIG_PREFIX, ConfigProperties.class);
        PROPERTY_BEAN_MAP.put(CONFIG_FILE_PREFIX, ConfigFileProperties.class);
        PROPERTY_BEAN_MAP.put(REGISTRY_PREFIX, RegistryProperties.class);

        PROPERTY_BEAN_MAP.put(CONFIG_NACOS_PREFIX, ConfigNacosProperties.class);
        PROPERTY_BEAN_MAP.put(CONFIG_CONSUL_PREFIX, ConfigConsulProperties.class);
        PROPERTY_BEAN_MAP.put(CONFIG_ZK_PREFIX, ConfigZooKeeperProperties.class);
        PROPERTY_BEAN_MAP.put(CONFIG_APOLLO_PREFIX, ConfigApolloProperties.class);
        PROPERTY_BEAN_MAP.put(CONFIG_ETCD3_PREFIX, ConfigEtcd3Properties.class);
        PROPERTY_BEAN_MAP.put(CONFIG_CUSTOM_PREFIX, ConfigCustomProperties.class);

        PROPERTY_BEAN_MAP.put(REGISTRY_CONSUL_PREFIX, RegistryConsulProperties.class);
        PROPERTY_BEAN_MAP.put(REGISTRY_ETCD3_PREFIX, RegistryEtcd3Properties.class);
        PROPERTY_BEAN_MAP.put(REGISTRY_EUREKA_PREFIX, RegistryEurekaProperties.class);
        PROPERTY_BEAN_MAP.put(REGISTRY_NACOS_PREFIX, RegistryNacosProperties.class);
        PROPERTY_BEAN_MAP.put(REGISTRY_NAMINGSERVER_PREFIX, RegistryNamingServerProperties.class);
        PROPERTY_BEAN_MAP.put(REGISTRY_REDIS_PREFIX, RegistryRedisProperties.class);
        PROPERTY_BEAN_MAP.put(REGISTRY_SOFA_PREFIX, RegistrySofaProperties.class);
        PROPERTY_BEAN_MAP.put(REGISTRY_ZK_PREFIX, RegistryZooKeeperProperties.class);
        PROPERTY_BEAN_MAP.put(REGISTRY_CUSTOM_PREFIX, RegistryCustomProperties.class);
        PROPERTY_BEAN_MAP.put(REGISTRY_RAFT_PREFIX, RegistryRaftProperties.class);

        PROPERTY_BEAN_MAP.put(THREAD_FACTORY_PREFIX, ThreadFactoryProperties.class);
        PROPERTY_BEAN_MAP.put(TRANSPORT_PREFIX, TransportProperties.class);
        PROPERTY_BEAN_MAP.put(SHUTDOWN_PREFIX, ShutdownProperties.class);
        PROPERTY_BEAN_MAP.put(LOG_PREFIX, LogProperties.class);


        PROPERTY_BEAN_MAP.put(SEATA_PREFIX, SeataProperties.class);

        PROPERTY_BEAN_MAP.put(CLIENT_RM_PREFIX, RmProperties.class);
        PROPERTY_BEAN_MAP.put(CLIENT_TM_PREFIX, TmProperties.class);
        PROPERTY_BEAN_MAP.put(LOCK_PREFIX, LockProperties.class);
        PROPERTY_BEAN_MAP.put(SERVICE_PREFIX, ServiceProperties.class);
        PROPERTY_BEAN_MAP.put(UNDO_PREFIX, UndoProperties.class);
        PROPERTY_BEAN_MAP.put(COMPRESS_PREFIX, UndoCompressProperties.class);
        PROPERTY_BEAN_MAP.put(LOAD_BALANCE_PREFIX, LoadBalanceProperties.class);
        PROPERTY_BEAN_MAP.put(SAGA_ASYNC_THREAD_POOL_PREFIX, SagaAsyncThreadPoolProperties.class);
        PROPERTY_BEAN_MAP.put(TCC_PREFIX, SeataTccProperties.class);
    }
}
