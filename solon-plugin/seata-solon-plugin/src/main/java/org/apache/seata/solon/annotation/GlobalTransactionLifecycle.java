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
package org.apache.seata.solon.annotation;

import org.apache.seata.common.util.StringUtils;
import org.apache.seata.config.CachedConfigurationChangeListener;
import org.apache.seata.config.ConfigurationChangeEvent;
import org.apache.seata.config.ConfigurationFactory;
import org.apache.seata.core.constants.ConfigurationKeys;
import org.apache.seata.core.rpc.ShutdownHook;
import org.apache.seata.core.rpc.netty.RmNettyRemotingClient;
import org.apache.seata.core.rpc.netty.TmNettyRemotingClient;
import org.apache.seata.integration.tx.api.interceptor.handler.ProxyInvocationHandler;
import org.apache.seata.integration.tx.api.interceptor.parser.DefaultInterfaceParser;
import org.apache.seata.rm.RMClient;
import org.apache.seata.rm.tcc.api.TwoPhaseBusinessAction;
import org.apache.seata.spring.annotation.GlobalLock;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.apache.seata.tm.TMClient;
import org.apache.seata.tm.api.FailureHandler;
import org.apache.seata.tm.api.FailureHandlerHolder;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.bean.LifecycleBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.seata.common.DefaultValues.*;

/**
 * The type Global transaction lifecycle (old: The type Global transaction scanner.)
 *
 */
public class GlobalTransactionLifecycle implements CachedConfigurationChangeListener, LifecycleBean {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalTransactionLifecycle.class);

    private static final int AT_MODE = 1;
    private static final int MT_MODE = 2;

    private static final int ORDER_NUM = 1024;
    private static final int DEFAULT_MODE = AT_MODE + MT_MODE;

    private final String applicationId;
    private final String txServiceGroup;
    private static String accessKey;
    private static String secretKey;
    private volatile boolean disableGlobalTransaction = ConfigurationFactory.getInstance().getBoolean(
            ConfigurationKeys.DISABLE_GLOBAL_TRANSACTION, DEFAULT_DISABLE_GLOBAL_TRANSACTION);
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private final FailureHandler failureHandlerHook;

    /**
     * Instantiates a new Global transaction scanner.
     *
     * @param applicationId      the application id
     * @param txServiceGroup     the tx service group
     * @param exposeProxy        the exposeProxy
     * @param failureHandlerHook the failure handler hook
     */
    public GlobalTransactionLifecycle(String applicationId, String txServiceGroup, boolean exposeProxy,
                                      FailureHandler failureHandlerHook) {
        this(applicationId, txServiceGroup, DEFAULT_MODE, exposeProxy, failureHandlerHook);
    }

    /**
     * Instantiates a new Global transaction scanner.
     *
     * @param applicationId      the application id
     * @param txServiceGroup     the tx service group
     * @param mode               the mode
     * @param exposeProxy        the exposeProxy
     * @param failureHandlerHook the failure handler hook
     */
    public GlobalTransactionLifecycle(String applicationId, String txServiceGroup, int mode, boolean exposeProxy,
                                      FailureHandler failureHandlerHook) {
        this.applicationId = applicationId;
        this.txServiceGroup = txServiceGroup;
        this.failureHandlerHook = failureHandlerHook;
        FailureHandlerHolder.setFailureHandler(this.failureHandlerHook);
    }

    /**
     * Sets access key.
     *
     * @param accessKey the access key
     */
    public static void setAccessKey(String accessKey) {
        GlobalTransactionLifecycle.accessKey = accessKey;
    }

    /**
     * Sets secret key.
     *
     * @param secretKey the secret key
     */
    public static void setSecretKey(String secretKey) {
        GlobalTransactionLifecycle.secretKey = secretKey;
    }


    protected void initClient() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Initializing Global Transaction Clients ... ");
        }
        if (DEFAULT_TX_GROUP_OLD.equals(txServiceGroup)) {
            LOGGER.warn("the default value of seata.tx-service-group: {} has already changed to {} since Seata 1.5, " +
                            "please change your default configuration as soon as possible " +
                            "and we don't recommend you to use default tx-service-group's value provided by seata",
                    DEFAULT_TX_GROUP_OLD, DEFAULT_TX_GROUP);
        }
        if (StringUtils.isNullOrEmpty(applicationId) || StringUtils.isNullOrEmpty(txServiceGroup)) {
            throw new IllegalArgumentException(String.format("applicationId: %s, txServiceGroup: %s", applicationId, txServiceGroup));
        }
        //init TM
        TMClient.init(applicationId, txServiceGroup, accessKey, secretKey);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Transaction Manager Client is initialized. applicationId[{}] txServiceGroup[{}]", applicationId, txServiceGroup);
        }
        //init RM
        RMClient.init(applicationId, txServiceGroup);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Resource Manager is initialized. applicationId[{}] txServiceGroup[{}]", applicationId, txServiceGroup);
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Global Transaction Clients are initialized. ");
        }
        registerSpringShutdownHook();

    }

    protected void registerSpringShutdownHook() {
        ShutdownHook.getInstance().addDisposable(TmNettyRemotingClient.getInstance(applicationId, txServiceGroup, accessKey, secretKey));
        ShutdownHook.getInstance().addDisposable(RmNettyRemotingClient.getInstance(applicationId, txServiceGroup));
    }


    @Override
    public void start() {
        if (disableGlobalTransaction) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Global transaction is disabled.");
            }
            ConfigurationFactory.getInstance().addConfigListener(ConfigurationKeys.DISABLE_GLOBAL_TRANSACTION, (CachedConfigurationChangeListener) this);
            return;
        }

        if (initialized.compareAndSet(false, true)) {
            initClient();
        }
    }

    @Override
    public void stop() {
        ShutdownHook.getInstance().destroyAll();
    }


    @Override
    public void onChangeEvent(ConfigurationChangeEvent event) {
        if (ConfigurationKeys.DISABLE_GLOBAL_TRANSACTION.equals(event.getDataId())) {
            disableGlobalTransaction = Boolean.parseBoolean(event.getNewValue().trim());
            if (!disableGlobalTransaction && initialized.compareAndSet(false, true)) {
                LOGGER.info("{} config changed, old value:true, new value:{}", ConfigurationKeys.DISABLE_GLOBAL_TRANSACTION,
                        event.getNewValue());
                initClient();
                ConfigurationFactory.getInstance().removeConfigListener(ConfigurationKeys.DISABLE_GLOBAL_TRANSACTION, this);
            }
        }
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getTxServiceGroup() {
        return txServiceGroup;
    }

    public static String getAccessKey() {
        return accessKey;
    }

    public static String getSecretKey() {
        return secretKey;
    }
}
