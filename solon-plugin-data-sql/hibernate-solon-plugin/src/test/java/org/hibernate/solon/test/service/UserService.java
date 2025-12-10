package org.hibernate.solon.test.service;

import org.hibernate.solon.test.entity.User;
import org.hibernate.solon.test.repository.UserRepository;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户服务类（测试用）
 * 
 * @author noear
 * @since 3.4
 */
@Component
public class UserService {
    
    @Inject
    private UserRepository userRepository;
    
    /**
     * 保存用户
     */
    @Transaction
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    /**
     * 批量保存用户
     */
    @Transaction
    public void batchSaveUsers(List<User> users) {
        userRepository.saveAll(users);
    }
    
    /**
     * 批量保存用户（使用自定义批量大小）
     */
    @Transaction
    public void batchSaveUsersWithSize(List<User> users, int batchSize) {
        // 注意：getBatchHelper是protected方法，需要通过Session创建
        // 这里仅作为示例，实际使用时建议直接使用saveAll方法
        userRepository.saveAll(users);
    }
    
    /**
     * 根据ID查找用户
     */
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    /**
     * 查找所有用户
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    /**
     * 根据名称查找用户
     */
    public List<User> findByName(String name) {
        return userRepository.findByName(name);
    }
    
    /**
     * 搜索用户
     */
    public List<User> searchUsers(String name, Integer minAge) {
        return userRepository.searchUsers(name, minAge);
    }
    
    /**
     * 分页查询用户
     */
    public org.hibernate.solon.integration.query.PageQuery<User> findUsersPage(int page, int size) {
        return userRepository.findAll(page, size);
    }
    
    /**
     * 分页动态搜索用户
     */
    public org.hibernate.solon.integration.query.PageQuery<User> searchUsersPage(String name, Integer minAge, int page, int size) {
        return userRepository.searchUsersPage(name, minAge, page, size);
    }
    
    /**
     * 删除用户
     */
    @Transaction
    public void deleteUser(User user) {
        userRepository.delete(user);
    }
    
    /**
     * 批量删除用户
     */
    @Transaction
    public void batchDeleteUsers(List<User> users) {
        userRepository.deleteAll(users);
    }
    
    /**
     * 创建测试数据
     */
    @Transaction
    public List<User> createTestUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            User user = new User();
            user.setName("测试用户" + i);
            user.setAge(20 + i);
            user.setEmail("test" + i + "@example.com");
            users.add(user);
        }
        userRepository.saveAll(users);
        return users;
    }
}

