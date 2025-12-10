package org.hibernate.solon.test;

import org.hibernate.solon.integration.query.PageQuery;
import org.hibernate.solon.test.entity.User;
import org.hibernate.solon.test.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;
import org.noear.solon.test.SolonTest;

import java.util.List;
import java.util.Optional;

/**
 * Repository测试类
 * 
 * <p>测试HibernateRepository的基础CRUD和扩展功能</p>
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
public class RepositoryTest {
    
    @Inject
    private UserRepository userRepository;
    
    /**
     * 测试保存实体
     */
    @Test
    @Transaction
    public void testSave() {
        User user = new User();
        user.setName("Repository测试用户");
        user.setAge(25);
        user.setEmail("repo@example.com");
        
        User saved = userRepository.save(user);
        
        System.out.println("✅ 保存成功，ID: " + saved.getId());
        assert saved.getId() != null : "ID应该不为空";
    }
    
    /**
     * 测试根据ID查找
     */
    @Test
    @Transaction
    public void testFindById() {
        // 先创建一个用户
        User user = new User();
        user.setName("查找测试");
        user.setAge(30);
        user.setEmail("find@example.com");
        userRepository.save(user);
        
        Long id = user.getId();
        
        // 根据ID查找
        Optional<User> found = userRepository.findById(id);
        
        if (found.isPresent()) {
            System.out.println("✅ 查找成功: " + found.get().getName());
            assert found.get().getId().equals(id) : "ID应该匹配";
        } else {
            System.out.println("❌ 查找失败");
        }
    }
    
    /**
     * 测试查找所有
     */
    @Test
    @Transaction
    public void testFindAll() {
        List<User> users = userRepository.findAll();
        
        System.out.println("✅ 查询到 " + users.size() + " 个用户");
    }
    
    /**
     * 测试统计数量
     */
    @Test
    @Transaction
    public void testCount() {
        long count = userRepository.count();
        
        System.out.println("✅ 用户总数: " + count);
    }
    
    /**
     * 测试判断是否存在
     */
    @Test
    @Transaction
    public void testExistsById() {
        // 先创建一个用户
        User user = new User();
        user.setName("存在性测试");
        user.setAge(28);
        user.setEmail("exists@example.com");
        userRepository.save(user);
        
        Long id = user.getId();
        
        boolean exists = userRepository.existsById(id);
        boolean notExists = userRepository.existsById(999999L);
        
        System.out.println("✅ ID " + id + " 存在: " + exists);
        System.out.println("✅ ID 999999 不存在: " + !notExists);
        
        assert exists : "用户应该存在";
        assert !notExists : "不存在的ID应该返回false";
    }
    
    /**
     * 测试删除
     */
    @Test
    @Transaction
    public void testDelete() {
        // 先创建一个用户
        User user = new User();
        user.setName("删除测试");
        user.setAge(27);
        user.setEmail("delete@example.com");
        userRepository.save(user);
        
        long countBefore = userRepository.count();
        
        // 删除
        userRepository.delete(user);
        
        long countAfter = userRepository.count();
        
        System.out.println("✅ 删除前数量: " + countBefore);
        System.out.println("✅ 删除后数量: " + countAfter);
        
        assert countAfter == countBefore - 1 : "删除后数量应该减少1";
    }
    
    /**
     * 测试根据ID删除
     */
    @Test
    @Transaction
    public void testDeleteById() {
        // 先创建一个用户
        User user = new User();
        user.setName("根据ID删除测试");
        user.setAge(26);
        user.setEmail("deletebyid@example.com");
        userRepository.save(user);
        
        Long id = user.getId();
        long countBefore = userRepository.count();
        
        // 根据ID删除
        userRepository.deleteById(id);
        
        long countAfter = userRepository.count();
        
        System.out.println("✅ 根据ID删除成功");
        System.out.println("   删除前数量: " + countBefore);
        System.out.println("   删除后数量: " + countAfter);
        
        assert countAfter == countBefore - 1 : "删除后数量应该减少1";
    }
    
    /**
     * 测试自定义查询方法
     */
    @Test
    @Transaction
    public void testFindByName() {
        // 创建测试用户
        User user = new User();
        user.setName("自定义查询测试");
        user.setAge(29);
        user.setEmail("custom@example.com");
        userRepository.save(user);
        
        // 根据名称查找
        List<User> users = userRepository.findByName("自定义查询测试");
        
        System.out.println("✅ 根据名称查询到 " + users.size() + " 个用户");
        assert users.size() > 0 : "应该查询到至少1个用户";
    }
    
    /**
     * 测试动态查询
     */
    @Test
    @Transaction
    public void testSearchUsers() {
        // 创建测试数据
        User user1 = new User();
        user1.setName("搜索测试1");
        user1.setAge(25);
        user1.setEmail("search1@example.com");
        userRepository.save(user1);
        
        User user2 = new User();
        user2.setName("搜索测试2");
        user2.setAge(30);
        user2.setEmail("search2@example.com");
        userRepository.save(user2);
        
        // 动态查询
        List<User> users = userRepository.searchUsers("搜索", 20);
        
        System.out.println("✅ 动态查询到 " + users.size() + " 个用户");
        assert users.size() >= 2 : "应该查询到至少2个用户";
    }
    
    /**
     * 测试分页查询
     */
    @Test
    @Transaction
    public void testFindUsersPage() {
        // 创建测试数据
        for (int i = 1; i <= 15; i++) {
            User user = new User();
            user.setName("分页测试用户" + i);
            user.setAge(20 + i);
            user.setEmail("page" + i + "@example.com");
            userRepository.save(user);
        }
        
        // 分页查询
        PageQuery<User> page = userRepository.findUsers(1, 10);
        
        System.out.println("✅ 分页查询结果:");
        System.out.println("   总记录数: " + page.getTotal());
        System.out.println("   当前页: " + page.getPage());
        System.out.println("   每页大小: " + page.getSize());
        System.out.println("   总页数: " + page.getTotalPages());
        System.out.println("   当前页数据: " + page.getContent().size() + " 条");
        
        assert page.getTotal() >= 15 : "总记录数应该至少15条";
        assert page.getContent().size() == 10 : "第一页应该有10条数据";
    }
    
    /**
     * 测试分页动态查询
     */
    @Test
    @Transaction
    public void testSearchUsersPage() {
        // 创建测试数据
        for (int i = 1; i <= 20; i++) {
            User user = new User();
            user.setName("分页搜索用户" + i);
            user.setAge(20 + i);
            user.setEmail("pagesearch" + i + "@example.com");
            userRepository.save(user);
        }
        
        // 分页动态查询
        PageQuery<User> page = userRepository.searchUsersPage("分页搜索", 20, 1, 5);
        
        System.out.println("✅ 分页动态查询结果:");
        System.out.println("   总记录数: " + page.getTotal());
        System.out.println("   当前页数据: " + page.getContent().size() + " 条");
        
        assert page.getTotal() >= 20 : "总记录数应该至少20条";
        assert page.getContent().size() == 5 : "每页应该有5条数据";
    }
}

