package org.hibernate.solon.integration.monitor;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

/**
 * 性能监控器
 * 
 * <p>监控Hibernate的性能指标，包括查询统计、缓存统计等</p>
 * 
 * @author noear
 * @since 3.4
 */
public class PerformanceMonitor {
    
    private final Statistics statistics;
    
    public PerformanceMonitor(SessionFactory sessionFactory) {
        this.statistics = sessionFactory.getStatistics();
    }
    
    /**
     * 获取统计信息
     */
    public Statistics getStatistics() {
        return statistics;
    }
    
    /**
     * 获取查询执行总数
     */
    public long getQueryExecutionCount() {
        return statistics.getQueryExecutionCount();
    }
    
    /**
     * 获取查询执行总时间（毫秒）
     */
    public long getQueryExecutionMaxTime() {
        return statistics.getQueryExecutionMaxTime();
    }
    
    /**
     * 获取慢查询数量（超过指定阈值的查询）
     */
    public long getSlowQueryCount(long thresholdMillis) {
        // 注意：Hibernate Statistics不直接提供慢查询统计
        // 这里需要结合日志或其他方式实现
        return 0;
    }
    
    /**
     * 获取实体加载总数
     */
    public long getEntityLoadCount() {
        return statistics.getEntityLoadCount();
    }
    
    /**
     * 获取实体获取总数
     */
    public long getEntityFetchCount() {
        return statistics.getEntityFetchCount();
    }
    
    /**
     * 获取集合加载总数
     */
    public long getCollectionLoadCount() {
        return statistics.getCollectionLoadCount();
    }
    
    /**
     * 获取二级缓存命中数
     */
    public long getSecondLevelCacheHitCount() {
        return statistics.getSecondLevelCacheHitCount();
    }
    
    /**
     * 获取二级缓存未命中数
     */
    public long getSecondLevelCacheMissCount() {
        return statistics.getSecondLevelCacheMissCount();
    }
    
    /**
     * 获取查询缓存命中数
     */
    public long getQueryCacheHitCount() {
        return statistics.getQueryCacheHitCount();
    }
    
    /**
     * 获取查询缓存未命中数
     */
    public long getQueryCacheMissCount() {
        return statistics.getQueryCacheMissCount();
    }
    
    /**
     * 获取连接总数
     */
    public long getConnectCount() {
        return statistics.getConnectCount();
    }
    
    /**
     * 获取事务总数
     */
    public long getTransactionCount() {
        return statistics.getTransactionCount();
    }
    
    /**
     * 获取成功事务数
     */
    public long getSuccessfulTransactionCount() {
        return statistics.getSuccessfulTransactionCount();
    }
    
    /**
     * 获取缓存命中率（二级缓存）
     */
    public double getSecondLevelCacheHitRate() {
        long hits = getSecondLevelCacheHitCount();
        long misses = getSecondLevelCacheMissCount();
        long total = hits + misses;
        return total > 0 ? (double) hits / total : 0.0;
    }
    
    /**
     * 获取查询缓存命中率
     */
    public double getQueryCacheHitRate() {
        long hits = getQueryCacheHitCount();
        long misses = getQueryCacheMissCount();
        long total = hits + misses;
        return total > 0 ? (double) hits / total : 0.0;
    }
    
    /**
     * 获取性能报告（字符串格式）
     */
    public String getPerformanceReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Hibernate Performance Report ===\n");
        report.append("Query Execution Count: ").append(getQueryExecutionCount()).append("\n");
        report.append("Query Execution Max Time: ").append(getQueryExecutionMaxTime()).append(" ms\n");
        report.append("Entity Load Count: ").append(getEntityLoadCount()).append("\n");
        report.append("Entity Fetch Count: ").append(getEntityFetchCount()).append("\n");
        report.append("Collection Load Count: ").append(getCollectionLoadCount()).append("\n");
        report.append("Second Level Cache Hit Rate: ").append(String.format("%.2f%%", getSecondLevelCacheHitRate() * 100)).append("\n");
        report.append("Query Cache Hit Rate: ").append(String.format("%.2f%%", getQueryCacheHitRate() * 100)).append("\n");
        report.append("Transaction Count: ").append(getTransactionCount()).append("\n");
        report.append("Successful Transaction Count: ").append(getSuccessfulTransactionCount()).append("\n");
        return report.toString();
    }
    
    /**
     * 清空统计信息
     */
    public void clear() {
        statistics.clear();
    }
}

