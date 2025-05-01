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
package org.apache.seata.saga.engine.tm;

import org.apache.seata.common.exception.FrameworkErrorCode;
import org.apache.seata.core.exception.TransactionException;
import org.apache.seata.core.model.BranchStatus;
import org.apache.seata.core.model.BranchType;
import org.apache.seata.core.model.GlobalStatus;
import org.apache.seata.rm.DefaultResourceManager;
import org.apache.seata.saga.engine.exception.EngineExecutionException;
import org.apache.seata.tm.api.GlobalTransaction;
import org.apache.seata.tm.api.GlobalTransactionContext;
import org.apache.seata.tm.api.GlobalTransactionRole;
import org.apache.seata.tm.api.TransactionalExecutor;
import org.apache.seata.tm.api.TransactionalExecutor.ExecutionException;
import org.apache.seata.tm.api.transaction.TransactionHook;
import org.apache.seata.tm.api.transaction.TransactionHookManager;
import org.apache.seata.tm.api.transaction.TransactionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Template of executing business logic with a global transaction for SAGA mode
 */
public class DefaultSagaTransactionalTemplate
        implements SagaTransactionalTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSagaTransactionalTemplate.class);
    
    @Override
    public void commitTransaction(GlobalTransaction tx) throws ExecutionException {
        try {
            triggerBeforeCommit(tx);
            tx.commit();
            triggerAfterCommit(tx);
        } catch (TransactionException txe) {
            // 4.1 Failed to commit
            throw new ExecutionException(tx, txe, TransactionalExecutor.Code.CommitFailure);
        }
    }

    @Override
    public void rollbackTransaction(GlobalTransaction tx, Throwable ex)
            throws TransactionException, ExecutionException {
        triggerBeforeRollback(tx);
        tx.rollback();
        triggerAfterRollback(tx);
        // Successfully rolled back
    }

    @Override
    public GlobalTransaction beginTransaction(TransactionInfo txInfo) throws ExecutionException {
        GlobalTransaction tx = GlobalTransactionContext.getCurrentOrCreate();
        try {
            triggerBeforeBegin(tx);
            tx.begin(txInfo.getTimeOut(), txInfo.getName());
            triggerAfterBegin(tx);
        } catch (TransactionException txe) {
            throw new ExecutionException(tx, txe, TransactionalExecutor.Code.BeginFailure);
        }
        return tx;
    }

    @Override
    public GlobalTransaction reloadTransaction(String xid) throws ExecutionException, TransactionException {
        return GlobalTransactionContext.reload(xid);
    }

    @Override
    public void reportTransaction(GlobalTransaction tx, GlobalStatus globalStatus)
            throws ExecutionException {
        try {
            tx.globalReport(globalStatus);
            triggerAfterCompletion(tx);
        } catch (TransactionException txe) {

            throw new ExecutionException(tx, txe, TransactionalExecutor.Code.ReportFailure);
        }
    }

    @Override
    public long branchRegister(String resourceId, String clientId, String xid, String applicationData, String lockKeys)
            throws TransactionException {
        return DefaultResourceManager.get().branchRegister(BranchType.SAGA, resourceId, clientId, xid, applicationData,
                lockKeys);
    }

    @Override
    public void branchReport(String xid, long branchId, BranchStatus status, String applicationData)
            throws TransactionException {
        DefaultResourceManager.get().branchReport(BranchType.SAGA, xid, branchId, status, applicationData);
    }

    protected void triggerBeforeBegin(GlobalTransaction tx) {
        if (tx.getGlobalTransactionRole() == GlobalTransactionRole.Launcher) {
            for (TransactionHook hook : getCurrentHooks()) {
                try {
                    hook.beforeBegin();
                } catch (Exception e) {
                    LOGGER.error("Failed execute beforeBegin in hook {}", e.getMessage(), e);
                }
            }
        }
    }

    protected void triggerAfterBegin(GlobalTransaction tx) {
        if (tx.getGlobalTransactionRole() == GlobalTransactionRole.Launcher) {
            for (TransactionHook hook : getCurrentHooks()) {
                try {
                    hook.afterBegin();
                } catch (Exception e) {
                    LOGGER.error("Failed execute afterBegin in hook {} ", e.getMessage(), e);
                }
            }
        }
    }

    protected void triggerBeforeRollback(GlobalTransaction tx) {
        if (tx.getGlobalTransactionRole() == GlobalTransactionRole.Launcher) {
            for (TransactionHook hook : getCurrentHooks()) {
                try {
                    hook.beforeRollback();
                } catch (Exception e) {
                    LOGGER.error("Failed execute beforeRollback in hook {} ", e.getMessage(), e);
                }
            }
        }
    }

    protected void triggerAfterRollback(GlobalTransaction tx) {
        if (tx.getGlobalTransactionRole() == GlobalTransactionRole.Launcher) {
            for (TransactionHook hook : getCurrentHooks()) {
                try {
                    hook.afterRollback();
                } catch (Exception e) {
                    LOGGER.error("Failed execute afterRollback in hook {}", e.getMessage(), e);
                }
            }
        }
    }

    protected void triggerBeforeCommit(GlobalTransaction tx) {
        if (tx.getGlobalTransactionRole() == GlobalTransactionRole.Launcher) {
            for (TransactionHook hook : getCurrentHooks()) {
                try {
                    hook.beforeCommit();
                } catch (Exception e) {
                    LOGGER.error("Failed execute beforeCommit in hook {}", e.getMessage(), e);
                }
            }
        }
    }

    protected void triggerAfterCommit(GlobalTransaction tx) {
        if (tx.getGlobalTransactionRole() == GlobalTransactionRole.Launcher) {
            for (TransactionHook hook : getCurrentHooks()) {
                try {
                    hook.afterCommit();
                } catch (Exception e) {
                    LOGGER.error("Failed execute afterCommit in hook {}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void triggerAfterCompletion(GlobalTransaction tx) {
        if (tx.getGlobalTransactionRole() == GlobalTransactionRole.Launcher) {
            for (TransactionHook hook : getCurrentHooks()) {
                try {
                    hook.afterCompletion();
                } catch (Exception e) {
                    LOGGER.error("Failed execute afterCompletion in hook {}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void cleanUp(GlobalTransaction tx) {
        if (tx == null) {
            throw new EngineExecutionException("Global transaction does not exist. Unable to proceed without a valid global transaction context.",
                    FrameworkErrorCode.ObjectNotExists);
        }
        if (tx.getGlobalTransactionRole() == GlobalTransactionRole.Launcher) {
            TransactionHookManager.clear();
        }
    }

    protected List<TransactionHook> getCurrentHooks() {
        return TransactionHookManager.getHooks();
    }
}
