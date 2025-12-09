# Hibernate DDL 详解

## 什么是 DDL？

**DDL** = **Data Definition Language（数据定义语言）**

DDL是SQL语言的一部分，用于定义和管理数据库结构，包括：
- 创建表（CREATE TABLE）
- 删除表（DROP TABLE）
- 修改表结构（ALTER TABLE）
- 创建索引（CREATE INDEX）
- 等等

## Hibernate DDL 的作用

Hibernate DDL功能可以**自动从Java实体类生成数据库表结构**，让你不需要手动编写SQL建表语句。

### 核心功能

1. **自动生成建表SQL**
   - 根据`@Entity`实体类自动生成`CREATE TABLE`语句
   - 根据`@Column`注解生成字段定义
   - 根据`@Id`、`@GeneratedValue`生成主键
   - 根据`@Table`注解生成表名

2. **自动执行DDL**
   - 应用启动时自动创建表
   - 应用启动时自动更新表结构
   - 应用启动时验证表结构

3. **生成DDL脚本**
   - 导出SQL脚本文件
   - 用于数据库迁移和版本控制

## 工作原理

```
Java实体类 → Hibernate分析注解 → 生成Metadata → 生成DDL SQL → 执行到数据库
```

### 示例流程

#### 1. 定义实体类

```java
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
}
```

#### 2. Hibernate自动生成DDL

```sql
CREATE TABLE user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    age INTEGER NOT NULL,
    email VARCHAR(100),
    PRIMARY KEY (id)
) ENGINE=InnoDB;
```

#### 3. 自动执行到数据库

根据配置，Hibernate会自动执行这个SQL，创建`user`表。

## DDL策略说明

### 1. `none` - 不执行（默认）

```yaml
hibernate:
  hbm2ddl:
    auto: none
```

**作用**：不执行任何DDL操作，完全由开发者手动管理数据库。

**适用场景**：
- 生产环境
- 已有完整的数据库迁移方案（如Flyway、Liquibase）

### 2. `create` - 创建表

```yaml
hibernate:
  hbm2ddl:
    auto: create
```

**作用**：应用启动时，删除所有表，然后重新创建。

**⚠️ 警告**：会删除所有数据！

**适用场景**：
- 开发环境
- 测试环境（每次启动清空数据）

### 3. `create-drop` - 创建并删除

```yaml
hibernate:
  hbm2ddl:
    auto: create-drop
```

**作用**：
- 启动时：创建所有表
- 关闭时：删除所有表

**适用场景**：
- 单元测试
- 集成测试

### 4. `update` - 更新表结构

```yaml
hibernate:
  hbm2ddl:
    auto: update
```

**作用**：
- 如果表不存在，创建表
- 如果表存在，添加缺失的列和约束
- **不会删除**已存在的列

**适用场景**：
- 开发环境
- 快速原型开发

**⚠️ 注意**：
- 不会删除列
- 不会修改列类型
- 复杂的结构变更可能失败

### 5. `validate` - 验证表结构

```yaml
hibernate:
  hbm2ddl:
    auto: validate
```

**作用**：
- 验证数据库表结构是否与实体类匹配
- **不修改数据库**，只验证
- 如果不匹配，启动失败

**适用场景**：
- 生产环境（安全检查）
- 确保数据库结构正确

## 实际应用场景

### 场景1：快速开发

```yaml
# 开发环境配置
jpa.db1:
  properties:
    hibernate:
      hbm2ddl:
        auto: update  # 自动更新表结构
```

**好处**：
- 修改实体类后，重启应用即可更新表结构
- 不需要手动写SQL
- 快速迭代

### 场景2：生成迁移脚本

```java
// 生成DDL脚本，用于数据库迁移
DdlGenerator generator = adapter.getDdlGenerator();
generator.generateDdlToFile("migration/v1.0.0__create_tables.sql", true);
```

**好处**：
- 版本控制
- 团队协作
- 生产环境部署

### 场景3：生产环境验证

```yaml
# 生产环境配置
jpa.db1:
  properties:
    hibernate:
      hbm2ddl:
        auto: validate  # 只验证，不修改
```

**好处**：
- 确保数据库结构正确
- 防止意外修改
- 启动时发现问题

## 完整示例

### 步骤1：定义实体类

```java
package com.example.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal price;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    // Getters and Setters...
}
```

### 步骤2：配置Hibernate

```yaml
# app.yml
jpa.db1:
  mappings:
    - com.example.entity.*
  properties:
    hibernate:
      dialect: org.hibernate.dialect.MySQL8Dialect
      hbm2ddl:
        auto: update  # 自动更新表结构
      show_sql: true
      format_sql: true
```

### 步骤3：启动应用

应用启动时，Hibernate会自动：
1. 扫描`com.example.entity`包下的所有实体类
2. 分析`@Entity`、`@Table`、`@Column`等注解
3. 生成DDL SQL
4. 执行到数据库（根据`hbm2ddl.auto`配置）

### 步骤4：查看生成的表

```sql
-- Hibernate自动生成的表结构
CREATE TABLE product (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    description VARCHAR(500),
    create_time DATETIME,
    update_time DATETIME,
    PRIMARY KEY (id)
) ENGINE=InnoDB;
```

## 手动使用DDL功能

### 1. 生成DDL脚本

```java
import org.hibernate.solon.integration.HibernateAdapter;
import org.hibernate.solon.integration.HibernateAdapterManager;
import org.hibernate.solon.integration.schema.DdlGenerator;

// 获取适配器
HibernateAdapter adapter = HibernateAdapterManager.getOnly("db1");

// 获取DDL生成器
DdlGenerator generator = adapter.getDdlGenerator();

// 生成DDL到文件
generator.generateDdlToFile("schema.sql", true);

// 生成DDL字符串
String ddl = generator.generateDdlString(true);
System.out.println(ddl);
```

### 2. 执行DDL操作

```java
import org.hibernate.solon.integration.schema.SchemaManager;

// 获取Schema管理器
SchemaManager schemaManager = adapter.getSchemaManager();

// 创建表
schemaManager.createSchema(false);

// 更新表结构
schemaManager.updateSchema();

// 删除表
schemaManager.dropSchema();

// 验证表结构
SchemaManager.SchemaValidationResult result = schemaManager.validateSchema();
if (result.isValid()) {
    System.out.println("验证通过");
} else {
    System.out.println("验证失败: " + result.getMessage());
}
```

## 注意事项

### ⚠️ 生产环境警告

1. **不要使用`create`或`create-drop`**
   - 会删除所有数据
   - 会导致数据丢失

2. **谨慎使用`update`**
   - 不会删除列
   - 复杂的结构变更可能失败
   - 建议使用数据库迁移工具

3. **推荐使用`validate`或`none`**
   - 只验证，不修改
   - 安全可靠

### ✅ 最佳实践

1. **开发环境**：使用`update`快速迭代
2. **测试环境**：使用`create-drop`每次清空
3. **生产环境**：使用`validate`或`none`，配合迁移工具

### 🔧 替代方案

对于生产环境，建议使用专业的数据库迁移工具：
- **Flyway**：基于SQL脚本的迁移工具
- **Liquibase**：支持多种格式的迁移工具

这些工具提供：
- 版本控制
- 回滚功能
- 更精确的控制
- 更好的团队协作

## 总结

Hibernate DDL功能的核心价值：

1. **开发效率**：自动生成表结构，无需手写SQL
2. **快速迭代**：修改实体类即可更新数据库
3. **减少错误**：自动处理类型映射、约束等
4. **脚本生成**：可以导出SQL用于迁移

**适用场景**：
- ✅ 快速原型开发
- ✅ 开发环境
- ✅ 测试环境
- ❌ 生产环境（建议使用迁移工具）

