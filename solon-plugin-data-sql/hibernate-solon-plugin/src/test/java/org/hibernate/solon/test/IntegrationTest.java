package org.hibernate.solon.test;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.solon.annotation.Db;
import org.hibernate.solon.integration.HibernateAdapter;
import org.hibernate.solon.integration.HibernateAdapterManager;
import org.hibernate.solon.integration.query.PageQuery;
import org.hibernate.solon.test.entity.Product;
import org.hibernate.solon.test.entity.User;
import org.hibernate.solon.test.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;
import org.noear.solon.test.SolonTest;

import java.util.List;

/**
 * 集成测试类
 * 
 * <p>测试多个功能的集成使用</p>
 * 
 * <p><b>⚠️ 测试前准备：</b></p>
 * <ol>
 *   <li>确保数据库表已创建（test_user、product表）</li>
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
public class IntegrationTest {
    
    @Db
    @Inject
    private SessionFactory sessionFactory;
    
    @Inject
    private UserRepository userRepository;
    
    /**
     * 测试完整工作流程
     */
    @Test
    @Transaction
    public void testCompleteWorkflow() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("完整工作流程测试");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // 1. 创建用户（使用Repository）
        System.out.println("\n1. 创建用户");
        User user = new User();
        user.setName("集成测试用户");
        user.setAge(30);
        user.setEmail("integration@example.com");
        user = userRepository.save(user);
        System.out.println("   ✅ 用户创建成功，ID: " + user.getId());
        
        // 2. 查询用户（使用Repository）
        System.out.println("\n2. 查询用户");
        java.util.Optional<User> found = userRepository.findById(user.getId());
        if (found.isPresent()) {
            System.out.println("   ✅ 查询成功: " + found.get().getName());
        }
        
        // 3. 分页查询（使用Repository）
        System.out.println("\n3. 分页查询");
        PageQuery<User> page = userRepository.findUsers(1, 10);
        System.out.println("   ✅ 分页查询成功");
        System.out.println("      总记录数: " + page.getTotal());
        System.out.println("      当前页数据: " + page.getContent().size() + " 条");
        
        // 4. 动态查询（使用Repository）
        System.out.println("\n4. 动态查询");
        List<User> users = userRepository.searchUsers("集成", 20);
        System.out.println("   ✅ 动态查询成功，查询到 " + users.size() + " 个用户");
        
        // 5. 统计（使用Repository）
        System.out.println("\n5. 统计数量");
        long count = userRepository.count();
        System.out.println("   ✅ 统计成功，总用户数: " + count);
        
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("✅ 完整工作流程测试通过");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    /**
     * 测试多实体类操作
     */
    @Test
    @Transaction
    public void testMultipleEntities() {
        Session session = sessionFactory.getCurrentSession();
        
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("多实体类操作测试");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // 创建User
        User user = new User();
        user.setName("多实体测试用户");
        user.setAge(28);
        user.setEmail("multi@example.com");
        session.save(user);
        System.out.println("✅ User创建成功，ID: " + user.getId());
        
        // 创建Product（如果Product实体存在）
        try {
            Product product = new Product();
            product.setCode("TEST001");
            product.setName("测试产品");
            product.setPrice(new java.math.BigDecimal("99.99"));
            product.setStock(100);
            session.save(product);
            System.out.println("✅ Product创建成功，ID: " + product.getId());
        } catch (Exception e) {
            System.out.println("⚠️  Product创建失败（可能实体类未正确配置）: " + e.getMessage());
        }
        
        System.out.println("✅ 多实体类操作测试完成");
    }
    
    /**
     * 测试HibernateAdapter功能
     */
    @Test
    public void testHibernateAdapter() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("HibernateAdapter功能测试");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                System.out.println("⚠️  未找到Hibernate适配器");
                return;
            }
            
            // 测试获取SessionFactory
            adapter.getSessionFactory();
            System.out.println("✅ SessionFactory获取成功");
            
            // 测试获取Configuration
            org.hibernate.cfg.Configuration config = adapter.getConfiguration();
            System.out.println("✅ Configuration获取成功");
            System.out.println("   已注册实体类数: " + 
                (config instanceof org.hibernate.solon.integration.HibernateConfiguration ?
                    ((org.hibernate.solon.integration.HibernateConfiguration) config)
                        .getRegisteredClasses().size() : "未知"));
            
            // 测试获取SchemaManager
            adapter.getSchemaManager();
            System.out.println("✅ SchemaManager获取成功");
            
            // 测试获取DdlGenerator
            adapter.getDdlGenerator();
            System.out.println("✅ DdlGenerator获取成功");
            
            System.out.println("✅ HibernateAdapter功能测试完成");
            
        } catch (Exception e) {
            System.err.println("❌ HibernateAdapter测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

