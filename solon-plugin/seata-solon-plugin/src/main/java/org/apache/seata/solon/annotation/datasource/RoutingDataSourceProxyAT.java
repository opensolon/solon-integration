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

import org.apache.seata.rm.datasource.DataSourceProxy;
import org.apache.seata.rm.datasource.SeataDataSourceProxy;
import org.noear.solon.data.datasource.RoutingDataSource;

import javax.sql.DataSource;

/**
 * @author noear 2024/10/26 created
 */
public class RoutingDataSourceProxyAT extends DataSourceProxy implements RoutingDataSource {
    public RoutingDataSourceProxyAT(DataSource targetDataSource) {
        super(targetDataSource);
    }

    public RoutingDataSourceProxyAT(DataSource targetDataSource, String resourceGroupId) {
        super(targetDataSource, resourceGroupId);
    }

    @Override
    public DataSource determineCurrentTarget() {
        DataSource dataSource = ((RoutingDataSource) getTargetDataSource()).determineCurrentTarget();

        if (dataSource instanceof SeataDataSourceProxy == false) {
            dataSource = DataSourceProxyHolder.computeIfAbsent(dataSource, DataSourceProxy::new);
        }

        return dataSource;
    }
}
