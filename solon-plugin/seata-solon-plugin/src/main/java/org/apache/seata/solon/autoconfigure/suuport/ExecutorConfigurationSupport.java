package org.apache.seata.solon.autoconfigure.suuport;


import org.noear.solon.lang.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public abstract class ExecutorConfigurationSupport extends CustomizableThreadFactory {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorConfigurationSupport.class);

    private ThreadFactory threadFactory = this;

    private boolean threadNamePrefixSet = false;

    private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();

    private boolean waitForTasksToCompleteOnShutdown = false;

    private long awaitTerminationMillis = 0;

    @Nullable
    private String beanName;

    @Nullable
    private ExecutorService executor;


    /**
     * Set the ThreadFactory to use for the ExecutorService's thread pool.
     * Default is the underlying ExecutorService's default thread factory.
     * <p>In a Java EE 7 or other managed environment with JSR-236 support,
     * consider specifying a JNDI-located ManagedThreadFactory: by default,
     * to be found at "java:comp/DefaultManagedThreadFactory".
     * Use the "jee:jndi-lookup" namespace element in XML or the programmatic
     * {@link org.springframework.jndi.JndiLocatorDelegate} for convenient lookup.
     * Alternatively, consider using Spring's {@link DefaultManagedAwareThreadFactory}
     * with its fallback to local threads in case of no managed thread factory found.
     *
     * @see java.util.concurrent.Executors#defaultThreadFactory()
     * @see javax.enterprise.concurrent.ManagedThreadFactory
     * @see DefaultManagedAwareThreadFactory
     */
    public void setThreadFactory(@Nullable ThreadFactory threadFactory) {
        this.threadFactory = (threadFactory != null ? threadFactory : this);
    }

    @Override
    public void setThreadNamePrefix(@Nullable String threadNamePrefix) {
        super.setThreadNamePrefix(threadNamePrefix);
        this.threadNamePrefixSet = true;
    }

    /**
     * Set the RejectedExecutionHandler to use for the ExecutorService.
     * Default is the ExecutorService's default abort policy.
     *
     * @see java.util.concurrent.ThreadPoolExecutor.AbortPolicy
     */
    public void setRejectedExecutionHandler(@Nullable RejectedExecutionHandler rejectedExecutionHandler) {
        this.rejectedExecutionHandler =
                (rejectedExecutionHandler != null ? rejectedExecutionHandler : new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * Set whether to wait for scheduled tasks to complete on shutdown,
     * not interrupting running tasks and executing all tasks in the queue.
     * <p>Default is {@code false}, shutting down immediately through interrupting
     * ongoing tasks and clearing the queue. Switch this flag to {@code true} if
     * you prefer fully completed tasks at the expense of a longer shutdown phase.
     * <p>Note that Spring's container shutdown continues while ongoing tasks
     * are being completed. If you want this executor to block and wait for the
     * termination of tasks before the rest of the container continues to shut
     * down - e.g. in order to keep up other resources that your tasks may need -,
     * set the {@link #setAwaitTerminationSeconds "awaitTerminationSeconds"}
     * property instead of or in addition to this property.
     *
     * @see java.util.concurrent.ExecutorService#shutdown()
     * @see java.util.concurrent.ExecutorService#shutdownNow()
     */
    public void setWaitForTasksToCompleteOnShutdown(boolean waitForJobsToCompleteOnShutdown) {
        this.waitForTasksToCompleteOnShutdown = waitForJobsToCompleteOnShutdown;
    }

    /**
     * Set the maximum number of seconds that this executor is supposed to block
     * on shutdown in order to wait for remaining tasks to complete their execution
     * before the rest of the container continues to shut down. This is particularly
     * useful if your remaining tasks are likely to need access to other resources
     * that are also managed by the container.
     * <p>By default, this executor won't wait for the termination of tasks at all.
     * It will either shut down immediately, interrupting ongoing tasks and clearing
     * the remaining task queue - or, if the
     * {@link #setWaitForTasksToCompleteOnShutdown "waitForTasksToCompleteOnShutdown"}
     * flag has been set to {@code true}, it will continue to fully execute all
     * ongoing tasks as well as all remaining tasks in the queue, in parallel to
     * the rest of the container shutting down.
     * <p>In either case, if you specify an await-termination period using this property,
     * this executor will wait for the given time (max) for the termination of tasks.
     * As a rule of thumb, specify a significantly higher timeout here if you set
     * "waitForTasksToCompleteOnShutdown" to {@code true} at the same time,
     * since all remaining tasks in the queue will still get executed - in contrast
     * to the default shutdown behavior where it's just about waiting for currently
     * executing tasks that aren't reacting to thread interruption.
     *
     * @see #setAwaitTerminationMillis
     * @see java.util.concurrent.ExecutorService#shutdown()
     * @see java.util.concurrent.ExecutorService#awaitTermination
     */
    public void setAwaitTerminationSeconds(int awaitTerminationSeconds) {
        this.awaitTerminationMillis = awaitTerminationSeconds * 1000L;
    }

    /**
     * Variant of {@link #setAwaitTerminationSeconds} with millisecond precision.
     *
     * @see #setAwaitTerminationSeconds
     * @since 5.2.4
     */
    public void setAwaitTerminationMillis(long awaitTerminationMillis) {
        this.awaitTerminationMillis = awaitTerminationMillis;
    }

    public void setBeanName(String name) {
        this.beanName = name;
    }


    /**
     * Calls {@code initialize()} after the container applied all property values.
     *
     * @see #initialize()
     */
    public void afterPropertiesSet() {
        initialize();
    }

    /**
     * Set up the ExecutorService.
     */
    public void initialize() {
        if (logger.isDebugEnabled()) {
            logger.debug("Initializing ExecutorService" + (this.beanName != null ? " '" + this.beanName + "'" : ""));
        }
        if (!this.threadNamePrefixSet && this.beanName != null) {
            setThreadNamePrefix(this.beanName + "-");
        }
        this.executor = initializeExecutor(this.threadFactory, this.rejectedExecutionHandler);
    }

    /**
     * Create the target {@link java.util.concurrent.ExecutorService} instance.
     * Called by {@code afterPropertiesSet}.
     *
     * @param threadFactory            the ThreadFactory to use
     * @param rejectedExecutionHandler the RejectedExecutionHandler to use
     * @return a new ExecutorService instance
     * @see #afterPropertiesSet()
     */
    protected abstract ExecutorService initializeExecutor(
            ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler);


    /**
     * Calls {@code shutdown} when the BeanFactory destroys the executor instance.
     *
     * @see #shutdown()
     */
    public void destroy() {
        shutdown();
    }

    /**
     * Perform a full shutdown on the underlying ExecutorService,
     * according to the corresponding configuration settings.
     *
     * @see #setWaitForTasksToCompleteOnShutdown
     * @see #setAwaitTerminationMillis
     * @see java.util.concurrent.ExecutorService#shutdown()
     * @see java.util.concurrent.ExecutorService#shutdownNow()
     * @see java.util.concurrent.ExecutorService#awaitTermination
     */
    public void shutdown() {
        if (logger.isDebugEnabled()) {
            logger.debug("Shutting down ExecutorService" + (this.beanName != null ? " '" + this.beanName + "'" : ""));
        }
        if (this.executor != null) {
            if (this.waitForTasksToCompleteOnShutdown) {
                this.executor.shutdown();
            } else {
                for (Runnable remainingTask : this.executor.shutdownNow()) {
                    cancelRemainingTask(remainingTask);
                }
            }
            awaitTerminationIfNecessary(this.executor);
        }
    }

    /**
     * Cancel the given remaining task which never commended execution,
     * as returned from {@link ExecutorService#shutdownNow()}.
     *
     * @param task the task to cancel (typically a {@link RunnableFuture})
     * @see #shutdown()
     * @see RunnableFuture#cancel(boolean)
     * @since 5.0.5
     */
    protected void cancelRemainingTask(Runnable task) {
        if (task instanceof Future) {
            ((Future<?>) task).cancel(true);
        }
    }

    /**
     * Wait for the executor to terminate, according to the value of the
     * {@link #setAwaitTerminationSeconds "awaitTerminationSeconds"} property.
     */
    private void awaitTerminationIfNecessary(ExecutorService executor) {
        if (this.awaitTerminationMillis > 0) {
            try {
                if (!executor.awaitTermination(this.awaitTerminationMillis, TimeUnit.MILLISECONDS)) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Timed out while waiting for executor" +
                                (this.beanName != null ? " '" + this.beanName + "'" : "") + " to terminate");
                    }
                }
            } catch (InterruptedException ex) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Interrupted while waiting for executor" +
                            (this.beanName != null ? " '" + this.beanName + "'" : "") + " to terminate");
                }
                Thread.currentThread().interrupt();
            }
        }
    }

}

