package org.apache.seata.solon.autoconfigure.suuport;

import java.util.concurrent.*;

public class ThreadPoolExecutorFactoryBean extends ExecutorConfigurationSupport {

    String OBJECT_TYPE_ATTRIBUTE = "factoryBeanObjectType";

    private int corePoolSize = 1;

    private int maxPoolSize = Integer.MAX_VALUE;

    private int keepAliveSeconds = 60;

    private boolean allowCoreThreadTimeOut = false;

    private boolean prestartAllCoreThreads = false;

    private int queueCapacity = Integer.MAX_VALUE;

    private boolean exposeUnconfigurableExecutor = false;

    private ExecutorService exposedExecutor;


    /**
     * Set the ThreadPoolExecutor's core pool size.
     * Default is 1.
     */
    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    /**
     * Set the ThreadPoolExecutor's maximum pool size.
     * Default is {@code Integer.MAX_VALUE}.
     */
    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    /**
     * Set the ThreadPoolExecutor's keep-alive seconds.
     * Default is 60.
     */
    public void setKeepAliveSeconds(int keepAliveSeconds) {
        this.keepAliveSeconds = keepAliveSeconds;
    }

    /**
     * Specify whether to allow core threads to time out. This enables dynamic
     * growing and shrinking even in combination with a non-zero queue (since
     * the max pool size will only grow once the queue is full).
     * <p>Default is "false".
     *
     * @see java.util.concurrent.ThreadPoolExecutor#allowCoreThreadTimeOut(boolean)
     */
    public void setAllowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
    }

    /**
     * Specify whether to start all core threads, causing them to idly wait for work.
     * <p>Default is "false".
     *
     * @see java.util.concurrent.ThreadPoolExecutor#prestartAllCoreThreads
     * @since 5.3.14
     */
    public void setPrestartAllCoreThreads(boolean prestartAllCoreThreads) {
        this.prestartAllCoreThreads = prestartAllCoreThreads;
    }

    /**
     * Set the capacity for the ThreadPoolExecutor's BlockingQueue.
     * Default is {@code Integer.MAX_VALUE}.
     * <p>Any positive value will lead to a LinkedBlockingQueue instance;
     * any other value will lead to a SynchronousQueue instance.
     *
     * @see java.util.concurrent.LinkedBlockingQueue
     * @see java.util.concurrent.SynchronousQueue
     */
    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    /**
     * Specify whether this FactoryBean should expose an unconfigurable
     * decorator for the created executor.
     * <p>Default is "false", exposing the raw executor as bean reference.
     * Switch this flag to "true" to strictly prevent clients from
     * modifying the executor's configuration.
     *
     * @see java.util.concurrent.Executors#unconfigurableExecutorService
     */
    public void setExposeUnconfigurableExecutor(boolean exposeUnconfigurableExecutor) {
        this.exposeUnconfigurableExecutor = exposeUnconfigurableExecutor;
    }


    @Override
    protected ExecutorService initializeExecutor(
            ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {

        BlockingQueue<Runnable> queue = createQueue(this.queueCapacity);
        ThreadPoolExecutor executor = createExecutor(this.corePoolSize, this.maxPoolSize,
                this.keepAliveSeconds, queue, threadFactory, rejectedExecutionHandler);
        if (this.allowCoreThreadTimeOut) {
            executor.allowCoreThreadTimeOut(true);
        }
        if (this.prestartAllCoreThreads) {
            executor.prestartAllCoreThreads();
        }

        // Wrap executor with an unconfigurable decorator.
        this.exposedExecutor = (this.exposeUnconfigurableExecutor ?
                Executors.unconfigurableExecutorService(executor) : executor);

        return executor;
    }

    /**
     * Create a new instance of {@link ThreadPoolExecutor} or a subclass thereof.
     * <p>The default implementation creates a standard {@link ThreadPoolExecutor}.
     * Can be overridden to provide custom {@link ThreadPoolExecutor} subclasses.
     *
     * @param corePoolSize             the specified core pool size
     * @param maxPoolSize              the specified maximum pool size
     * @param keepAliveSeconds         the specified keep-alive time in seconds
     * @param queue                    the BlockingQueue to use
     * @param threadFactory            the ThreadFactory to use
     * @param rejectedExecutionHandler the RejectedExecutionHandler to use
     * @return a new ThreadPoolExecutor instance
     * @see #afterPropertiesSet()
     */
    protected ThreadPoolExecutor createExecutor(
            int corePoolSize, int maxPoolSize, int keepAliveSeconds, BlockingQueue<Runnable> queue,
            ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {

        return new ThreadPoolExecutor(corePoolSize, maxPoolSize,
                keepAliveSeconds, TimeUnit.SECONDS, queue, threadFactory, rejectedExecutionHandler);
    }

    /**
     * Create the BlockingQueue to use for the ThreadPoolExecutor.
     * <p>A LinkedBlockingQueue instance will be created for a positive
     * capacity value; a SynchronousQueue else.
     *
     * @param queueCapacity the specified queue capacity
     * @return the BlockingQueue instance
     * @see java.util.concurrent.LinkedBlockingQueue
     * @see java.util.concurrent.SynchronousQueue
     */
    protected BlockingQueue<Runnable> createQueue(int queueCapacity) {
        if (queueCapacity > 0) {
            return new LinkedBlockingQueue<>(queueCapacity);
        } else {
            return new SynchronousQueue<>();
        }
    }

    public ExecutorService getObject() {
        return this.exposedExecutor;
    }

    public Class<? extends ExecutorService> getObjectType() {
        return (this.exposedExecutor != null ? this.exposedExecutor.getClass() : ExecutorService.class);
    }
    
    public boolean isSingleton() {
        return true;
    }

}
