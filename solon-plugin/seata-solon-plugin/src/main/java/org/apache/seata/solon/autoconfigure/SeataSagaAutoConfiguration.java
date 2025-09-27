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
package org.apache.seata.solon.autoconfigure;

import org.apache.seata.saga.engine.StateMachineConfig;
import org.apache.seata.saga.engine.StateMachineEngine;
import org.apache.seata.saga.engine.impl.ProcessCtrlStateMachineEngine;
import org.apache.seata.saga.rm.StateMachineEngineHolder;
import org.apache.seata.solon.autoconfigure.config.DbStateMachineConfig;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Condition;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

import static org.apache.seata.solon.autoconfigure.StarterConstants.SAGA_STATE_MACHINE_PREFIX;

@Configuration
@Condition(onExpression = "${" + StarterConstants.SAGA_PREFIX + ".enabled:true} == 'true'")
public class SeataSagaAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeataSagaAutoConfiguration.class);

    public static final String SAGA_DATA_SOURCE_BEAN_NAME = "seataSagaDataSource";
    public static final String SAGA_ASYNC_THREAD_POOL_EXECUTOR_BEAN_NAME = "seataSagaAsyncThreadPoolExecutor";
    public static final String SAGA_REJECTED_EXECUTION_HANDLER_BEAN_NAME = "seataSagaRejectedExecutionHandler";

    /**
     * Create state machine config bean.
     */
    @Bean
    @Condition(onBean = DataSource.class, onMissingBean = StateMachineConfig.class)
    public StateMachineConfig dbStateMachineConfig(
            DataSource dataSource,
            @Inject(value = SAGA_DATA_SOURCE_BEAN_NAME, required = false) DataSource sagaDataSource,
            @Inject(value = SAGA_ASYNC_THREAD_POOL_EXECUTOR_BEAN_NAME, required = false) ThreadPoolExecutor threadPoolExecutor,
            @Inject("${solon.app.name:}") String applicationId,
            @Inject("${seata.txServiceGroup:}") String txServiceGroup,
            @Inject(value = "${" + SAGA_STATE_MACHINE_PREFIX + ".enableAsync}", required = false) Boolean enableAsync) throws Exception {
        DbStateMachineConfig config = new DbStateMachineConfig();
        config.setDataSource(sagaDataSource != null ? sagaDataSource : dataSource);
        config.setApplicationId(applicationId);
        config.setTxServiceGroup(txServiceGroup);
        if (Objects.equals(enableAsync, Boolean.TRUE)) {
            config.setEnableAsync(true);
        }

        if (threadPoolExecutor != null) {
            config.setThreadPoolExecutor(threadPoolExecutor);
        }

        config.afterPropertiesSet();

        return config;
    }

    /**
     * @param config state machine config
     *               Create state machine engine bean.
     */
    @Bean
    @Condition(onMissingBean = StateMachineEngine.class, onBean = StateMachineConfig.class)
    public StateMachineEngine stateMachineEngine(StateMachineConfig config) {
        ProcessCtrlStateMachineEngine engine = new ProcessCtrlStateMachineEngine();
        engine.setStateMachineConfig(config);
        StateMachineEngineHolder.setStateMachineEngine(engine);
        return engine;
    }
}