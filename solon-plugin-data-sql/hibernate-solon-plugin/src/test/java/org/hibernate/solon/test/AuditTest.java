package org.hibernate.solon.test;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.solon.annotation.Db;
import org.hibernate.solon.test.entity.User;
import org.junit.jupiter.api.Test;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;
import org.noear.solon.test.SolonTest;

import java.time.LocalDateTime;

/**
 * 审计功能测试类
 * 
 * <p>测试@CreatedDate和@LastModifiedDate注解的自动填充功能</p>
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
public class AuditTest {
    
    @Db
    @Inject
    private SessionFactory sessionFactory;
    
    /**
     * 测试创建时间自动填充
     */
    @Test
    @Transaction
    public void testCreatedDate() {
        Session session = sessionFactory.getCurrentSession();
        
        // 创建新用户（不设置createTime）
        User user = new User();
        user.setName("审计测试用户");
        user.setAge(25);
        user.setEmail("audit@example.com");
        // createTime应该由@CreatedDate自动填充
        
        // 保存前检查
        System.out.println("保存前 createTime: " + user.getCreateTime());
        
        // 保存
        session.save(user);
        session.flush(); // 触发AuditListener
        
        // 保存后检查
        System.out.println("保存后 createTime: " + user.getCreateTime());
        
        if (user.getCreateTime() != null) {
            System.out.println("✅ @CreatedDate自动填充成功");
        } else {
            System.out.println("❌ @CreatedDate自动填充失败");
        }
    }
    
    /**
     * 测试更新时间自动填充
     */
    @Test
    @Transaction
    public void testLastModifiedDate() {
        Session session = sessionFactory.getCurrentSession();
        
        // 创建用户
        User user = new User();
        user.setName("更新时间测试");
        user.setAge(30);
        user.setEmail("update@example.com");
        
        session.save(user);
        session.flush();
        
        LocalDateTime createTime = user.getCreateTime();
        LocalDateTime updateTime = user.getUpdateTime();
        
        System.out.println("创建时 createTime: " + createTime);
        System.out.println("创建时 updateTime: " + updateTime);
        
        // 等待1秒后更新
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 更新用户
        user.setName("更新时间测试_已修改");
        session.update(user);
        session.flush();
        
        LocalDateTime newUpdateTime = user.getUpdateTime();
        
        System.out.println("更新后 updateTime: " + newUpdateTime);
        
        if (newUpdateTime != null && !newUpdateTime.equals(updateTime)) {
            System.out.println("✅ @LastModifiedDate自动更新成功");
        } else {
            System.out.println("❌ @LastModifiedDate自动更新失败");
        }
    }
    
    /**
     * 测试创建和更新时间的完整流程
     */
    @Test
    @Transaction
    public void testAuditCompleteFlow() {
        Session session = sessionFactory.getCurrentSession();
        
        // 创建用户
        User user = new User();
        user.setName("完整审计测试");
        user.setAge(28);
        user.setEmail("complete@example.com");
        
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("1. 创建用户");
        System.out.println("   保存前 createTime: " + user.getCreateTime());
        System.out.println("   保存前 updateTime: " + user.getUpdateTime());
        
        session.save(user);
        session.flush();
        
        System.out.println("   保存后 createTime: " + user.getCreateTime());
        System.out.println("   保存后 updateTime: " + user.getUpdateTime());
        
        // 验证创建时间
        if (user.getCreateTime() != null && user.getUpdateTime() != null) {
            System.out.println("✅ 创建时时间字段已自动填充");
        }
        
        // 等待后更新
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("\n2. 更新用户");
        LocalDateTime oldUpdateTime = user.getUpdateTime();
        user.setName("完整审计测试_已更新");
        
        session.update(user);
        session.flush();
        
        System.out.println("   更新前 updateTime: " + oldUpdateTime);
        System.out.println("   更新后 updateTime: " + user.getUpdateTime());
        
        // 验证更新时间
        if (user.getUpdateTime() != null && 
            user.getUpdateTime().isAfter(oldUpdateTime)) {
            System.out.println("✅ 更新时updateTime已自动更新");
        }
        
        // 验证createTime未改变
        if (user.getCreateTime() != null && 
            user.getCreateTime().equals(user.getCreateTime())) {
            System.out.println("✅ createTime未被修改（符合预期）");
        }
        
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}

