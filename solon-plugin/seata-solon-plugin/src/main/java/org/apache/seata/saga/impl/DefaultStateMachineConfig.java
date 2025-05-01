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
package org.apache.seata.saga.impl;

import org.apache.seata.saga.engine.config.AbstractStateMachineConfig;
import org.apache.seata.saga.engine.expression.ExpressionFactory;
import org.apache.seata.saga.engine.expression.ExpressionFactoryManager;
import org.apache.seata.saga.engine.expression.snel.SolonExpressionFactory;
import org.apache.seata.saga.engine.invoker.ServiceInvokerManager;
import org.apache.seata.saga.engine.invoker.SolonServiceInvoker;
import org.apache.seata.saga.statelang.domain.DomainConstants;
import org.noear.solon.Solon;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.util.ResourceUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * State machine configuration base spring. In spring context,some uses will be combined with the spring framework.
 * such as expression evaluation add spring el impl, serviceInvoker add spring bean Invoker impl, etc ...
 */
public class DefaultStateMachineConfig extends AbstractStateMachineConfig {

    private AppContext applicationContext;

    private String RESOURCES = "classpath*:seata/saga/statelang/**/*.json";

    public void init() throws Exception {
        // super init
        super.init();

        // register StateMachine def  after init
        registerStateMachineDef();

        // register spring el ExpressionFactoryManager
        registerSpringElExpressionFactoryManager();

        // register serviceInvoker as spring bean invoker after init
        registerSpringBeanServiceInvoker();
    }

    private void registerStateMachineDef() throws IOException {
        Collection<String> registerResources = ResourceUtil.scanResources(RESOURCES);

        InputStream[] resourceAsStreamArray = new InputStream[registerResources.size()];
        Iterator<String> iterator = registerResources.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            String resource = iterator.next();
            resourceAsStreamArray[i] = ResourceUtil.getResourceAsStream(resource);
            i++;
        }
        getStateMachineRepository().registryByResources(resourceAsStreamArray, getDefaultTenantId());
    }

    private void registerSpringElExpressionFactoryManager() {
        ExpressionFactoryManager expressionFactoryManager = getExpressionFactoryManager();
        ExpressionFactory bean = Solon.context().getBean(ExpressionFactory.class);
        if (Objects.nonNull(bean)) {
            expressionFactoryManager.putExpressionFactory(ExpressionFactoryManager.DEFAULT_EXPRESSION_TYPE, bean);
        } else {
            SolonExpressionFactory springELExpressionFactory = new SolonExpressionFactory();
            expressionFactoryManager.putExpressionFactory(ExpressionFactoryManager.DEFAULT_EXPRESSION_TYPE, springELExpressionFactory);
        }
    }

    private void registerSpringBeanServiceInvoker() {
        ServiceInvokerManager manager = getServiceInvokerManager();
        SolonServiceInvoker springBeanServiceInvoker = new SolonServiceInvoker();
        springBeanServiceInvoker.setSagaJsonParser(getSagaJsonParser());
        springBeanServiceInvoker.setThreadPoolExecutor(getThreadPoolExecutor());
        manager.putServiceInvoker(DomainConstants.SERVICE_TYPE_SPRING_BEAN, springBeanServiceInvoker);
    }
}
