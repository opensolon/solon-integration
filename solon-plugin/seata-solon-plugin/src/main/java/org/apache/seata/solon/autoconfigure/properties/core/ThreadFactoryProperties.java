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
package org.apache.seata.solon.autoconfigure.properties.core;

import org.apache.seata.core.rpc.netty.NettyBaseConfig.WorkThreadMode;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

import static org.apache.seata.common.DefaultValues.*;
import static org.apache.seata.solon.autoconfigure.StarterConstants.THREAD_FACTORY_PREFIX_KEBAB_STYLE;

@Configuration
@Inject(value = "${" + THREAD_FACTORY_PREFIX_KEBAB_STYLE + "}",required = false)
public class ThreadFactoryProperties {
    private String bossThreadPrefix = DEFAULT_BOSS_THREAD_PREFIX;
    private String workerThreadPrefix = DEFAULT_NIO_WORKER_THREAD_PREFIX;
    private String serverExecutorThreadPrefix = DEFAULT_EXECUTOR_THREAD_PREFIX;
    private boolean shareBossWorker = false;
    private String clientSelectorThreadPrefix = DEFAULT_SELECTOR_THREAD_PREFIX;
    private int clientSelectorThreadSize = DEFAULT_SELECTOR_THREAD_SIZE;
    private String clientWorkerThreadPrefix = DEFAULT_WORKER_THREAD_PREFIX;
    /**
     * netty boss thread size
     */
    private int bossThreadSize = DEFAULT_BOSS_THREAD_SIZE;
    /**
     * auto default pin or 8
     */
    private String workerThreadSize = WorkThreadMode.Default.name();

    public String getBossThreadPrefix() {
        return bossThreadPrefix;
    }

    public ThreadFactoryProperties setBossThreadPrefix(String bossThreadPrefix) {
        this.bossThreadPrefix = bossThreadPrefix;
        return this;
    }

    public String getWorkerThreadPrefix() {
        return workerThreadPrefix;
    }

    public ThreadFactoryProperties setWorkerThreadPrefix(String workerThreadPrefix) {
        this.workerThreadPrefix = workerThreadPrefix;
        return this;
    }

    public String getServerExecutorThreadPrefix() {
        return serverExecutorThreadPrefix;
    }

    public ThreadFactoryProperties setServerExecutorThreadPrefix(String serverExecutorThreadPrefix) {
        this.serverExecutorThreadPrefix = serverExecutorThreadPrefix;
        return this;
    }

    public boolean isShareBossWorker() {
        return shareBossWorker;
    }

    public ThreadFactoryProperties setShareBossWorker(boolean shareBossWorker) {
        this.shareBossWorker = shareBossWorker;
        return this;
    }

    public String getClientSelectorThreadPrefix() {
        return clientSelectorThreadPrefix;
    }

    public ThreadFactoryProperties setClientSelectorThreadPrefix(String clientSelectorThreadPrefix) {
        this.clientSelectorThreadPrefix = clientSelectorThreadPrefix;
        return this;
    }

    public String getClientWorkerThreadPrefix() {
        return clientWorkerThreadPrefix;
    }

    public ThreadFactoryProperties setClientWorkerThreadPrefix(String clientWorkerThreadPrefix) {
        this.clientWorkerThreadPrefix = clientWorkerThreadPrefix;
        return this;
    }

    public int getClientSelectorThreadSize() {
        return clientSelectorThreadSize;
    }

    public ThreadFactoryProperties setClientSelectorThreadSize(int clientSelectorThreadSize) {
        this.clientSelectorThreadSize = clientSelectorThreadSize;
        return this;
    }

    public int getBossThreadSize() {
        return bossThreadSize;
    }

    public ThreadFactoryProperties setBossThreadSize(int bossThreadSize) {
        this.bossThreadSize = bossThreadSize;
        return this;
    }

    public String getWorkerThreadSize() {
        return workerThreadSize;
    }

    public ThreadFactoryProperties setWorkerThreadSize(String workerThreadSize) {
        this.workerThreadSize = workerThreadSize;
        return this;
    }
}
