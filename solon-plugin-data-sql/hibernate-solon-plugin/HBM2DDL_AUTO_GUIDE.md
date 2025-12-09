# Hibernate hbm2ddl.auto 完整指南

## 概述

`hbm2ddl.auto` 是 Hibernate 的核心功能，可以根据实体类的注解自动生成和执行数据库表结构。类似于 Halo 博客系统使用的功能。

## 功能特性

### ✅ 支持的注解特性

1. **基础注解**
   - `@Entity` - 标识实体类
   - `@Table` - 指定表名、索引、唯一约束
   - `@Id` - 主键
   - `@GeneratedValue` - 主键生成策略
   - `@Column` - 列定义（长度、精度、是否为空等）

2. **索引和约束**
   - `@Index` - 创建索引
   - `@UniqueConstraint` - 唯一约束
   - `unique = true` - 列级唯一约束

3. **数据类型**
   - `@Lob` - 大文本/大二进制
   - `@Enumerated` - 枚举类型
   - `precision` 和 `scale` - 数值精度

4. **时间字段**
   - `@CreatedDate` - 自动填充创建时间
   - `@LastModifiedDate` - 自动更新修改时间
   - `updatable = false` - 不可更新

5. **关系映射**
   - `@OneToMany` - 一对多
   - `@ManyToOne` - 多对一
   - `@ManyToMany` - 多对多
   - `@OneToOne` - 一对一

## 配置方式

### 方式1：YAML配置（推荐）

```yaml
# app.yml
jpa.db1:
  mappings:
    - com.example.entity.*
  properties:
    hibernate:
      # 数据库方言
      dialect: org.hibernate.dialect.MySQL8Dialect
      
      # DDL自动执行策略
      hbm2ddl:
        auto: update  # 可选: none, create, create-drop, update, validate
      
      # SQL日志
      show_sql: true
      format_sql: true
      use_sql_comments: true
```

### 方式2：Properties配置

```properties
# application.properties
jpa.db1.mappings=com.example.entity.*
jpa.db1.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
jpa.db1.properties.hibernate.hbm2ddl.auto=update
jpa.db1.properties.hibernate.show_sql=true
```

## 实体类示例

### 完整示例：Product实体

```java
package com.example.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "product",
    // 表级索引
    indexes = {
        @Index(name = "idx_product_name", columnList = "name"),
        @Index(name = "idx_product_category", columnList = "category_id"),
        @Index(name = "idx_product_category_status", columnList = "category_id,status")
    },
    // 唯一约束
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_code", columnNames = {"code"})
    }
)
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    // 唯一约束、不为空、固定长度
    @Column(name = "code", nullable = false, length = 32, unique = true)
    private String code;
    
    // 不为空、指定长度
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    // 长文本
    @Column(name = "description", length = 2000)
    @Lob
    private String description;
    
    // 精度和标度
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    // 枚举类型
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status;
    
    // 外键字段
    @Column(name = "category_id", nullable = false)
    private Long categoryId;
    
    // 创建时间（不可更新）
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    // 更新时间（自动更新）
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
    
    // Getters and Setters...
}
```

### 生成的DDL示例

```sql
CREATE TABLE product (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    category_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_code (code),
    KEY idx_product_name (name),
    KEY idx_product_category (category_id),
    KEY idx_product_category_status (category_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## DDL策略详解

### 1. `none` - 不执行（默认）

```yaml
hibernate:
  hbm2ddl:
    auto: none
```

**行为**：
- 不执行任何DDL操作
- 完全由开发者手动管理数据库

**适用场景**：
- 生产环境
- 使用数据库迁移工具（Flyway、Liquibase）

### 2. `create` - 创建表

```yaml
hibernate:
  hbm2ddl:
    auto: create
```

**行为**：
- 启动时：删除所有表，然后重新创建
- ⚠️ **会删除所有数据！**

**适用场景**：
- 开发环境
- 测试环境（每次启动清空数据）

### 3. `create-drop` - 创建并删除

```yaml
hibernate:
  hbm2ddl:
    auto: create-drop
```

**行为**：
- 启动时：创建所有表
- 关闭时：删除所有表

**适用场景**：
- 单元测试
- 集成测试

### 4. `update` - 更新表结构（推荐用于开发）

```yaml
hibernate:
  hbm2ddl:
    auto: update
```

**行为**：
- 如果表不存在：创建表
- 如果表存在：添加缺失的列和约束
- **不会删除**已存在的列
- **不会修改**列类型

**适用场景**：
- 开发环境
- 快速原型开发

**⚠️ 注意事项**：
- 不会删除列（需要手动删除）
- 不会修改列类型（需要手动修改）
- 复杂的结构变更可能失败

### 5. `validate` - 验证表结构

```yaml
hibernate:
  hbm2ddl:
    auto: validate
```

**行为**：
- 验证数据库表结构是否与实体类匹配
- **不修改数据库**，只验证
- 如果不匹配，启动失败

**适用场景**：
- 生产环境（安全检查）
- 确保数据库结构正确

## 常用注解说明

### @Column 注解属性

```java
@Column(
    name = "column_name",           // 列名
    nullable = false,                // 是否允许为空
    length = 100,                    // 字符串长度
    precision = 10,                  // 数值精度（总位数）
    scale = 2,                       // 数值标度（小数位数）
    unique = true,                   // 是否唯一
    updatable = true,                // 是否可更新
    insertable = true                // 是否可插入
)
```

### @Table 注解属性

```java
@Table(
    name = "table_name",             // 表名
    indexes = {                      // 索引
        @Index(name = "idx_name", columnList = "name"),
        @Index(name = "idx_composite", columnList = "col1,col2")
    },
    uniqueConstraints = {            // 唯一约束
        @UniqueConstraint(name = "uk_code", columnNames = {"code"})
    }
)
```

### @Index 注解

```java
@Index(
    name = "index_name",             // 索引名称
    columnList = "column1,column2"   // 索引列（支持多列）
)
```

### @UniqueConstraint 注解

```java
@UniqueConstraint(
    name = "constraint_name",        // 约束名称
    columnNames = {"col1", "col2"}   // 唯一列（支持多列）
)
```

## 完整配置示例

### app.yml 完整配置

```yaml
# 数据源配置
test.db1:
  jdbcUrl: jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
  driverClassName: com.mysql.cj.jdbc.Driver
  username: root
  password: root
  maximumPoolSize: 10
  minimumIdle: 5

# Hibernate配置
jpa.db1:
  # 实体类映射包
  mappings:
    - com.example.entity.*
  
  # Hibernate属性
  properties:
    hibernate:
      # 数据库方言
      dialect: org.hibernate.dialect.MySQL8Dialect
      
      # DDL自动执行策略
      hbm2ddl:
        auto: update
      
      # SQL日志
      show_sql: true
      format_sql: true
      use_sql_comments: true
      
      # 字符集
      connection:
        characterEncoding: utf8mb4
        useUnicode: true
      
      # 批量操作
      jdbc:
        batch_size: 50
        batch_versioned_data: true
```

## 使用流程

### 1. 定义实体类

```java
@Entity
@Table(name = "user", indexes = {
    @Index(name = "idx_user_email", columnList = "email")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(nullable = false, length = 100, unique = true)
    private String email;
}
```

### 2. 配置 hbm2ddl.auto

```yaml
jpa.db1:
  properties:
    hibernate:
      hbm2ddl:
        auto: update
```

### 3. 启动应用

应用启动时，Hibernate会自动：
1. 扫描实体类
2. 分析注解
3. 生成DDL
4. 执行到数据库

### 4. 查看生成的表

```sql
-- 自动生成的表结构
CREATE TABLE user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY idx_user_email (email)
) ENGINE=InnoDB;
```

## 最佳实践

### ✅ 开发环境

```yaml
hibernate:
  hbm2ddl:
    auto: update  # 自动更新表结构
  show_sql: true  # 显示SQL
```

### ✅ 测试环境

```yaml
hibernate:
  hbm2ddl:
    auto: create-drop  # 每次启动重新创建
```

### ✅ 生产环境

```yaml
hibernate:
  hbm2ddl:
    auto: validate  # 只验证，不修改
  show_sql: false  # 不显示SQL
```

## 常见问题

### Q1: 为什么修改了实体类，表结构没有更新？

**A**: 检查 `hbm2ddl.auto` 配置是否为 `update`。如果是 `none` 或 `validate`，不会自动更新。

### Q2: 如何删除不需要的列？

**A**: `update` 策略不会删除列。需要：
1. 手动执行 `ALTER TABLE` 删除列
2. 或使用 `create` 策略（会删除所有数据）
3. 或使用数据库迁移工具

### Q3: 如何生成DDL脚本而不执行？

**A**: 使用 `DdlGenerator`：

```java
DdlGenerator generator = adapter.getDdlGenerator();
generator.generateDdlToFile("schema.sql", true);
```

### Q4: 索引没有创建？

**A**: 检查：
1. `@Index` 注解是否正确
2. `columnList` 中的列名是否正确
3. 是否使用了 `update` 策略（会创建缺失的索引）

## 参考

- [Hibernate官方文档](https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html)
- [JPA注解参考](https://docs.oracle.com/javaee/7/api/javax/persistence/package-summary.html)

