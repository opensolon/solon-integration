package org.hibernate.solon.integration.monitor;

import org.hibernate.SessionFactory;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 慢查询检测器
 * 
 * <p>检测执行时间超过阈值的查询</p>
 * 
 * @author noear
 * @since 3.4
 */
public class SlowQueryDetector {
    
    private static final Logger log = LoggerFactory.getLogger(SlowQueryDetector.class);
    
    private final Statistics statistics;
    private final long thresholdMillis;
    
    /**
     * 构造函数
     * 
     * @param sessionFactory SessionFactory
     * @param thresholdMillis 慢查询阈值（毫秒），默认1000ms
     */
    public SlowQueryDetector(SessionFactory sessionFactory, long thresholdMillis) {
        this.statistics = sessionFactory.getStatistics();
        this.thresholdMillis = thresholdMillis > 0 ? thresholdMillis : 1000;
    }
    
    /**
     * 构造函数（使用默认阈值1000ms）
     */
    public SlowQueryDetector(SessionFactory sessionFactory) {
        this(sessionFactory, 1000);
    }
    
    /**
     * 检测慢查询
     * 
     * @return 慢查询列表
     */
    public List<SlowQueryInfo> detectSlowQueries() {
        List<SlowQueryInfo> slowQueries = new ArrayList<>();
        
        if (!statistics.isStatisticsEnabled()) {
            log.warn("Statistics is not enabled, cannot detect slow queries");
            return slowQueries;
        }
        
        String[] queries = statistics.getQueries();
        
        for (String query : queries) {
            QueryStatistics queryStatistics = statistics.getQueryStatistics(query);
            
            long maxExecutionTime = queryStatistics.getExecutionMaxTime();
            
            if (maxExecutionTime > thresholdMillis) {
                SlowQueryInfo info = new SlowQueryInfo();
                info.setQuery(query);
                info.setMaxExecutionTime(maxExecutionTime);
                info.setExecutionCount(queryStatistics.getExecutionCount());
                info.setTotalExecutionTime(queryStatistics.getExecutionTotalTime());
                info.setAverageExecutionTime(queryStatistics.getExecutionTotalTime() / 
                    (queryStatistics.getExecutionCount() > 0 ? queryStatistics.getExecutionCount() : 1));
                
                slowQueries.add(info);
            }
        }
        
        return slowQueries;
    }
    
    /**
     * 记录慢查询日志
     */
    public void logSlowQueries() {
        List<SlowQueryInfo> slowQueries = detectSlowQueries();
        
        if (slowQueries.isEmpty()) {
            return;
        }
        
        log.warn("Detected {} slow queries (threshold: {} ms):", slowQueries.size(), thresholdMillis);
        
        for (SlowQueryInfo info : slowQueries) {
            log.warn("Slow Query: {} | Max Time: {} ms | Avg Time: {} ms | Count: {}", 
                info.getQuery(), 
                info.getMaxExecutionTime(),
                info.getAverageExecutionTime(),
                info.getExecutionCount());
        }
    }
    
    /**
     * 慢查询信息
     */
    public static class SlowQueryInfo {
        private String query;
        private long maxExecutionTime;
        private long executionCount;
        private long totalExecutionTime;
        private long averageExecutionTime;
        
        // Getters and Setters
        public String getQuery() {
            return query;
        }
        
        public void setQuery(String query) {
            this.query = query;
        }
        
        public long getMaxExecutionTime() {
            return maxExecutionTime;
        }
        
        public void setMaxExecutionTime(long maxExecutionTime) {
            this.maxExecutionTime = maxExecutionTime;
        }
        
        public long getExecutionCount() {
            return executionCount;
        }
        
        public void setExecutionCount(long executionCount) {
            this.executionCount = executionCount;
        }
        
        public long getTotalExecutionTime() {
            return totalExecutionTime;
        }
        
        public void setTotalExecutionTime(long totalExecutionTime) {
            this.totalExecutionTime = totalExecutionTime;
        }
        
        public long getAverageExecutionTime() {
            return averageExecutionTime;
        }
        
        public void setAverageExecutionTime(long averageExecutionTime) {
            this.averageExecutionTime = averageExecutionTime;
        }
    }
}

