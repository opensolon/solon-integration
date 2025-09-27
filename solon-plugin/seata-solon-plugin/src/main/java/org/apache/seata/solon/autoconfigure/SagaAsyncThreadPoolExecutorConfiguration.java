package org.apache.seata.solon.autoconfigure;

import org.apache.seata.solon.autoconfigure.properties.SagaAsyncThreadPoolProperties;
import org.apache.seata.solon.autoconfigure.suuport.ThreadPoolExecutorFactoryBean;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Condition;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.apache.seata.solon.autoconfigure.SeataSagaAutoConfiguration.SAGA_ASYNC_THREAD_POOL_EXECUTOR_BEAN_NAME;
import static org.apache.seata.solon.autoconfigure.SeataSagaAutoConfiguration.SAGA_REJECTED_EXECUTION_HANDLER_BEAN_NAME;
import static org.apache.seata.solon.autoconfigure.StarterConstants.SAGA_ASYNC_THREAD_POOL_PREFIX;

/**
 * The saga async thread pool executor configuration.
 */
@Configuration
@Condition(onExpression = "${" + SAGA_ASYNC_THREAD_POOL_PREFIX + ".enableAsync:false} == 'true'")
public class SagaAsyncThreadPoolExecutorConfiguration {

    /**
     * Create rejected execution handler bean.
     */
    @Bean(SAGA_REJECTED_EXECUTION_HANDLER_BEAN_NAME)
    @Condition(onMissingBean = RejectedExecutionHandler.class)
    public RejectedExecutionHandler sagaRejectedExecutionHandler() {
        return new ThreadPoolExecutor.CallerRunsPolicy();
    }

    /**
     * Create state machine async thread pool executor bean.
     */
    @Bean(SAGA_ASYNC_THREAD_POOL_EXECUTOR_BEAN_NAME)
    @Condition(onMissingBean = ThreadPoolExecutor.class)
    public ThreadPoolExecutor sagaAsyncThreadPoolExecutor(
            SagaAsyncThreadPoolProperties properties,
            @Inject(SAGA_REJECTED_EXECUTION_HANDLER_BEAN_NAME) RejectedExecutionHandler rejectedExecutionHandler) {
        ThreadPoolExecutorFactoryBean threadFactory = new ThreadPoolExecutorFactoryBean();
        threadFactory.setBeanName("sagaStateMachineThreadPoolExecutorFactory");
        threadFactory.setThreadNamePrefix("sagaAsyncExecute-");
        threadFactory.setCorePoolSize(properties.getCorePoolSize());
        threadFactory.setMaxPoolSize(properties.getMaxPoolSize());
        threadFactory.setKeepAliveSeconds(properties.getKeepAliveTime());

        return new ThreadPoolExecutor(
                properties.getCorePoolSize(),
                properties.getMaxPoolSize(),
                properties.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                threadFactory,
                rejectedExecutionHandler
        );
    }
}
