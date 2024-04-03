# mybatis-tkmapper-solon-plugin

```xml
<dependency>
    <groupId>org.noear</groupId>
    <artifactId>mybatis-plus-solon-plugin</artifactId>
</dependency>
```
#### 1、描述

mybatis的扩展插件, 提供solon对tkMapper的支持([代码仓库](https://github.com/abel533/Mapper))。

插件依赖 [**mybatis-solon-plugin**](/article/20)

#### 2、强调多数据源支持

* 强调多数据源的配置。例：demo.db1...，demo.db2...
* 强调带 name 的 DataSource Bean
* 强调使用 @Db("name") 的数据源注解

@Db 可注入类型：

| 支持类型              | 说明                                                                  | 
|-------------------|---------------------------------------------------------------------| 
| Mapper.class      | 注入 Mapper。例：`@Db("db1") UserMapper userMapper`                      | 
| Configuration     | 注入 Configuration，一般仅用于配置。例：`@Db("db1") Configuration db1Cfg`        | 
| SqlSessionFactory | 注入 SqlSessionFactory。例：`@Db("db1") SqlSessionFactory db1` （不推荐直接使用） | 
| Config            | 注入 Config。例：`@Db("db1") Config tkConfig`，对特定ktMapper config的设置      | 

#### 3、数据源配置

```yml
# 配置数据源
demo.db1:
  schema: rock
  jdbcUrl: jdbc:mysql://localhost:3306/rock?useUnicode=true&characterEncoding=utf8&autoReconnect=true&rewriteBatchedStatements=true
  driverClassName: com.mysql.cj.jdbc.Driver
  username: root
  password: 123456
  
# 配置数据源对应的 mybatis 信息（要与 DataSource bean 的名字对上）
mybatis.db1:
  typeAliases:    #支持包名 或 类名（大写开头 或 *）//支持 ** 或 * 占位符
    - "demo4021.model"
    - "demo4021.model.*" #这个表达式同上效果
  typeHandlers: #支持包名 或 类名（大写开头 或 *）//支持 ** 或 * 占位符
    - "demo4021.dso.mybaits.handler"
    - "demo4021.dso.mybaits.handler.*" #这个表达式同上效果
  mappers:        #支持包名 或 类名（大写开头 或 *）或 xml（.xml结尾）//支持 ** 或 * 占位符
    - "demo4021.**.mapper"
    - "demo4021.**.mapper.*" #这个表达式同上效果
    - "classpath:demo4021/**/mapper.xml"
    - "classpath:demo4021/**/mapping/*.xml"
  configuration:  #扩展配置（要与 Configuration 类的属性一一对应）
    cacheEnabled: false
    mapperVerifyEnabled: false #如果为 true，则要求所有 mapper 有 @Mapper 主解
    mapUnderscoreToCamelCase: true
  tk:
    mapper:  #tkMapper的配置
        style: camelhumpandlowercase
        safe-update: true
        safe-delete: true


#
#提示：使用 "**" 表达式时，范围要尽量小。不要用 "org.**"、"com.**" 之类的开头，范围太大了，会影响启动速度。
#
```

其中 configuration 配置节对应的实体为：org.apache.ibatis.session.Configuration（相关项，可参考实体属性）
其中 config 配置节对应的实体为：tk.mybatis.mapper.entity.Config（相关项，可参考实体属性）

#### 4、关于 mappers 配置的补说明（必看）

* 思路上，是以数据源为主，去关联对应的 mapper（为了多数据源方便）
* 如果配置了 xml ，则 xml 对应的 mapper 可以不用配置（会自动关联进去）

```
mybatis.db1:
    mappers: 
        - "classpath:demo4021/**/mapper.xml"
```

* 如果没有对应 xml 文件的 mapper，必须配置一下

```
mybatis.db1:
    mappers: 
        - "demo4021.**.mapper.*"
```

#### 5、代码应用

```java
import javax.persistence.Table;

//配置数据源
@Configuration
public class Config {
    //此下的 db1 与 mybatis.db1 将对应在起来 //可以用 @Db("db1") 注入mapper
    //typed=true，表示默认数据源。@Db 可不带名字注入 
    @Bean(value = "db1", typed = true)
    public DataSource db1(@Inject("${demo.db1}") HikariDataSource ds) {
        return ds;
    }
}

// 支持jpa注解 的App实体类
@Table(name="app")
public class App {

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")
    private Long id;

    @Column(name = "`name`")
    private String name;
    
    // getter or setter .....
}

// Mapper一定使用 tk.mybatis.mapper.common.Mapper
public interface AppMapper extends Mapper<App> {
    
    List<User> findByName(@Param("name") String name);
}

//应用
@Component
public class AppService {
    //可用 @Db 或 @Db("db1") 注入
    @Db
    AppMapper appMapper; //xml sql mapper

    public void test() {
        App app = appMapper.selectByPrimaryKey(12);
        List<AppL> apps = appBaseMapper.findByName("测试");
    }
}
```

#### 6、分页查询

* [mybatis-pagehelper-solon-plugin](/article/220)
* [mybatis-sqlhelper-solon-plugin](/article/221)

```java
public void paging() {
    try (Page<User> page = PageHelper.startPage(1, 10)) {
        PageInfo<User> pageInfo = page.doSelectPageInfo(() -> userMapper.selectAll());
        System.out.println("总数:" + pageInfo.getTotal());
        pageInfo.getList().forEach(System.out::println);
    }
}
```
**具体可参考：**

[https://gitee.com/noear/solon-examples/tree/main/4.Solon-Data/demo4102-tkmapper](https://gitee.com/noear/solon-examples/tree/main/4.Solon-Data/demo4102-tkmapper)