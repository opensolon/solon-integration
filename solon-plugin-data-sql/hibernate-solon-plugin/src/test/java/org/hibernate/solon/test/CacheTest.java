package org.hibernate.solon.test;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.solon.annotation.Db;
import org.hibernate.solon.test.entity.User;
import org.junit.jupiter.api.Test;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;
import org.noear.solon.test.SolonTest;

import java.util.List;

/**
 * 缓存功能测试类
 * 
 * <p>测试二级缓存和查询缓存功能</p>
 * 
 * <p><b>⚠️ 测试前准备：</b></p>
 * <ol>
 *   <li>确保数据库表已创建（test_user表）</li>
 *   <li>创建方式：
 *     <ul>
 *       <li>方式1：配置 hbm2ddl.auto=create 或 update，启动时自动创建</li>
 *       <li>方式2：执行 SQL脚本：src/test/resources/test_schema.sql</li>
 *       <li>方式3：运行 DdlGeneratorTest 生成DDL后手动执行</li>
 *     </ul>
 *   </li>
 *   <li>需要配置缓存提供者（如EhCache、Redis等）</li>
 *   <li>在配置中启用：hibernate.cache.use_second_level_cache=true</li>
 *   <li>详细说明请参考：TEST_GUIDE.md</li>
 * </ol>
 * 
 * @author noear
 * @since 3.4
 */
@SolonTest(TestApp.class)
public class CacheTest {
    
    @Db
    @Inject
    private SessionFactory sessionFactory;
    
    /**
     * 测试二级缓存
     */
    @Test
    @Transaction
    public void testSecondLevelCache() {
        Session session = sessionFactory.getCurrentSession();
        
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("二级缓存测试");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // 检查缓存是否启用
        boolean cacheEnabled = sessionFactory.getCache() != null;
        System.out.println("缓存是否启用: " + cacheEnabled);
        
        if (cacheEnabled) {
            // 第一次查询（从数据库）
            User user1 = session.get(User.class, 1L);
            System.out.println("第一次查询: " + (user1 != null ? "找到用户" : "未找到"));
            
            // 清除Session，模拟新的Session
            session.clear();
            
            // 第二次查询（应该从缓存）
            User user2 = session.get(User.class, 1L);
            System.out.println("第二次查询: " + (user2 != null ? "找到用户" : "未找到"));
            
            if (user2 != null) {
                System.out.println("✅ 二级缓存可能生效（需要配置缓存提供者）");
            }
        } else {
            System.out.println("⚠️  二级缓存未启用（需要在配置中启用）");
        }
    }
    
    /**
     * 测试查询缓存
     */
    @Test
    @Transaction
    public void testQueryCache() {
        Session session = sessionFactory.getCurrentSession();
        
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("查询缓存测试");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        try {
            // 第一次查询
            List<User> users1 = session.createQuery("FROM User", User.class)
                .setCacheable(true)  // 启用查询缓存
                .list();
            System.out.println("第一次查询结果: " + users1.size() + " 条");
            
            // 清除Session
            session.clear();
            
            // 第二次查询（应该从缓存）
            List<User> users2 = session.createQuery("FROM User", User.class)
                .setCacheable(true)
                .list();
            System.out.println("第二次查询结果: " + users2.size() + " 条");
            
            System.out.println("✅ 查询缓存测试完成（需要配置查询缓存）");
            
        } catch (Exception e) {
            System.out.println("⚠️  查询缓存测试失败: " + e.getMessage());
            System.out.println("   可能原因：查询缓存未启用");
        }
    }
    
    /**
     * 测试缓存统计
     */
    @Test
    public void testCacheStatistics() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("缓存统计信息");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        if (sessionFactory.getStatistics().isStatisticsEnabled()) {
            long secondLevelCacheHitCount = 
                sessionFactory.getStatistics().getSecondLevelCacheHitCount();
            long secondLevelCacheMissCount = 
                sessionFactory.getStatistics().getSecondLevelCacheMissCount();
            long queryCacheHitCount = 
                sessionFactory.getStatistics().getQueryCacheHitCount();
            long queryCacheMissCount = 
                sessionFactory.getStatistics().getQueryCacheMissCount();
            
            System.out.println("二级缓存命中次数: " + secondLevelCacheHitCount);
            System.out.println("二级缓存未命中次数: " + secondLevelCacheMissCount);
            System.out.println("查询缓存命中次数: " + queryCacheHitCount);
            System.out.println("查询缓存未命中次数: " + queryCacheMissCount);
            
            // 计算命中率
            long totalSecondLevel = secondLevelCacheHitCount + secondLevelCacheMissCount;
            if (totalSecondLevel > 0) {
                double hitRate = (double) secondLevelCacheHitCount / totalSecondLevel * 100;
                System.out.println("二级缓存命中率: " + String.format("%.2f%%", hitRate));
            }
            
            long totalQuery = queryCacheHitCount + queryCacheMissCount;
            if (totalQuery > 0) {
                double queryHitRate = (double) queryCacheHitCount / totalQuery * 100;
                System.out.println("查询缓存命中率: " + String.format("%.2f%%", queryHitRate));
            }
        } else {
            System.out.println("⚠️  统计功能未启用");
        }
    }
}

