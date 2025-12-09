package org.hibernate.solon.test;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.solon.annotation.Db;
import org.hibernate.solon.integration.batch.BatchOperationHelper;
import org.hibernate.solon.test.entity.User;
import org.junit.jupiter.api.Test;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;
import org.noear.solon.test.SolonTest;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量操作测试类
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
 *   <li>详细说明请参考：TEST_GUIDE.md</li>
 * </ol>
 * 
 * @author noear
 * @since 3.4
 */
@SolonTest(TestApp.class)
public class BatchOperationTest {
    
    @Db
    @Inject
    private SessionFactory sessionFactory;
    
    /**
     * 测试批量保存
     */
    @Test
    @Transaction
    public void testBatchSave() {
        Session session = sessionFactory.getCurrentSession();
        BatchOperationHelper helper = new BatchOperationHelper(session, 50);
        
        // 创建测试数据
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            User user = new User();
            user.setName("批量用户" + i);
            user.setAge(20 + i);
            user.setEmail("batch" + i + "@example.com");
            users.add(user);
        }
        
        // 批量保存
        long startTime = System.currentTimeMillis();
        helper.batchSave(users);
        long endTime = System.currentTimeMillis();
        
        System.out.println("批量保存 " + users.size() + " 条记录，耗时: " + (endTime - startTime) + " ms");
    }
    
    /**
     * 测试批量更新
     */
    @Test
    @Transaction
    public void testBatchUpdate() {
        Session session = sessionFactory.getCurrentSession();
        BatchOperationHelper helper = new BatchOperationHelper(session);
        
        // 查询所有用户
        List<User> users = session.createQuery("FROM User", User.class).list();
        
        // 更新年龄
        for (User user : users) {
            user.setAge(user.getAge() + 1);
        }
        
        // 批量更新
        long startTime = System.currentTimeMillis();
        helper.batchUpdate(users);
        long endTime = System.currentTimeMillis();
        
        System.out.println("批量更新 " + users.size() + " 条记录，耗时: " + (endTime - startTime) + " ms");
    }
    
    /**
     * 测试批量删除
     */
    @Test
    @Transaction
    public void testBatchDelete() {
        Session session = sessionFactory.getCurrentSession();
        BatchOperationHelper helper = new BatchOperationHelper(session);
        
        // 查询要删除的用户
        List<User> users = session.createQuery(
            "FROM User WHERE name LIKE :name",
            User.class
        ).setParameter("name", "%批量%").list();
        
        // 批量删除
        long startTime = System.currentTimeMillis();
        helper.batchDelete(users);
        long endTime = System.currentTimeMillis();
        
        System.out.println("批量删除 " + users.size() + " 条记录，耗时: " + (endTime - startTime) + " ms");
    }
    
    /**
     * 测试使用StatelessSession的批量插入
     */
    @Test
    @Transaction
    public void testStatelessBatchInsert() {
        Session session = sessionFactory.getCurrentSession();
        BatchOperationHelper helper = new BatchOperationHelper(session);
        
        // 创建测试数据
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            User user = new User();
            user.setName("Stateless用户" + i);
            user.setAge(20 + i);
            user.setEmail("stateless" + i + "@example.com");
            users.add(user);
        }
        
        // 使用StatelessSession批量插入
        long startTime = System.currentTimeMillis();
        helper.batchInsertWithStateless(users);
        long endTime = System.currentTimeMillis();
        
        System.out.println("StatelessSession批量插入 " + users.size() + " 条记录，耗时: " + (endTime - startTime) + " ms");
    }
}
