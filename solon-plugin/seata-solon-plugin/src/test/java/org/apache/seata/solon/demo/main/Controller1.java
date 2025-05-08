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
package org.apache.seata.solon.demo.main;

import org.apache.seata.spring.annotation.GlobalTransactional;
import org.noear.solon.annotation.Body;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.net.http.HttpUtils;

import java.util.Map;

/**
 * @author noear 2024/10/28 created
 */
@Controller
public class Controller1 {
    @GlobalTransactional
    @Mapping
    public void add(@Body Map<String, Object> params) throws Exception {
        HttpUtils.http("http://localhost:8082/user")
                .data(params)
                .post();

        HttpUtils.http("http://localhost:8082/order")
                .data(params)
                .post();
    }
}
