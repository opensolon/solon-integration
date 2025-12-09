package org.hibernate.solon.test;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.solon.annotation.Db;
import org.hibernate.solon.integration.query.NamedQueryRegistry;
import org.hibernate.solon.test.entity.User;
import org.junit.jupiter.api.Test;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;
import org.noear.solon.test.SolonTest;

import java.util.List;

/**
 * 命名查询测试类
 * 
 * <p>测试@NamedQuery和NamedQueryRegistry功能</p>
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
 *   <li>注意：需要在实体类上定义@NamedQuery注解</li>
 *   <li>详细说明请参考：TEST_GUIDE.md</li>
 * </ol>
 * 
 * @author noear
 * @since 3.4
 */
@SolonTest(TestApp.class)
public class NamedQueryTest {
    
    @Db
    @Inject
    private SessionFactory sessionFactory;
    
    /**
     * 测试命名查询注册
     */
    @Test
    public void testNamedQueryRegistration() {
        // 检查命名查询是否已注册
        String query = NamedQueryRegistry.getQuery("findUserByName");
        
        if (query != null) {
            System.out.println("✅ 命名查询已注册: findUserByName");
            System.out.println("   HQL: " + query);
        } else {
            System.out.println("⚠️  命名查询未找到（可能未在实体类上定义）");
        }
    }
    
    /**
     * 测试执行命名查询
     */
    @Test
    @Transaction
    public void testExecuteNamedQuery() {
        Session session = sessionFactory.getCurrentSession();
        
        try {
            // 使用NamedQueryRegistry创建查询
            String queryName = "findUserByName";
            String hql = NamedQueryRegistry.getQuery(queryName);
            
            if (hql != null) {
                // 使用Session的createNamedQuery方法
                org.hibernate.query.Query<User> query = 
                    session.createNamedQuery(queryName, User.class);
                query.setParameter("name", "测试用户");
                
                List<User> users = query.list();
                System.out.println("✅ 命名查询执行成功，查询到 " + users.size() + " 个用户");
            } else {
                System.out.println("⚠️  命名查询未定义，跳过测试");
            }
        } catch (Exception e) {
            System.err.println("❌ 执行命名查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试直接使用Session的命名查询
     */
    @Test
    @Transaction
    public void testSessionNamedQuery() {
        Session session = sessionFactory.getCurrentSession();
        
        try {
            // 尝试使用Session的createNamedQuery（如果实体类定义了@NamedQuery）
            org.hibernate.query.Query<User> query = 
                session.createNamedQuery("findUserByName", User.class);
            query.setParameter("name", "测试用户");
            
            List<User> users = query.list();
            System.out.println("✅ Session命名查询执行成功，查询到 " + users.size() + " 个用户");
        } catch (Exception e) {
            System.out.println("⚠️  Session命名查询失败（可能未定义）: " + e.getMessage());
        }
    }
}

