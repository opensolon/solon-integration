# 数据库兼容性说明

## ✅ 支持所有Hibernate兼容的数据库

本插件基于Hibernate框架，**支持所有Hibernate支持的数据库**，通过配置`dialect`（数据库方言）来适配不同的数据库。

## 支持的数据库列表

### 关系型数据库

| 数据库 | Dialect类 | 配置示例 |
|--------|-----------|----------|
| **MySQL** | `org.hibernate.dialect.MySQL8Dialect` | ✅ 已测试 |
| **MySQL 5.7** | `org.hibernate.dialect.MySQL57Dialect` | ✅ 支持 |
| **MariaDB** | `org.hibernate.dialect.MariaDBDialect` | ✅ 支持 |
| **PostgreSQL** | `org.hibernate.dialect.PostgreSQLDialect` | ✅ 支持 |
| **Oracle** | `org.hibernate.dialect.OracleDialect` | ✅ 支持 |
| **SQL Server** | `org.hibernate.dialect.SQLServerDialect` | ✅ 支持 |
| **H2** | `org.hibernate.dialect.H2Dialect` | ✅ 支持 |
| **HSQLDB** | `org.hibernate.dialect.HSQLDialect` | ✅ 支持 |
| **SQLite** | `org.hibernate.dialect.SQLiteDialect` | ✅ 支持 |
| **DB2** | `org.hibernate.dialect.DB2Dialect` | ✅ 支持 |
| **Informix** | `org.hibernate.dialect.InformixDialect` | ✅ 支持 |
| **Sybase** | `org.hibernate.dialect.SybaseDialect` | ✅ 支持 |

### 配置方式

只需在配置文件中指定对应的`dialect`即可：

```yaml
jpa.db1:
  properties:
    hibernate:
      dialect: org.hibernate.dialect.MySQL8Dialect  # MySQL 8
      # dialect: org.hibernate.dialect.PostgreSQLDialect  # PostgreSQL
      # dialect: org.hibernate.dialect.OracleDialect  # Oracle
      # dialect: org.hibernate.dialect.SQLServerDialect  # SQL Server
```

## 工作原理

Hibernate通过**Dialect（方言）**机制来适配不同的数据库：

1. **SQL语法差异**：不同数据库的SQL语法略有不同
2. **数据类型映射**：不同数据库的数据类型不同
3. **DDL生成**：不同数据库的建表语句不同
4. **分页语法**：不同数据库的分页语法不同

Hibernate的Dialect类会自动处理这些差异，生成对应数据库的SQL语句。

## 配置示例

### MySQL配置

```yaml
test.db1:
  jdbcUrl: jdbc:mysql://localhost:3306/test
  driverClassName: com.mysql.cj.jdbc.Driver
  username: root
  password: root

jpa.db1:
  properties:
    hibernate:
      dialect: org.hibernate.dialect.MySQL8Dialect
      hbm2ddl:
        auto: update
```

### PostgreSQL配置

```yaml
test.db1:
  jdbcUrl: jdbc:postgresql://localhost:5432/test
  driverClassName: org.postgresql.Driver
  username: postgres
  password: postgres

jpa.db1:
  properties:
    hibernate:
      dialect: org.hibernate.dialect.PostgreSQLDialect
      hbm2ddl:
        auto: update
```

### Oracle配置

```yaml
test.db1:
  jdbcUrl: jdbc:oracle:thin:@localhost:1521:xe
  driverClassName: oracle.jdbc.OracleDriver
  username: system
  password: oracle

jpa.db1:
  properties:
    hibernate:
      dialect: org.hibernate.dialect.OracleDialect
      hbm2ddl:
        auto: update
```

### SQL Server配置

```yaml
test.db1:
  jdbcUrl: jdbc:sqlserver://localhost:1433;databaseName=test
  driverClassName: com.microsoft.sqlserver.jdbc.SQLServerDriver
  username: sa
  password: password

jpa.db1:
  properties:
    hibernate:
      dialect: org.hibernate.dialect.SQLServerDialect
      hbm2ddl:
        auto: update
```

## 注意事项

### 1. 数据库驱动依赖

使用不同数据库时，需要添加对应的JDBC驱动依赖：

**MySQL:**
```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
```

**PostgreSQL:**
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
```

**Oracle:**
```xml
<dependency>
    <groupId>com.oracle.database.jdbc</groupId>
    <artifactId>ojdbc8</artifactId>
</dependency>
```

### 2. DDL策略差异

不同数据库对DDL的支持略有差异：

- **MySQL**: 完全支持所有DDL策略
- **PostgreSQL**: 完全支持所有DDL策略
- **Oracle**: 支持，但某些特性可能需要调整
- **SQL Server**: 支持，但某些特性可能需要调整

### 3. 数据类型映射

不同数据库的数据类型映射：

| Java类型 | MySQL | PostgreSQL | Oracle | SQL Server |
|----------|-------|------------|--------|------------|
| String | VARCHAR | VARCHAR | VARCHAR2 | NVARCHAR |
| Long | BIGINT | BIGINT | NUMBER | BIGINT |
| Integer | INT | INTEGER | NUMBER | INT |
| BigDecimal | DECIMAL | DECIMAL | NUMBER | DECIMAL |
| LocalDateTime | DATETIME | TIMESTAMP | TIMESTAMP | DATETIME2 |

Hibernate会自动处理这些映射，无需手动配置。

## 多数据源支持

每个数据源可以配置不同的数据库：

```yaml
# MySQL数据源
test.db1:
  jdbcUrl: jdbc:mysql://localhost:3306/test1
  driverClassName: com.mysql.cj.jdbc.Driver

jpa.db1:
  properties:
    hibernate:
      dialect: org.hibernate.dialect.MySQL8Dialect

# PostgreSQL数据源
test.db2:
  jdbcUrl: jdbc:postgresql://localhost:5432/test2
  driverClassName: org.postgresql.Driver

jpa.db2:
  properties:
    hibernate:
      dialect: org.hibernate.dialect.PostgreSQLDialect
```

## 总结

✅ **本插件完全支持所有Hibernate兼容的数据库**

- 通过配置`dialect`即可切换数据库
- 无需修改代码
- 自动处理SQL语法差异
- 自动处理数据类型映射
- 支持多数据源，每个数据源可以使用不同的数据库

