# AutoTable 功能增强文档

## 概述

本文档介绍 Hibernate-Solon 插件中 AutoTable（自动表创建）功能的增强特性。

## 新增功能

### 1. 增强的日志和统计

#### 功能说明

自动表创建时会输出详细的日志信息，包括：
- 执行策略
- 执行时间
- 创建的表数量
- 表名列表

#### 日志示例

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
开始执行Hibernate自动DDL (adapter: db1, strategy: update)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
执行策略: UPDATE - 更新表结构
✅ Hibernate自动更新Schema完成 (adapter: db1, 耗时: 1234ms)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Hibernate自动表创建统计 (adapter: db1)
  表数量: 5
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
已创建的表: user, product, category, order, order_item
```

### 2. 错误处理增强

#### 配置选项

```yaml
jpa.db1:
  properties:
    hibernate:
      # 执行DDL时出错是否跳过（继续执行）
      ddl_skip_on_error: false  # 默认false，出错时抛出异常
```

#### 行为说明

- `ddl_skip_on_error: false`（默认）：出错时抛出异常，应用启动失败
- `ddl_skip_on_error: true`：出错时记录警告日志，继续执行

### 3. 表命名策略

#### 支持的策略

1. **下划线命名**（默认）
   - 实体类：`UserInfo` → 表名：`user_info`
   - 字段：`userName` → 列名：`user_name`

2. **驼峰命名**
   - 实体类：`UserInfo` → 表名：`UserInfo`
   - 字段：`userName` → 列名：`userName`

3. **大写命名**
   - 实体类：`UserInfo` → 表名：`USER_INFO`
   - 字段：`userName` → 列名：`USER_NAME`

4. **小写命名**
   - 实体类：`UserInfo` → 表名：`user_info`
   - 字段：`userName` → 列名：`user_name`

#### 配置方式

```yaml
jpa.db1:
  properties:
    hibernate:
      # 物理命名策略
      physical_naming_strategy: org.hibernate.solon.integration.schema.TableNamingStrategy
      
      # 表前缀（可选）
      table_prefix: t_
```

#### 使用示例

```java
// 实体类
@Entity
public class UserInfo {
    @Id
    private Long id;
    
    @Column
    private String userName;
}

// 使用下划线命名策略 + 前缀 t_
// 生成的表结构：
// CREATE TABLE t_user_info (
//     id BIGINT,
//     user_name VARCHAR(255)
// )
```

### 4. 表结构验证增强

#### 功能说明

- 更详细的验证信息
- 表数量统计
- 验证结果报告

#### 使用示例

```java
SchemaManager schemaManager = adapter.getSchemaManager();
SchemaManager.SchemaValidationResult result = schemaManager.validateSchema();

if (result.isValid()) {
    System.out.println("验证通过: " + result.getMessage());
} else {
    System.out.println("验证失败: " + result.getMessage());
}
```

### 5. 表变更检测

#### 功能说明

检测实体类与数据库表结构的差异，报告：
- 新增的表
- 修改的表
- 删除的表

#### 使用示例

```java
import org.hibernate.solon.integration.schema.AutoTableEnhancer;

AutoTableEnhancer.TableChangeReport report = 
    AutoTableEnhancer.detectTableChanges(adapter);

if (!report.isValid()) {
    System.out.println("检测到表结构变更:");
    System.out.println("新增表: " + report.getAddedTables());
    System.out.println("修改表: " + report.getModifiedTables());
    System.out.println("删除表: " + report.getRemovedTables());
}
```

## 配置选项

### 完整配置示例

```yaml
jpa.db1:
  mappings:
    - com.example.entity.*
  properties:
    hibernate:
      # DDL策略
      hbm2ddl:
        auto: update
      
      # 表命名策略
      physical_naming_strategy: org.hibernate.solon.integration.schema.TableNamingStrategy
      table_prefix: t_
      
      # 增强功能配置
      enable_table_comments: true      # 启用表注释（MySQL）
      ddl_log: true                     # 启用DDL日志
      enable_schema_validation: true   # 启用Schema验证
      ddl_skip_on_error: false         # 出错时是否跳过
      
      # SQL日志
      show_sql: true
      format_sql: true
```

## 最佳实践

### 1. 开发环境

```yaml
hibernate:
  hbm2ddl:
    auto: update
  ddl_log: true
  ddl_skip_on_error: false  # 开发环境应该及时发现错误
```

### 2. 测试环境

```yaml
hibernate:
  hbm2ddl:
    auto: create-drop
  ddl_log: true
  ddl_skip_on_error: false
```

### 3. 生产环境

```yaml
hibernate:
  hbm2ddl:
    auto: validate
  ddl_log: false
  enable_schema_validation: true
  ddl_skip_on_error: false  # 生产环境必须严格
```

## 使用示例

### 示例1：使用表命名策略

```java
@Entity
@Table(name = "UserInfo")  // 即使指定了表名，命名策略仍会应用
public class UserInfo {
    @Id
    private Long id;
    
    @Column(name = "userName")
    private String userName;
}
```

配置：
```yaml
hibernate:
  physical_naming_strategy: org.hibernate.solon.integration.schema.TableNamingStrategy
  table_prefix: app_
```

结果：
- 表名：`app_user_info`
- 列名：`user_name`

### 示例2：错误处理

```yaml
hibernate:
  hbm2ddl:
    auto: update
  ddl_skip_on_error: true  # 出错时继续执行
```

当DDL执行出错时：
- 记录警告日志
- 继续执行后续操作
- 应用正常启动

### 示例3：表结构统计

应用启动后，会自动输出表统计信息：

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Hibernate自动表创建统计 (adapter: db1)
  表数量: 5
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
已创建的表: user, product, category, order, order_item
```

## 故障排查

### 问题1：表名不符合预期

**原因**：命名策略配置不正确

**解决**：
1. 检查 `physical_naming_strategy` 配置
2. 检查 `table_prefix` 配置
3. 查看日志中的实际表名

### 问题2：DDL执行失败但应用仍启动

**原因**：`ddl_skip_on_error` 配置为 `true`

**解决**：将 `ddl_skip_on_error` 设置为 `false`，确保错误能被及时发现

### 问题3：表统计信息不显示

**原因**：日志级别设置过高

**解决**：确保日志级别包含 `INFO` 级别

## 相关类

- `AutoTableConfig` - 自动表配置类
- `AutoTableEnhancer` - 自动表增强器
- `TableNamingStrategy` - 表命名策略
- `SchemaAutoExecutor` - Schema自动执行器

## 参考

- [HBM2DDL_AUTO_GUIDE.md](./HBM2DDL_AUTO_GUIDE.md) - 完整指南
- [HBM2DDL_QUICK_START.md](./HBM2DDL_QUICK_START.md) - 快速开始

