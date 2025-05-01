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
package org.apache.seata.solon.integration;

import org.apache.seata.rm.tcc.api.LocalTCC;
import org.apache.seata.rm.tcc.api.TwoPhaseBusinessAction;
import org.apache.seata.solon.annotation.AdapterSolonSeaterInterceptor;
import org.apache.seata.solon.annotation.GlobalTransactionalInterceptor;
import org.apache.seata.solon.annotation.TccActionInterceptor;
import org.apache.seata.solon.annotation.datasource.SeataAutoDataSourceProxyCreator;
import org.apache.seata.solon.autoconfigure.SagaAsyncThreadPoolExecutorConfiguration;
import org.apache.seata.solon.autoconfigure.SeataAutoConfiguration;
import org.apache.seata.solon.autoconfigure.SeataSagaAutoConfiguration;
import org.apache.seata.solon.autoconfigure.properties.PropertiesHelper;
import org.apache.seata.solon.autoconfigure.properties.SeataProperties;
import org.apache.seata.solon.autoconfigure.properties.client.ServiceProperties;
import org.apache.seata.solon.integration.intercept.SeataHttpExtension;
import org.apache.seata.solon.integration.intercept.SeataNamiFilter;
import org.apache.seata.solon.integration.intercept.SeataSolonRouterInterceptor;
import org.apache.seata.spring.annotation.GlobalLock;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.noear.nami.NamiManager;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;
import org.noear.solon.core.util.ClassUtil;
import org.noear.solon.net.http.HttpExtensionManager;

import javax.sql.DataSource;

/**
 * Seata for solon plugin (like module lifecycle)
 *
 * @author noear 2024/10/25 created
 */
public class SeataPlugin implements Plugin {
    @Override
    public void start(AppContext context) throws Throwable {
        context.getBeanAsync(ServiceProperties.class, bean -> {
            bean.afterPropertiesSet();
        });

        PropertiesHelper.initBeanMap();

        //for autoconfigure
        context.beanScan(PropertiesHelper.class);
        context.beanMake(SeataAutoConfiguration.class);
        context.beanMake(SeataSagaAutoConfiguration.class);
        context.beanMake(SagaAsyncThreadPoolExecutorConfiguration.class);

        SeataProperties seataProperties = context.getBean(SeataProperties.class);
        SeataAutoDataSourceProxyCreator seataAutoDataSourceProxyCreator = new SeataAutoDataSourceProxyCreator(seataProperties.getDataSourceProxyMode());

        //for dataSource proxy
        context.subWrapsOfType(DataSource.class, bw -> {
            bw.proxySet(seataAutoDataSourceProxyCreator);
        }, Integer.MIN_VALUE);

        //for nami
        if (ClassUtil.hasClass(() -> NamiManager.class)) {
            NamiManager.reg(new SeataNamiFilter());
        }

        //for http-utils
        if (ClassUtil.hasClass(() -> HttpExtensionManager.class)) {
            HttpExtensionManager.add(new SeataHttpExtension());
        }

        //for solon
        context.app().routerInterceptor(Integer.MIN_VALUE, new SeataSolonRouterInterceptor());

        //for annotation
        GlobalTransactionalInterceptor globalTransactionalInterceptor = new GlobalTransactionalInterceptor();
        context.beanInterceptorAdd(GlobalLock.class, globalTransactionalInterceptor);
        context.beanInterceptorAdd(GlobalTransactional.class, globalTransactionalInterceptor);

        TccActionInterceptor tccActionInterceptor = new TccActionInterceptor();
        context.beanInterceptorAdd(TwoPhaseBusinessAction.class, tccActionInterceptor);

        AdapterSolonSeaterInterceptor adapterSolonSeaterInterceptor = new AdapterSolonSeaterInterceptor();
        context.beanInterceptorAdd(GlobalLock.class, adapterSolonSeaterInterceptor);
        context.beanInterceptorAdd(GlobalTransactional.class, adapterSolonSeaterInterceptor);
        context.beanInterceptorAdd(LocalTCC.class, adapterSolonSeaterInterceptor);
        context.beanInterceptorAdd(TwoPhaseBusinessAction.class, adapterSolonSeaterInterceptor);
    }
}