package org.hibernate.solon.test.controller;

import org.hibernate.solon.integration.query.PageQuery;
import org.hibernate.solon.test.entity.User;
import org.hibernate.solon.test.service.UserService;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;

import java.util.List;

/**
 * 用户控制器（测试用）
 * 
 * @author noear
 * @since 3.4
 */
@Controller
@Mapping("/api/user")
public class UserController {
    
    @Inject
    private UserService userService;
    
    /**
     * 创建用户
     */
    @Mapping("/create")
    public User createUser(String name, Integer age, String email) {
        User user = new User();
        user.setName(name);
        user.setAge(age);
        user.setEmail(email);
        return userService.saveUser(user);
    }
    
    /**
     * 批量创建用户
     */
    @Mapping("/batch/create")
    public String batchCreateUsers(int count) {
        userService.createTestUsers(count);
        return "成功创建 " + count + " 个测试用户";
    }
    
    /**
     * 根据ID查找用户
     */
    @Mapping("/get")
    public User getUser(Long id) {
        return userService.findById(id);
    }
    
    /**
     * 查找所有用户
     */
    @Mapping("/list")
    public List<User> listUsers() {
        return userService.findAll();
    }
    
    /**
     * 分页查询用户
     */
    @Mapping("/page")
    public PageQuery<User> pageUsers(int page, int size) {
        return userService.findUsersPage(page, size);
    }
    
    /**
     * 搜索用户
     */
    @Mapping("/search")
    public List<User> searchUsers(String name, Integer minAge) {
        return userService.searchUsers(name, minAge);
    }
    
    /**
     * 删除用户
     */
    @Mapping("/delete")
    public String deleteUser(Long id) {
        User user = userService.findById(id);
        if (user != null) {
            userService.deleteUser(user);
            return "删除成功";
        }
        return "用户不存在";
    }
}

