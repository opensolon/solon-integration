# Hibernate hbm2ddl.auto 快速开始

## 5分钟快速上手

### 步骤1：添加依赖

确保项目中已包含 Hibernate-Solon 插件依赖。

### 步骤2：配置数据源和Hibernate

在 `app.yml` 中添加配置：

```yaml
# 数据源配置
test.db1:
  jdbcUrl: jdbc:mysql://localhost:3306/test
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
        auto: update  # 👈 关键配置：自动更新表结构
      show_sql: true
```

### 步骤3：创建实体类

```java
package com.example.entity;

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
    
    @Column(nullable = false, length = 100, unique = true)
    private String email;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    // Getters and Setters...
}
```

### 步骤4：启动应用

启动应用后，Hibernate会自动：
1. ✅ 扫描实体类
2. ✅ 分析注解
3. ✅ 生成DDL
4. ✅ 执行到数据库

### 步骤5：查看生成的表

```sql
-- Hibernate自动生成的表
CREATE TABLE user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    create_time DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY (email)
) ENGINE=InnoDB;
```

## 支持的注解特性

### ✅ 基础注解

```java
@Entity                    // 标识实体类
@Table(name = "user")     // 指定表名
@Id                       // 主键
@GeneratedValue           // 主键生成策略
@Column                   // 列定义
```

### ✅ 列属性

```java
@Column(
    nullable = false,      // 不为空
    length = 100,          // 字符长度
    unique = true,         // 唯一约束
    precision = 10,        // 数值精度
    scale = 2              // 小数位数
)
```

### ✅ 索引

```java
@Table(
    indexes = {
        @Index(name = "idx_name", columnList = "name"),
        @Index(name = "idx_composite", columnList = "col1,col2")
    }
)
```

### ✅ 唯一约束

```java
@Table(
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_code", columnNames = {"code"})
    }
)
```

### ✅ 大文本

```java
@Lob
@Column(name = "content")
private String content;
```

### ✅ 枚举

```java
@Enumerated(EnumType.STRING)
@Column(name = "status", length = 20)
private Status status;
```

## 配置选项

### hbm2ddl.auto 策略

| 策略 | 说明 | 适用场景 |
|------|------|----------|
| `none` | 不执行任何操作 | 生产环境 |
| `create` | 启动时创建表（会删除已存在的表） | 开发/测试 |
| `create-drop` | 启动创建，关闭删除 | 单元测试 |
| `update` | 启动时更新表结构 | **开发环境推荐** |
| `validate` | 验证表结构，不修改 | 生产环境 |

## 完整示例

### 实体类（包含各种注解）

```java
@Entity
@Table(
    name = "product",
    indexes = {
        @Index(name = "idx_product_name", columnList = "name"),
        @Index(name = "idx_product_category", columnList = "category_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_code", columnNames = {"code"})
    }
)
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 32, unique = true)
    private String code;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProductStatus status;
    
    // ...
}
```

### 配置

```yaml
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

### 生成的DDL

```sql
CREATE TABLE product (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(200) NOT NULL,
    price DECIMAL(10,2),
    status VARCHAR(20),
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_code (code),
    KEY idx_product_name (name),
    KEY idx_product_category (category_id)
) ENGINE=InnoDB;
```

## 常见问题

**Q: 修改实体类后，表结构没有更新？**

A: 确保 `hbm2ddl.auto` 配置为 `update`。

**Q: 如何删除不需要的列？**

A: `update` 策略不会删除列，需要手动执行 `ALTER TABLE` 或使用迁移工具。

**Q: 索引没有创建？**

A: 检查 `@Index` 注解和 `columnList` 配置是否正确。

## 参考文档

- 完整指南：`HBM2DDL_AUTO_GUIDE.md`
- DDL功能：`DDL_USAGE.md`
- DDL说明：`DDL_EXPLANATION.md`

