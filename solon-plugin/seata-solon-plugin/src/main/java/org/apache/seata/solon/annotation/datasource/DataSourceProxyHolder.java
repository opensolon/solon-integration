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

import org.apache.seata.rm.datasource.SeataDataSourceProxy;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * the type data source proxy holder
 *
 */
public class DataSourceProxyHolder {

    private static final Map<DataSource, SeataDataSourceProxy> PROXY_MAP = new ConcurrentHashMap<>(4);

    static SeataDataSourceProxy put(DataSource origin, SeataDataSourceProxy proxy) {
        return PROXY_MAP.put(origin, proxy);
    }

    static SeataDataSourceProxy computeIfAbsent(DataSource origin, Function<DataSource, SeataDataSourceProxy> mappingFunction) {
        return PROXY_MAP.computeIfAbsent(origin, mappingFunction);
    }

    static SeataDataSourceProxy get(DataSource origin) {
        return PROXY_MAP.get(origin);
    }
}
