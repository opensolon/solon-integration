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
package org.apache.seata.solon.autoconfigure.properties.client;

import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

import static org.apache.seata.common.DefaultValues.*;
import static org.apache.seata.solon.autoconfigure.StarterConstants.UNDO_PREFIX;

@Configuration
@Inject(value = "${" + UNDO_PREFIX + "}", required = false)
public class UndoProperties {
    private boolean dataValidation = DEFAULT_TRANSACTION_UNDO_DATA_VALIDATION;
    private String logSerialization = DEFAULT_TRANSACTION_UNDO_LOG_SERIALIZATION;
    private String logTable = DEFAULT_TRANSACTION_UNDO_LOG_TABLE;
    private boolean onlyCareUpdateColumns = DEFAULT_ONLY_CARE_UPDATE_COLUMNS;

    public boolean isDataValidation() {
        return dataValidation;
    }

    public UndoProperties setDataValidation(boolean dataValidation) {
        this.dataValidation = dataValidation;
        return this;
    }

    public String getLogSerialization() {
        return logSerialization;
    }

    public UndoProperties setLogSerialization(String logSerialization) {
        this.logSerialization = logSerialization;
        return this;
    }

    public String getLogTable() {
        return logTable;
    }

    public UndoProperties setLogTable(String logTable) {
        this.logTable = logTable;
        return this;
    }

    public boolean isOnlyCareUpdateColumns() {
        return onlyCareUpdateColumns;
    }

    public UndoProperties setOnlyCareUpdateColumns(boolean onlyCareUpdateColumns) {
        this.onlyCareUpdateColumns = onlyCareUpdateColumns;
        return this;
    }
}
