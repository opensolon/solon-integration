package org.hibernate.solon.test;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.solon.annotation.Db;
import org.hibernate.solon.integration.batch.BatchOperationHelper;
import org.hibernate.solon.test.entity.User;
import org.noear.solon.annotation.Component;
import org.noear.solon.data.annotation.Tran;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量操作测试类
 * 
 * @author noear
 * @since 3.4
 */
@Component
public class BatchOperationTest {
    
    @Db
    private SessionFactory sessionFactory;
    
    /**
     * 测试批量保存
     */
    @Tran
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
    @Tran
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
    @Tran
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
    @Tran
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

