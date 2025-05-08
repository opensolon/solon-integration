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

import org.apache.seata.solon.annotation.GlobalTransactionLifecycle;
import org.apache.seata.solon.autoconfigure.properties.SeataProperties;
import org.apache.seata.tm.api.DefaultFailureHandlerImpl;
import org.apache.seata.tm.api.FailureHandler;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Condition;
import org.noear.solon.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.seata.common.ConfigurationKeys.SEATA_PREFIX;
import static org.apache.seata.common.Constants.BEAN_NAME_FAILURE_HANDLER;

@Configuration
@Condition(onProperty = "${"+SEATA_PREFIX+".enabled:true} = true")
public class SeataAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeataAutoConfiguration.class);

    @Bean(value = BEAN_NAME_FAILURE_HANDLER, typed = true)
    @Condition(onMissingBean = FailureHandler.class)
    public FailureHandler failureHandler() {
        return new DefaultFailureHandlerImpl();
    }

    //old: globalTransactionScanner
    @Bean
    @Condition(onMissingBean = GlobalTransactionLifecycle.class)
    public GlobalTransactionLifecycle globalTransactionLifecycle(SeataProperties seataProperties,
                                                               FailureHandler failureHandler) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Automatically configure Seata");
        }

        //set accessKey and secretKey
        GlobalTransactionLifecycle.setAccessKey(seataProperties.getAccessKey());
        GlobalTransactionLifecycle.setSecretKey(seataProperties.getSecretKey());

        // create global transaction scanner
        return new GlobalTransactionLifecycle(seataProperties.getApplicationId(), seataProperties.getTxServiceGroup(),
                seataProperties.isExposeProxy(), failureHandler);
    }
}