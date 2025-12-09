package org.hibernate.solon.test.repository;

import org.hibernate.solon.integration.HibernateRepository;
import org.hibernate.solon.integration.query.DynamicQueryBuilder;
import org.hibernate.solon.integration.query.PageQuery;
import org.hibernate.solon.test.entity.User;
import org.noear.solon.annotation.Component;

import java.util.List;
import java.util.Map;

/**
 * 用户Repository（测试用）
 * 
 * @author noear
 * @since 3.4
 */
@Component
public class UserRepository extends HibernateRepository<User, Long> {
    
    public UserRepository() {
        super(User.class);
    }
    
    /**
     * 根据名称查找用户
     */
    public List<User> findByName(String name) {
        return getQueryHelper().list(
            "FROM User WHERE name = :name",
            User.class,
            Map.of("name", name)
        );
    }
    
    /**
     * 分页查询用户
     */
    public PageQuery<User> findUsers(int page, int size) {
        return findAll(page, size);
    }
    
    /**
     * 动态查询用户
     */
    public List<User> searchUsers(String name, Integer minAge) {
        DynamicQueryBuilder builder = new DynamicQueryBuilder("FROM User u");
        if (name != null && !name.isEmpty()) {
            builder.like("u.name", "name", name);
        }
        if (minAge != null) {
            builder.where("u.age >= :age", "age", minAge);
        }
        builder.orderBy("u.createTime DESC");
        return findByBuilder(builder);
    }
    
    /**
     * 分页动态查询
     */
    public PageQuery<User> searchUsersPage(String name, Integer minAge, int page, int size) {
        DynamicQueryBuilder builder = new DynamicQueryBuilder("FROM User u");
        if (name != null && !name.isEmpty()) {
            builder.like("u.name", "name", name);
        }
        if (minAge != null) {
            builder.where("u.age >= :age", "age", minAge);
        }
        builder.orderBy("u.createTime DESC");
        return findPageByBuilder(builder, page, size);
    }
}

