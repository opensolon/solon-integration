# Hibernate-Solon插件使用示例文档

## 目录

1. [快速开始](#快速开始)
2. [基础配置](#基础配置)
3. [实体类定义](#实体类定义)
4. [Repository使用](#repository使用)
5. [查询功能](#查询功能)
6. [批量操作](#批量操作)
7. [审计功能](#审计功能)
8. [缓存使用](#缓存使用)
9. [性能监控](#性能监控)
10. [完整示例](#完整示例)

## 快速开始

### 1. 添加依赖

在`pom.xml`中添加依赖：

```xml
<dependency>
    <groupId>org.noear</groupId>
    <artifactId>hibernate-solon-plugin</artifactId>
    <version>3.7.3</version>
</dependency>
```

### 2. 配置数据源

在`app.yml`中配置数据源：

```yaml
# 数据源配置
test.db1:
  schema: rock
  jdbcUrl: jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf8
  driverClassName: com.mysql.cj.jdbc.Driver
  username: root
  password: root

# Hibernate配置
jpa.db1:
  mappings:
    - com.example.entity.*
  properties:
    hibernate:
      dialect: org.hibernate.dialect.MySQL8Dialect
      hbm2ddl:
        auto: update
      show_sql: true
      format_sql: true
```

### 3. 启用Hibernate

在启动类上添加`@EnableHibernate`注解：

```java
import org.hibernate.solon.annotation.EnableHibernate;
import org.noear.solon.Solon;

@EnableHibernate(basePackages = "com.example.entity")
public class App {
    public static void main(String[] args) {
        Solon.start(App.class, args);
    }
}
```

## 基础配置

### 配置数据源Bean

```java
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    
    @Bean(name = "db1", typed = true)
    public DataSource dataSource(@Inject("${test.db1}") HikariDataSource ds) {
        return ds;
    }
}
```

### 完整配置示例

```yaml
# 数据源配置
test.db1:
  schema: rock
  jdbcUrl: jdbc:mysql://localhost:3306/test
  driverClassName: com.mysql.cj.jdbc.Driver
  username: root
  password: root

# Hibernate配置
jpa.db1:
  # 实体类映射
  mappings:
    - com.example.entity.*
  
  # 属性配置
  properties:
    hibernate:
      # 数据库方言
      dialect: org.hibernate.dialect.MySQL8Dialect
      
      # DDL策略
      hbm2ddl:
        auto: update
      
      # SQL日志
      show_sql: true
      format_sql: true
      use_sql_comments: true
      
      # 批量操作
      jdbc:
        batch_size: 50
        batch_versioned_data: true
      
      # 二级缓存
      cache:
        use_second_level_cache: true
        use_query_cache: true
        region:
          factory_class: org.hibernate.cache.ehcache.EhCacheRegionFactory
      
      # 懒加载
      enable_lazy_load_no_trans: false
      default_batch_fetch_size: 16
      
      # 性能监控
      generate_statistics: true
```

## 实体类定义

### 基础实体类

```java
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(nullable = false)
    private Integer age;
    
    @Column(length = 100)
    private String email;
    
    // Getters and Setters
    // ...
}
```

### 使用审计功能

```java
import org.hibernate.solon.annotation.CreatedDate;
import org.hibernate.solon.annotation.LastModifiedDate;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @CreatedDate
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    // Getters and Setters
    // ...
}
```

### 使用缓存

```java
import org.hibernate.solon.annotation.Cacheable;
import javax.persistence.*;

@Entity
@Table(name = "user")
@Cacheable(strategy = Cacheable.CacheStrategy.READ_WRITE)
public class User {
    // ...
}
```

## Repository使用

### 创建Repository

```java
import org.hibernate.solon.integration.HibernateRepository;
import org.noear.solon.annotation.Component;
import com.example.entity.User;

@Component
public class UserRepository extends HibernateRepository<User, Long> {
    
    public UserRepository() {
        super(User.class);
    }
    
    // 可以添加自定义查询方法
    public List<User> findByName(String name) {
        return getQueryHelper().list(
            "FROM User WHERE name = :name",
            User.class,
            Map.of("name", name)
        );
    }
}
```

### 基础CRUD操作

```java
@Service
public class UserService {
    
    @Inject
    private UserRepository userRepository;
    
    // 保存
    @Tran
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    // 查找
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    // 查找所有
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    // 删除
    @Tran
    public void deleteUser(User user) {
        userRepository.delete(user);
    }
    
    // 更新
    @Tran
    public User updateUser(User user) {
        return userRepository.saveOrUpdate(user);
    }
}
```

## 查询功能

### 1. 使用查询助手

```java
@Service
public class UserService {
    
    @Db
    private SessionFactory sessionFactory;
    
    @Tran
    public List<User> findUsers() {
        Session session = sessionFactory.getCurrentSession();
        HibernateQueryHelper helper = new HibernateQueryHelper(session);
        
        // 基本查询
        return helper.list("FROM User", User.class);
    }
    
    @Tran
    public List<User> findUsersByName(String name) {
        Session session = sessionFactory.getCurrentSession();
        HibernateQueryHelper helper = new HibernateQueryHelper(session);
        
        // 带参数查询
        return helper.list(
            "FROM User WHERE name = :name",
            User.class,
            Map.of("name", name)
        );
    }
}
```

### 2. 分页查询

```java
@Tran
public PageQuery<User> findUsersPage(int page, int size) {
    Session session = sessionFactory.getCurrentSession();
    HibernateQueryHelper helper = new HibernateQueryHelper(session);
    
    // 分页查询
    return helper.pageQuery(
        "FROM User",
        User.class,
        page,
        size
    );
}

// 使用Repository的分页方法
public PageQuery<User> findUsersPage(int page, int size) {
    return userRepository.findAll(page, size);
}
```

### 3. 动态查询构建器

```java
@Tran
public List<User> searchUsers(String name, Integer minAge, Integer maxAge) {
    Session session = sessionFactory.getCurrentSession();
    HibernateQueryHelper helper = new HibernateQueryHelper(session);
    
    // 构建动态查询
    DynamicQueryBuilder builder = new DynamicQueryBuilder("FROM User u");
    
    if (name != null && !name.isEmpty()) {
        builder.like("u.name", "name", name);
    }
    
    if (minAge != null) {
        builder.where("u.age >= :minAge", "minAge", minAge);
    }
    
    if (maxAge != null) {
        builder.where("u.age <= :maxAge", "maxAge", maxAge);
    }
    
    builder.orderBy("u.createTime DESC");
    
    // 执行查询
    String hql = builder.build();
    Map<String, Object> params = builder.getParameters();
    
    return helper.list(hql, User.class, params);
}

// 使用Repository的动态查询方法
public List<User> searchUsers(String name, Integer minAge) {
    DynamicQueryBuilder builder = new DynamicQueryBuilder("FROM User u");
    builder.like("u.name", "name", name)
           .where("u.age >= :age", "age", minAge)
           .orderBy("u.createTime DESC");
    
    return userRepository.findByBuilder(builder);
}
```

### 4. 命名查询

```java
@Entity
@NamedQueries({
    @NamedQuery(
        name = "User.findByEmail",
        query = "FROM User WHERE email = :email"
    ),
    @NamedQuery(
        name = "User.findByAgeRange",
        query = "FROM User WHERE age BETWEEN :minAge AND :maxAge"
    )
})
public class User {
    // ...
}

// 使用命名查询
@Tran
public List<User> findByEmail(String email) {
    Session session = sessionFactory.getCurrentSession();
    return session.createNamedQuery("User.findByEmail", User.class)
                  .setParameter("email", email)
                  .list();
}
```

## 批量操作

### 1. 批量保存

```java
@Service
public class UserService {
    
    @Inject
    private UserRepository userRepository;
    
    @Tran
    public void batchSaveUsers(List<User> users) {
        // 使用Repository的批量方法
        userRepository.saveAll(users);
    }
    
    @Tran
    public void batchSaveWithCustomSize(List<User> users) {
        // 使用自定义批量大小
        BatchOperationHelper helper = userRepository.getBatchHelper(100);
        helper.batchSave(users);
    }
}
```

### 2. 批量更新

```java
@Tran
public void batchUpdateUsers(List<User> users) {
    // 更新所有用户的年龄
    for (User user : users) {
        user.setAge(user.getAge() + 1);
    }
    
    userRepository.updateAll(users);
}
```

### 3. 批量删除

```java
@Tran
public void batchDeleteUsers(List<User> users) {
    userRepository.deleteAll(users);
}
```

### 4. 使用StatelessSession（高性能批量插入）

```java
@Tran
public void highPerformanceBatchInsert(List<User> users) {
    Session session = sessionFactory.getCurrentSession();
    BatchOperationHelper helper = new BatchOperationHelper(session);
    
    // 使用StatelessSession进行批量插入（性能更好，但不支持级联）
    helper.batchInsertWithStateless(users);
}
```

## 审计功能

### 实体类配置

```java
import org.hibernate.solon.annotation.CreatedDate;
import org.hibernate.solon.annotation.LastModifiedDate;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @CreatedDate
    @Column(name = "create_time")
    private LocalDateTime createTime;  // 插入时自动设置
    
    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;  // 插入和更新时自动设置
    
    // Getters and Setters
    // ...
}
```

### 使用示例

```java
@Service
public class UserService {
    
    @Inject
    private UserRepository userRepository;
    
    @Tran
    public User createUser(String name) {
        User user = new User();
        user.setName(name);
        // createTime和updateTime会自动设置
        return userRepository.save(user);
    }
    
    @Tran
    public User updateUser(Long id, String name) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setName(name);
            // updateTime会自动更新
            return userRepository.save(user);
        }
        return null;
    }
}
```

## 缓存使用

### 1. 配置缓存

在`app.yml`中配置：

```yaml
jpa.db1:
  properties:
    hibernate:
      cache:
        use_second_level_cache: true
        use_query_cache: true
        region:
          factory_class: org.hibernate.cache.ehcache.EhCacheRegionFactory
```

### 2. 实体类缓存

```java
import org.hibernate.solon.annotation.Cacheable;
import javax.persistence.*;

@Entity
@Table(name = "user")
@Cacheable(strategy = Cacheable.CacheStrategy.READ_WRITE)
public class User {
    // ...
}
```

### 3. 查询缓存

```java
@Tran
public List<User> findCachedUsers() {
    Session session = sessionFactory.getCurrentSession();
    return session.createQuery("FROM User", User.class)
                  .setCacheable(true)  // 启用查询缓存
                  .list();
}
```

## 性能监控

### 1. 启用统计

在配置中启用：

```yaml
jpa.db1:
  properties:
    hibernate:
      generate_statistics: true
```

### 2. 使用性能监控器

```java
@Component
public class MonitorService {
    
    @Db
    private SessionFactory sessionFactory;
    
    public void printPerformanceReport() {
        PerformanceMonitor monitor = new PerformanceMonitor(sessionFactory);
        
        // 获取性能报告
        String report = monitor.getPerformanceReport();
        System.out.println(report);
        
        // 获取缓存命中率
        System.out.println("二级缓存命中率: " + 
            String.format("%.2f%%", monitor.getSecondLevelCacheHitRate() * 100));
        System.out.println("查询缓存命中率: " + 
            String.format("%.2f%%", monitor.getQueryCacheHitRate() * 100));
    }
    
    public void detectSlowQueries() {
        SlowQueryDetector detector = new SlowQueryDetector(sessionFactory, 1000);
        
        // 检测并记录慢查询
        detector.logSlowQueries();
        
        // 获取慢查询列表
        var slowQueries = detector.detectSlowQueries();
        for (var info : slowQueries) {
            System.out.println("慢查询: " + info.getQuery());
            System.out.println("  最大执行时间: " + info.getMaxExecutionTime() + " ms");
        }
    }
}
```

## 完整示例

### 完整的Service示例

```java
import org.hibernate.solon.integration.query.DynamicQueryBuilder;
import org.hibernate.solon.integration.query.PageQuery;
import org.hibernate.solon.test.entity.User;
import org.hibernate.solon.test.repository.UserRepository;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Service;
import org.noear.solon.data.annotation.Tran;

import java.util.List;

@Service
public class UserService {
    
    @Inject
    private UserRepository userRepository;
    
    // 保存用户
    @Tran
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    // 批量保存
    @Tran
    public void batchSaveUsers(List<User> users) {
        userRepository.saveAll(users);
    }
    
    // 查找用户
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    // 分页查询
    public PageQuery<User> findUsersPage(int page, int size) {
        return userRepository.findAll(page, size);
    }
    
    // 动态搜索
    public List<User> searchUsers(String name, Integer minAge) {
        return userRepository.searchUsers(name, minAge);
    }
    
    // 分页动态搜索
    public PageQuery<User> searchUsersPage(String name, Integer minAge, int page, int size) {
        return userRepository.searchUsersPage(name, minAge, page, size);
    }
    
    // 删除用户
    @Tran
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    // 批量删除
    @Tran
    public void batchDeleteUsers(List<User> users) {
        userRepository.deleteAll(users);
    }
}
```

### 完整的Controller示例

```java
import org.hibernate.solon.integration.query.PageQuery;
import org.hibernate.solon.test.entity.User;
import org.hibernate.solon.test.service.UserService;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;

import java.util.List;

@Controller
@Mapping("/api/user")
public class UserController {
    
    @Inject
    private UserService userService;
    
    @Mapping("/create")
    public User createUser(String name, Integer age, String email) {
        User user = new User();
        user.setName(name);
        user.setAge(age);
        user.setEmail(email);
        return userService.saveUser(user);
    }
    
    @Mapping("/get")
    public User getUser(Long id) {
        return userService.findById(id);
    }
    
    @Mapping("/list")
    public List<User> listUsers() {
        return userService.findAll();
    }
    
    @Mapping("/page")
    public PageQuery<User> pageUsers(int page, int size) {
        return userService.findUsersPage(page, size);
    }
    
    @Mapping("/search")
    public List<User> searchUsers(String name, Integer minAge) {
        return userService.searchUsers(name, minAge);
    }
    
    @Mapping("/delete")
    public String deleteUser(Long id) {
        userService.deleteUser(id);
        return "删除成功";
    }
}
```

## 注意事项

1. **事务管理**：批量操作和写操作需要在事务中执行，使用`@Tran`注解
2. **缓存配置**：使用二级缓存需要添加相应的缓存依赖（如EhCache）
3. **性能监控**：启用`generate_statistics`会有性能开销，生产环境建议关闭
4. **批量操作**：批量大小建议设置为20-50，过大可能导致内存问题
5. **懒加载**：避免在事务外访问懒加载属性，会导致LazyInitializationException

## 更多示例

更多使用示例请参考：
- `src/test/java/org/hibernate/solon/test/` 目录下的测试类
- `IMPLEMENTATION_SUMMARY.md` 实现总结文档

