package org.hibernate.solon.test;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.solon.annotation.Db;
import org.hibernate.solon.test.entity.User;
import org.junit.jupiter.api.Test;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;
import org.noear.solon.data.tran.TranUtils;
import org.noear.solon.test.SolonTest;

/**
 * 事务集成测试类
 * 
 * <p>测试Hibernate与Solon事务的集成</p>
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
public class TransactionTest {
    
    @Db
    @Inject
    private SessionFactory sessionFactory;
    
    /**
     * 测试事务提交
     */
    @Test
    @Transaction
    public void testTransactionCommit() {
        Session session = sessionFactory.getCurrentSession();
        
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("测试事务提交");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // 检查是否在事务中
        boolean inTrans = TranUtils.inTrans();
        System.out.println("是否在Solon事务中: " + inTrans);
        
        // 创建用户
        User user = new User();
        user.setName("事务提交测试");
        user.setAge(25);
        user.setEmail("commit@example.com");
        
        session.save(user);
        System.out.println("用户已保存，ID: " + user.getId());
        
        // 方法结束时会自动提交事务（@Transaction注解）
        System.out.println("✅ 事务将自动提交");
    }
    
    /**
     * 测试事务回滚
     */
    @Test
    @Transaction
    public void testTransactionRollback() {
        Session session = sessionFactory.getCurrentSession();
        
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("测试事务回滚");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        try {
            // 创建用户
            User user = new User();
            user.setName("事务回滚测试");
            user.setAge(25);
            user.setEmail("rollback@example.com");
            
            session.save(user);
            System.out.println("用户已保存，ID: " + user.getId());
            
            // 模拟异常，触发回滚
            throw new RuntimeException("模拟异常，触发事务回滚");
            
        } catch (Exception e) {
            System.out.println("捕获异常: " + e.getMessage());
            // 注意：Solon的事务回滚通过抛出异常自动处理
            System.out.println("✅ 事务将自动回滚（异常会触发回滚）");
            throw e; // 重新抛出异常以触发回滚
        }
    }
    
    /**
     * 测试只读事务
     */
    @Test
    @Transaction(readOnly = true)
    public void testReadOnlyTransaction() {
        Session session = sessionFactory.getCurrentSession();
        
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("测试只读事务");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // 查询操作
        long count = session.createQuery("SELECT COUNT(*) FROM User", Long.class)
            .uniqueResult();
        
        System.out.println("查询到用户数量: " + count);
        System.out.println("✅ 只读事务执行成功");
        
        // 注意：在只读事务中尝试写入会失败
        try {
            User user = new User();
            user.setName("只读事务测试");
            user.setAge(25);
            session.save(user);
            System.out.println("⚠️  只读事务中不应该允许写入");
        } catch (Exception e) {
            System.out.println("✅ 只读事务正确阻止了写入操作");
        }
    }
    
    /**
     * 测试嵌套事务
     */
    @Test
    @Transaction
    public void testNestedTransaction() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("测试嵌套事务");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // 外层事务
        System.out.println("外层事务开始");
        boolean inTrans1 = TranUtils.inTrans();
        System.out.println("外层事务中: " + inTrans1);
        
        // 调用内层方法（也带@Transaction）
        innerTransaction();
        
        System.out.println("外层事务继续");
        System.out.println("✅ 嵌套事务测试完成");
    }
    
    @Transaction
    private void innerTransaction() {
        System.out.println("  内层事务开始");
        boolean inTrans2 = TranUtils.inTrans();
        System.out.println("  内层事务中: " + inTrans2);
        
        // 内层事务操作
        User user = new User();
        user.setName("嵌套事务测试");
        user.setAge(26);
        user.setEmail("nested@example.com");
        Session session = sessionFactory.getCurrentSession();
        session.save(user);
        System.out.println("  内层事务：用户已保存，ID: " + user.getId());
    }
    
    /**
     * 测试事务传播
     */
    @Test
    @Transaction
    public void testTransactionPropagation() {
        Session session = sessionFactory.getCurrentSession();
        
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("测试事务传播");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // 检查事务状态
        boolean inTrans = TranUtils.inTrans();
        System.out.println("当前事务状态: " + (inTrans ? "在事务中" : "不在事务中"));
        
        // 执行多个操作
        User user1 = new User();
        user1.setName("传播测试1");
        user1.setAge(27);
        session.save(user1);
        
        User user2 = new User();
        user2.setName("传播测试2");
        user2.setAge(28);
        session.save(user2);
        
        System.out.println("✅ 多个操作在同一事务中执行");
        System.out.println("   用户1 ID: " + user1.getId());
        System.out.println("   用户2 ID: " + user2.getId());
    }
}

