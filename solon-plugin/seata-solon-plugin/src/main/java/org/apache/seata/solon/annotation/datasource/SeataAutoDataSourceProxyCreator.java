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
package org.apache.seata.solon.annotation.datasource;

import org.apache.seata.core.model.BranchType;
import org.apache.seata.rm.datasource.SeataDataSourceProxy;
import org.apache.seata.rm.datasource.xa.DataSourceProxyXA;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.data.datasource.RoutingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * @author noear 2024/10/25 created
 */
public class SeataAutoDataSourceProxyCreator implements BeanWrap.Proxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeataAutoDataSourceProxyCreator.class);

    private final String dataSourceProxyMode;

    public SeataAutoDataSourceProxyCreator(String dataSourceProxyMode) {
        this.dataSourceProxyMode = dataSourceProxyMode;
    }

    @Override
    public Object getProxy(BeanWrap bw, Object bean) {
        // we only care DataSource bean
        if (!(bean instanceof DataSource)) {
            return bean;
        }

        // when this bean is just a simple DataSource, not SeataDataSourceProxy
        if (!(bean instanceof SeataDataSourceProxy)) {
            // else, build proxy,  put <origin, proxy> to holder and return enhancer
            DataSource origin = (DataSource) bean;
            SeataDataSourceProxy proxy = buildProxy(origin, dataSourceProxyMode);
            DataSourceProxyHolder.put(origin, proxy);
            LOGGER.info("Auto proxy data source '{}' by '{}' mode.", bw.name(), dataSourceProxyMode);
            return proxy;
        }

        return bean;
    }

    protected SeataDataSourceProxy buildProxy(DataSource origin, String proxyMode) {
        if (BranchType.AT.name().equalsIgnoreCase(proxyMode)) {
            if (origin instanceof RoutingDataSource) {
                return new RoutingDataSourceProxyAT(origin);
            } else {
                return new org.apache.seata.rm.datasource.DataSourceProxy(origin);
            }
        }

        if (BranchType.XA.name().equalsIgnoreCase(proxyMode)) {
            if (origin instanceof RoutingDataSource) {
                return new RoutingDataSourceProxyXA(origin);
            } else {
                return new DataSourceProxyXA(origin);
            }
        }

        throw new IllegalArgumentException("Unknown dataSourceProxyMode: " + proxyMode);
    }
}
