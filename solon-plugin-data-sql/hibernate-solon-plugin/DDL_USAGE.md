# Hibernate DDL功能使用文档

## 概述

Hibernate-Solon插件提供了完整的DDL（Data Definition Language）功能，支持从实体类自动生成数据库Schema的DDL语句，并可以自动执行DDL操作。

## 功能特性

### ✅ 已实现功能

1. **DDL生成**
   - 从实体类生成CREATE TABLE语句
   - 从实体类生成DROP TABLE语句
   - 支持格式化SQL输出
   - 支持输出到文件或字符串

2. **Schema管理**
   - 创建Schema（createSchema）
   - 更新Schema（updateSchema）
   - 删除Schema（dropSchema）
   - 验证Schema（validateSchema）

3. **自动执行**
   - 根据`hbm2ddl.auto`配置自动执行DDL
   - 支持create、create-drop、update、validate策略

## 配置说明

### 1. 基础配置

在`app.yml`中配置DDL策略：

```yaml
jpa.db1:
  mappings:
    - com.example.entity.*
  properties:
    hibernate:
      # DDL策略
      hbm2ddl:
        auto: update  # 可选值: none, create, create-drop, update, validate
```

### 2. DDL策略说明

- **none**: 不执行任何DDL操作（默认）
- **create**: 启动时创建所有表，如果表已存在则报错
- **create-drop**: 启动时创建表，关闭时删除表（主要用于测试）
- **update**: 启动时更新表结构，添加缺失的列和约束
- **validate**: 启动时验证表结构，不修改数据库

## 使用方式

### 1. 自动执行DDL（推荐）

配置`hbm2ddl.auto`后，应用启动时会自动执行相应的DDL操作：

```yaml
jpa.db1:
  properties:
    hibernate:
      hbm2ddl:
        auto: update  # 自动更新表结构
```

### 2. 手动生成DDL脚本

#### 方式一：使用HibernateAdapter

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

#### 方式二：使用SchemaManager

```java
import org.hibernate.solon.integration.schema.SchemaManager;

// 获取Schema管理器
SchemaManager schemaManager = adapter.getSchemaManager();

// 生成DDL到文件
schemaManager.generateDdlToFile("schema.sql", true);

// 生成DDL字符串
String ddl = schemaManager.generateDdlString(true);

// 生成创建表的DDL
String createDdl = schemaManager.generateCreateDdl();

// 生成删除表的DDL
String dropDdl = schemaManager.generateDropDdl();
```

### 3. 手动执行DDL操作

```java
import org.hibernate.solon.integration.schema.SchemaManager;

SchemaManager schemaManager = adapter.getSchemaManager();

// 创建Schema（不删除已存在的表）
schemaManager.createSchema(false);

// 创建Schema（先删除已存在的表）
schemaManager.createSchema(true);

// 更新Schema（添加缺失的列和约束）
schemaManager.updateSchema();

// 删除Schema（删除所有表）
schemaManager.dropSchema();

// 验证Schema
SchemaManager.SchemaValidationResult result = schemaManager.validateSchema();
if (result.isValid()) {
    System.out.println("验证通过: " + result.getMessage());
} else {
    System.out.println("验证失败: " + result.getMessage());
}
```

### 4. 在Controller中使用

```java
import org.hibernate.solon.integration.HibernateAdapter;
import org.hibernate.solon.integration.HibernateAdapterManager;
import org.hibernate.solon.integration.schema.SchemaManager;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;

@Controller
@Mapping("/api/schema")
public class SchemaController {
    
    @Mapping("/generate")
    public String generateDdl(String outputFile) {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            SchemaManager schemaManager = adapter.getSchemaManager();
            schemaManager.generateDdlToFile(outputFile, true);
            return "DDL脚本已生成到: " + outputFile;
        } catch (Exception e) {
            return "生成失败: " + e.getMessage();
        }
    }
    
    @Mapping("/ddl")
    public String getDdl() {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            SchemaManager schemaManager = adapter.getSchemaManager();
            return schemaManager.generateDdlString(true);
        } catch (Exception e) {
            return "生成失败: " + e.getMessage();
        }
    }
    
    @Mapping("/create")
    public String createSchema(boolean drop) {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            SchemaManager schemaManager = adapter.getSchemaManager();
            schemaManager.createSchema(drop);
            return "Schema创建成功";
        } catch (Exception e) {
            return "创建失败: " + e.getMessage();
        }
    }
    
    @Mapping("/update")
    public String updateSchema() {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            SchemaManager schemaManager = adapter.getSchemaManager();
            schemaManager.updateSchema();
            return "Schema更新成功";
        } catch (Exception e) {
            return "更新失败: " + e.getMessage();
        }
    }
}
```

## 完整示例

### 实体类定义

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
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    // Getters and Setters
}
```

### 配置示例

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
        auto: update  # 自动更新表结构
      show_sql: true
      format_sql: true
```

### 生成的DDL示例

```sql
create table user (
    id bigint not null auto_increment,
    name varchar(50) not null,
    age integer not null,
    email varchar(100),
    create_time datetime,
    primary key (id)
) engine=InnoDB;
```

## 注意事项

1. **生产环境建议**
   - 生产环境建议使用`validate`或`none`
   - 不要在生产环境使用`create`或`create-drop`，会丢失数据
   - 使用`update`时要谨慎，确保不会破坏现有数据

2. **开发环境建议**
   - 开发环境可以使用`update`自动更新表结构
   - 测试环境可以使用`create-drop`，每次启动都重新创建表

3. **DDL生成时机**
   - 自动DDL在应用加载完成后执行（AppLoadEndEvent）
   - 确保所有实体类都已注册完成

4. **多数据源支持**
   - 每个数据源可以独立配置DDL策略
   - 通过适配器名称区分不同的数据源

## 高级用法

### 1. 生成特定表的DDL

目前DDL生成器会生成所有已注册实体类的DDL。如果需要生成特定表的DDL，可以：

1. 创建临时的Configuration，只添加需要的实体类
2. 使用该Configuration生成DDL

### 2. 自定义DDL输出格式

```java
DdlGenerator generator = adapter.getDdlGenerator();

// 格式化输出
String formattedDdl = generator.generateDdlString(true);

// 非格式化输出
String unformattedDdl = generator.generateDdlString(false);
```

### 3. 导出DDL到多个文件

```java
// 生成创建表的DDL
String createDdl = schemaManager.generateCreateDdl();
Files.write(Paths.get("create.sql"), createDdl.getBytes());

// 生成删除表的DDL
String dropDdl = schemaManager.generateDropDdl();
Files.write(Paths.get("drop.sql"), dropDdl.getBytes());
```

## 故障排查

### 问题1：DDL生成失败

**原因**: 实体类未正确注册

**解决**: 确保实体类已添加到Configuration中，检查`mappings`配置

### 问题2：自动DDL未执行

**原因**: 配置不正确或事件监听未注册

**解决**: 
1. 检查`hbm2ddl.auto`配置
2. 确保`SchemaAutoExecutor`已注册

### 问题3：表结构未更新

**原因**: `update`策略可能无法处理某些复杂的结构变更

**解决**: 手动执行DDL或使用数据库迁移工具（如Flyway）

## 相关文件

- `DdlGenerator.java` - DDL生成器
- `SchemaManager.java` - Schema管理器
- `SchemaAutoExecutor.java` - 自动DDL执行器
- `SchemaConfiguration.java` - Schema配置类

## 测试示例

参考测试类：
- `DdlGeneratorTest.java` - DDL生成测试
- `SchemaManagerTest.java` - Schema管理测试
- `SchemaController.java` - Schema管理API

