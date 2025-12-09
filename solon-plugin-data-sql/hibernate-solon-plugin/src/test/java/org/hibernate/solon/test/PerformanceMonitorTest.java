package org.hibernate.solon.test;

import org.hibernate.SessionFactory;
import org.hibernate.solon.annotation.Db;
import org.hibernate.solon.integration.monitor.PerformanceMonitor;
import org.hibernate.solon.integration.monitor.SlowQueryDetector;
import org.noear.solon.annotation.Component;

/**
 * 性能监控测试类
 * 
 * @author noear
 * @since 3.4
 */
@Component
public class PerformanceMonitorTest {
    
    @Db
    private SessionFactory sessionFactory;
    
    /**
     * 测试性能监控
     */
    public void testPerformanceMonitor() {
        PerformanceMonitor monitor = new PerformanceMonitor(sessionFactory);
        
        // 获取性能报告
        String report = monitor.getPerformanceReport();
        System.out.println(report);
        
        // 获取缓存命中率
        System.out.println("二级缓存命中率: " + String.format("%.2f%%", monitor.getSecondLevelCacheHitRate() * 100));
        System.out.println("查询缓存命中率: " + String.format("%.2f%%", monitor.getQueryCacheHitRate() * 100));
        
        // 获取统计信息
        System.out.println("查询执行总数: " + monitor.getQueryExecutionCount());
        System.out.println("实体加载总数: " + monitor.getEntityLoadCount());
        System.out.println("事务总数: " + monitor.getTransactionCount());
    }
    
    /**
     * 测试慢查询检测
     */
    public void testSlowQueryDetector() {
        SlowQueryDetector detector = new SlowQueryDetector(sessionFactory, 1000); // 阈值1秒
        
        // 检测并记录慢查询
        detector.logSlowQueries();
        
        // 获取慢查询列表
        java.util.List<SlowQueryDetector.SlowQueryInfo> slowQueries = detector.detectSlowQueries();
        System.out.println("检测到 " + slowQueries.size() + " 个慢查询");
        
        for (SlowQueryDetector.SlowQueryInfo info : slowQueries) {
            System.out.println("慢查询: " + info.getQuery());
            System.out.println("  最大执行时间: " + info.getMaxExecutionTime() + " ms");
            System.out.println("  平均执行时间: " + info.getAverageExecutionTime() + " ms");
            System.out.println("  执行次数: " + info.getExecutionCount());
        }
    }
}

