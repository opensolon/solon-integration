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
import static org.apache.seata.solon.autoconfigure.StarterConstants.LOCK_PREFIX;

@Configuration
@Inject(value = "${" + LOCK_PREFIX + "}",required = false)
public class LockProperties {
    private int retryInterval = DEFAULT_CLIENT_LOCK_RETRY_INTERVAL;
    private int retryTimes = DEFAULT_CLIENT_LOCK_RETRY_TIMES;
    private boolean retryPolicyBranchRollbackOnConflict = DEFAULT_CLIENT_LOCK_RETRY_POLICY_BRANCH_ROLLBACK_ON_CONFLICT;

    public int getRetryInterval() {
        return retryInterval;
    }

    public LockProperties setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
        return this;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public LockProperties setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
        return this;
    }

    public boolean isRetryPolicyBranchRollbackOnConflict() {
        return retryPolicyBranchRollbackOnConflict;
    }

    public LockProperties setRetryPolicyBranchRollbackOnConflict(boolean retryPolicyBranchRollbackOnConflict) {
        this.retryPolicyBranchRollbackOnConflict = retryPolicyBranchRollbackOnConflict;
        return this;
    }
}
