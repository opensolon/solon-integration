# 测试指南

## 📋 目录

- [测试环境准备](#测试环境准备)
- [数据库表结构创建](#数据库表结构创建)
- [测试配置](#测试配置)
- [运行测试](#运行测试)
- [测试类说明](#测试类说明)
- [常见问题](#常见问题)

## 🚀 测试环境准备

### 1. 数据库准备

#### 方式一：使用自动DDL（推荐）

在配置文件中设置 `hbm2ddl.auto=create` 或 `update`，应用启动时会自动创建表结构。

```yaml
jpa.db1:
  properties:
    hibernate:
      hbm2ddl:
        auto: create  # 或 update
```

**优点**：无需手动创建表，自动根据实体类生成  
**缺点**：会修改数据库结构，需谨慎使用

#### 方式二：手动执行SQL脚本（推荐用于生产环境）

1. 找到SQL脚本文件：`src/test/resources/test_schema.sql`
2. 在数据库中执行该脚本

```bash
# MySQL示例
mysql -u root -p test < src/test/resources/test_schema.sql
```

**优点**：可控性强，不会意外修改数据库  
**缺点**：需要手动维护SQL脚本

#### 方式三：使用DDL生成器生成SQL

运行 `DdlGeneratorTest.testGenerateDdlToFile()` 方法，会生成DDL脚本到 `target/schema.sql`，然后手动执行。

### 2. 配置文件准备

确保 `src/test/resources/app.yml` 或 `app-hbm2ddl.yml` 中配置了正确的数据库连接信息：

```yaml
test.db1:
  jdbcUrl: jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai
  driverClassName: com.mysql.cj.jdbc.Driver
  username: root
  password: root

jpa.db1:
  mappings:
    - org.hibernate.solon.test.entity.*
  properties:
    hibernate:
      dialect: org.hibernate.dialect.MySQL8Dialect
      hbm2ddl:
        auto: update  # 或 create/create-drop/validate
```

## 📊 数据库表结构创建

### 测试实体类对应的表

| 实体类 | 表名 | 说明 |
|--------|------|------|
| `User` | `test_user` | 用户表，基础测试 |
| `Product` | `product` | 产品表，包含索引和约束 |
| `Category` | `category` | 分类表，树形结构 |

### 快速创建表结构

#### 方法1：使用SQL脚本（推荐）

```sql
-- 执行 src/test/resources/test_schema.sql
-- 包含所有测试表的创建语句
```

#### 方法2：使用配置自动创建

```yaml
jpa.db1:
  properties:
    hibernate:
      hbm2ddl:
        auto: create  # 启动时自动创建
```

#### 方法3：使用SchemaManager手动创建

```java
@Tran
public void createTables() {
    HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
    SchemaManager schemaManager = adapter.getSchemaManager();
    schemaManager.createSchema(false);  // 不删除已存在的表
}
```

### 表结构详情

#### test_user 表

```sql
CREATE TABLE `test_user` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `age` INT(11) NOT NULL,
    `email` VARCHAR(100) DEFAULT NULL,
    `create_time` DATETIME DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`)
);
```

#### product 表

```sql
CREATE TABLE `product` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `code` VARCHAR(32) NOT NULL,
    `name` VARCHAR(200) NOT NULL,
    `description` TEXT,
    `price` DECIMAL(10,2) NOT NULL,
    `stock` INT(11) NOT NULL DEFAULT '0',
    `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    `category_id` BIGINT(20) NOT NULL,
    `create_time` DATETIME NOT NULL,
    `update_time` DATETIME NOT NULL,
    `deleted` TINYINT(1) NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_code` (`code`),
    KEY `idx_product_name` (`name`),
    KEY `idx_product_category` (`category_id`),
    KEY `idx_product_status` (`status`),
    KEY `idx_product_category_status` (`category_id`, `status`)
);
```

#### category 表

```sql
CREATE TABLE `category` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `parent_id` BIGINT(20) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_name_parent` (`name`, `parent_id`)
);
```

## ⚙️ 测试配置

### 基础配置示例

```yaml
# app.yml 或 app-hbm2ddl.yml

# 数据源配置
test.db1:
  jdbcUrl: jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai
  driverClassName: com.mysql.cj.jdbc.Driver
  username: root
  password: root
  maximumPoolSize: 10
  minimumIdle: 5

# Hibernate配置
jpa.db1:
  # 实体类映射包
  mappings:
    - org.hibernate.solon.test.entity.*
  
  # Hibernate属性
  properties:
    hibernate:
      # 数据库方言（必须）
      dialect: org.hibernate.dialect.MySQL8Dialect
      
      # DDL自动执行策略
      hbm2ddl:
        auto: update  # 推荐：update（开发）或 validate（生产）
      
      # SQL日志（测试时建议开启）
      show_sql: true
      format_sql: true
      
      # 批量操作
      jdbc:
        batch_size: 50
```

### 不同环境的配置建议

#### 开发环境

```yaml
hbm2ddl:
  auto: update  # 自动更新表结构
show_sql: true  # 显示SQL
```

#### 测试环境

```yaml
hbm2ddl:
  auto: create-drop  # 启动创建，关闭删除
show_sql: true
```

#### 生产环境

```yaml
hbm2ddl:
  auto: validate  # 只验证，不修改
show_sql: false
```

## 🧪 运行测试

### 方式1：使用IDE运行

1. 打开测试类文件
2. 右键点击测试方法
3. 选择 "Run" 或 "Debug"

### 方式2：使用Maven运行

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=QueryHelperTest

# 运行特定测试方法
mvn test -Dtest=QueryHelperTest#testBasicQuery
```

### 方式3：通过Controller调用

某些测试类可以通过HTTP接口调用：

```java
// 访问 http://localhost:8080/api/test/query
@Controller
@Mapping("/api/test")
public class TestController {
    @Inject
    private QueryHelperTest queryHelperTest;
    
    @Mapping("/query")
    public void testQuery() {
        queryHelperTest.testBasicQuery();
    }
}
```

## 📝 测试类说明

### 核心功能测试

| 测试类 | 功能 | 是否需要表结构 |
|--------|------|----------------|
| `QueryHelperTest` | 查询助手、分页查询 | ✅ 是 |
| `RepositoryTest` | Repository CRUD | ✅ 是 |
| `BatchOperationTest` | 批量操作 | ✅ 是 |
| `TransactionTest` | 事务集成 | ✅ 是 |
| `AuditTest` | 审计功能 | ✅ 是 |

### DDL功能测试

| 测试类 | 功能 | 是否需要表结构 |
|--------|------|----------------|
| `DdlGeneratorTest` | DDL生成 | ❌ 否（会生成DDL） |
| `SchemaManagerTest` | Schema管理 | ⚠️ 部分需要 |
| `AutoTableTest` | 自动表功能 | ⚠️ 部分需要 |

### 其他功能测试

| 测试类 | 功能 | 是否需要表结构 |
|--------|------|----------------|
| `PerformanceMonitorTest` | 性能监控 | ✅ 是 |
| `CacheTest` | 缓存功能 | ✅ 是 |
| `LazyLoadTest` | 懒加载 | ✅ 是 |
| `NamedQueryTest` | 命名查询 | ✅ 是 |
| `IntegrationTest` | 集成测试 | ✅ 是 |

## 🔧 常见问题

### Q1: 测试时提示"表不存在"

**解决方案**：

1. **检查配置**：确保 `hbm2ddl.auto` 设置为 `create` 或 `update`
2. **手动创建**：执行 `src/test/resources/test_schema.sql`
3. **生成DDL**：运行 `DdlGeneratorTest` 生成DDL脚本，然后手动执行

### Q2: 测试时提示"连接数据库失败"

**解决方案**：

1. 检查数据库服务是否启动
2. 检查配置文件中的数据库连接信息
3. 检查数据库用户权限

### Q3: 如何清理测试数据？

**解决方案**：

```java
@Tran
public void cleanTestData() {
    Session session = sessionFactory.getCurrentSession();
    session.createQuery("DELETE FROM User").executeUpdate();
    session.createQuery("DELETE FROM Product").executeUpdate();
    session.createQuery("DELETE FROM Category").executeUpdate();
}
```

### Q4: 如何在不同数据库上运行测试？

**解决方案**：

1. 修改配置文件中的 `dialect` 和 `jdbcUrl`
2. 根据数据库类型调整SQL脚本（`test_schema.sql`）
3. 某些数据库可能需要调整字段类型

### Q5: 测试时如何避免修改生产数据库？

**解决方案**：

1. 使用独立的测试数据库
2. 在配置文件中使用测试环境配置
3. 使用 `hbm2ddl.auto=validate` 只验证不修改

## 📚 相关文档

- [DDL功能说明](./DDL_EXPLANATION.md)
- [hbm2ddl.auto使用指南](./HBM2DDL_AUTO_GUIDE.md)
- [测试覆盖报告](./TEST_COVERAGE.md)
- [快速开始指南](./HBM2DDL_QUICK_START.md)

## 💡 测试最佳实践

1. **使用测试数据库**：不要在生产数据库上运行测试
2. **清理测试数据**：每个测试后清理数据，避免相互影响
3. **使用事务**：测试方法使用 `@Tran` 注解，测试后自动回滚
4. **配置隔离**：测试环境使用独立的配置文件
5. **DDL策略**：测试环境使用 `create-drop`，开发环境使用 `update`

## 🎯 快速开始

1. **准备数据库**
   ```bash
   mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS test CHARACTER SET utf8mb4;"
   ```

2. **创建表结构**
   ```bash
   mysql -u root -p test < src/test/resources/test_schema.sql
   ```
   或配置 `hbm2ddl.auto=create`

3. **配置数据库连接**
   编辑 `src/test/resources/app.yml`

4. **运行测试**
   ```bash
   mvn test
   ```

---

**提示**：如果遇到问题，请查看 [常见问题](#常见问题) 部分或查看相关文档。

