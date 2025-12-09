# Hibernate-Solon插件

基于Solon框架的Hibernate集成插件，提供完整的ORM功能和Solon风格的API。

## 功能特性

### ✅ 已实现功能

1. **基础ORM功能**
   - 实体类映射和关系映射
   - SessionFactory和EntityManager支持
   - 多数据源支持
   - 基本CRUD操作

2. **事务管理**
   - 与Solon事务系统完美集成
   - 声明式事务支持
   - 事务传播和隔离级别

3. **查询功能增强**
   - HibernateQueryHelper查询助手
   - 分页查询支持（PageQuery）
   - 动态查询构建器（DynamicQueryBuilder）
   - 命名查询注册表（NamedQueryRegistry）

4. **批量操作优化**
   - 批量保存/更新/删除
   - 可配置批量大小
   - StatelessSession高性能批量插入

5. **懒加载配置**
   - 默认懒加载策略
   - 批量抓取优化
   - N+1查询问题预防

6. **二级缓存支持**
   - 缓存配置管理
   - @Cacheable注解支持
   - 查询缓存支持

7. **性能监控**
   - 性能统计收集
   - 慢查询检测
   - 缓存命中率监控

8. **审计功能**
   - @CreatedDate自动设置创建时间
   - @LastModifiedDate自动更新修改时间
   - 支持多种时间类型

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>org.noear</groupId>
    <artifactId>hibernate-solon-plugin</artifactId>
    <version>3.7.3</version>
</dependency>
```

### 2. 配置数据源

在`app.yml`中配置：

```yaml
test.db1:
  jdbcUrl: jdbc:mysql://localhost:3306/test
  driverClassName: com.mysql.cj.jdbc.Driver
  username: root
  password: root

jpa.db1:
  mappings:
    - com.example.entity.*
  properties:
    hibernate:
      dialect: org.hibernate.dialect.MySQL8Dialect
      hbm2ddl:
        auto: update
      show_sql: true
```

### 3. 启用Hibernate

在启动类上添加注解：

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

### 4. 创建实体类

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
    private LocalDateTime createTime;
    
    @LastModifiedDate
    private LocalDateTime updateTime;
    
    // Getters and Setters
}
```

### 5. 创建Repository

```java
import org.hibernate.solon.integration.HibernateRepository;
import org.noear.solon.annotation.Component;

@Component
public class UserRepository extends HibernateRepository<User, Long> {
    public UserRepository() {
        super(User.class);
    }
}
```

### 6. 使用Repository

```java
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Component;
import org.noear.solon.data.annotation.Tran;

@Component
public class UserService {
    @Inject
    private UserRepository userRepository;
    
    @Tran
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    public List<User> findAll() {
        return userRepository.findAll();
    }
}
```

## 核心功能使用

### 查询功能

#### 分页查询

```java
// 使用Repository
PageQuery<User> page = userRepository.findAll(1, 10);

// 使用查询助手
Session session = sessionFactory.getCurrentSession();
HibernateQueryHelper helper = new HibernateQueryHelper(session);
PageQuery<User> page = helper.pageQuery("FROM User", User.class, 1, 10);
```

#### 动态查询

```java
DynamicQueryBuilder builder = new DynamicQueryBuilder("FROM User u");
builder.like("u.name", "name", "张")
       .where("u.age >= :age", "age", 18)
       .orderBy("u.createTime DESC");

List<User> users = userRepository.findByBuilder(builder);
```

### 批量操作

```java
// 批量保存
List<User> users = ...;
userRepository.saveAll(users);

// 批量更新
userRepository.updateAll(users);

// 批量删除
userRepository.deleteAll(users);
```

### 审计功能

```java
@Entity
public class User {
    @CreatedDate
    private LocalDateTime createTime;  // 插入时自动设置
    
    @LastModifiedDate
    private LocalDateTime updateTime;  // 插入和更新时自动设置
}
```

### 性能监控

```java
PerformanceMonitor monitor = new PerformanceMonitor(sessionFactory);
String report = monitor.getPerformanceReport();
System.out.println(report);

SlowQueryDetector detector = new SlowQueryDetector(sessionFactory, 1000);
detector.logSlowQueries();
```

## 测试示例

项目包含完整的测试示例，位于`src/test/java/org/hibernate/solon/test/`目录：

- `TestApp.java` - 测试应用启动类
- `entity/User.java` - 实体类示例
- `repository/UserRepository.java` - Repository示例
- `service/UserService.java` - Service示例
- `controller/UserController.java` - Controller示例
- `QueryHelperTest.java` - 查询功能测试
- `BatchOperationTest.java` - 批量操作测试
- `PerformanceMonitorTest.java` - 性能监控测试

## 文档

- [功能清单与扩展规划](HIBERNATE_FEATURES.md) - 详细的功能清单和扩展计划
- [实现总结](IMPLEMENTATION_SUMMARY.md) - 已实现功能的详细说明
- [使用示例](USAGE_EXAMPLES.md) - 完整的使用示例和最佳实践

## 配置说明

### 完整配置示例

```yaml
jpa.db1:
  mappings:
    - com.example.entity.*
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

## 注意事项

1. **事务管理**：批量操作和写操作需要在事务中执行
2. **缓存配置**：使用二级缓存需要添加相应的缓存依赖
3. **性能监控**：启用`generate_statistics`会有性能开销
4. **懒加载**：避免在事务外访问懒加载属性

## 版本要求

- JDK 21+
- Solon 3.7.3+
- Hibernate 5.6.15.Final

## 许可证

Apache License 2.0

## 贡献

欢迎提交Issue和Pull Request！

