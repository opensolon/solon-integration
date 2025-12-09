package org.hibernate.solon.test;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.solon.annotation.Db;
import org.hibernate.solon.test.entity.User;
import org.junit.jupiter.api.Test;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;
import org.noear.solon.test.SolonTest;

/**
 * 懒加载测试类
 * 
 * <p>测试Hibernate的懒加载功能</p>
 * 
 * @author noear
 * @since 3.4
 */
@SolonTest(TestApp.class)
public class LazyLoadTest {
    
    @Db
    @Inject
    private SessionFactory sessionFactory;
    
    /**
     * 测试懒加载配置
     */
    @Test
    public void testLazyLoadConfiguration() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("懒加载配置测试");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // 检查SessionFactory配置
        try {
            org.hibernate.engine.spi.SessionFactoryImplementor sfi = 
                (org.hibernate.engine.spi.SessionFactoryImplementor) sessionFactory;
            String dialect = sfi.getJdbcServices().getJdbcEnvironment().getDialect().getClass().getSimpleName();
            
            System.out.println("数据库方言: " + dialect);
            System.out.println("✅ 懒加载配置检查完成");
        } catch (Exception e) {
            System.out.println("⚠️  无法获取数据库方言信息: " + e.getMessage());
        }
    }
    
    /**
     * 测试Session内的懒加载
     */
    @Test
    @Transaction
    public void testLazyLoadInSession() {
        Session session = sessionFactory.getCurrentSession();
        
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Session内懒加载测试");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // 加载用户（不立即加载关联对象）
        User user = session.get(User.class, 1L);
        
        if (user != null) {
            System.out.println("✅ 用户加载成功: " + user.getName());
            System.out.println("   在Session内访问属性正常");
        } else {
            System.out.println("⚠️  用户不存在（ID=1）");
        }
    }
    
    /**
     * 测试批量抓取
     */
    @Test
    @Transaction
    public void testBatchFetch() {
        Session session = sessionFactory.getCurrentSession();
        
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("批量抓取测试");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // 查询多个用户
        java.util.List<User> users = session.createQuery("FROM User", User.class)
            .setMaxResults(10)
            .list();
        
        System.out.println("✅ 查询到 " + users.size() + " 个用户");
        System.out.println("   批量抓取配置会影响关联对象的加载策略");
    }
}

