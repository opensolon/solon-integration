---
title: "dubbo3-solon-plugin — Solon 与 Apache Dubbo 3 集成指南"
---

> 此插件，主要社区贡献人（浅念）

```xml
<dependency>
    <groupId>org.noear</groupId>
    <artifactId>dubbo3-solon-plugin</artifactId>
</dependency>
```

## 1、描述

分布式扩展插件。基于 Apache Dubbo 3 适配的 RPC 集成插件，覆盖**配置绑定、服务导出、服务引用与进程保活**；子配置属性与 Dubbo 官方 Config 字段一致。

完整字段含义见：[Dubbo 配置参考](https://dubbo.apache.org/zh-cn/overview/manual/java-sdk/reference-manual/config/)

---

## 2、快速开始

### 2.1 启用

启动类上必须标注 `@EnableDubbo`：

```java
@EnableDubbo
public class App {
    public static void main(String[] args) {
        Solon.start(App.class, args);
    }
}
```

| 注解 | 用途 |
|------|------|
| `@DubboService` | 暴露服务（兼容旧 `@Service`） |
| `@DubboReference` | 注入消费代理（兼容旧 `@Reference`） |

仅标注 `@DubboService` 即可被扫描注册，**无需额外加 `@Component` / `@Managed`**。

### 2.2 声明接口

```java
public interface HelloService {
    String sayHello(String name);
}
```

### 2.3 提供者（服务端）

**配置**（`app.yml`）：

```yaml
server.port: 8011

dubbo:
  application:
    name: hello-provider
    owner: noear
  registry:
    address: nacos://localhost:8848
  protocol:
    name: dubbo
    port: 20880
```

> 协议 `port` 未配置时默认取 `server.port + 20000`。

**代码**：

```java
@EnableDubbo
public class DubboProviderApp {
    public static void main(String[] args) {
        Solon.start(DubboProviderApp.class, args);
    }
}

@DubboService(group = "hello")
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "hello, " + name;
    }
}
```

### 2.4 消费者（客户端）

```java
@EnableDubbo
public class DubboConsumeApp {
    public static void main(String[] args) {
        Solon.start(DubboConsumeApp.class, args, app -> app.enableHttp(false));

        DubboConsumeApp tmp = Solon.context().getBean(DubboConsumeApp.class);
        System.out.println(tmp.home());
    }

    @DubboReference(group = "hello")
    HelloService helloService;

    public String home() {
        return helloService.sayHello("noear");
    }
}
```

---

## 3、配置总览

### 3.1 支持的配置前缀

| 前缀 | 形态 | 说明 |
|------|------|------|
| `dubbo.application` | 单 | 应用信息；缺 `name` 时用 `solon.app.group` + `-` + `solon.app.name` |
| `dubbo.registry` / `dubbo.registries` | 单 / 多 | 注册中心；缺 `address` 时补齐 `N/A` |
| `dubbo.protocol` / `dubbo.protocols` | 单 / 多 | 协议；缺 `name` 补 `dubbo`，缺 `port` 补 `server.port + 20000` |
| `dubbo.provider` / `dubbo.providers` | 单 / 多 | 服务端默认；map key → `id`，可被 `@DubboService(provider=...)` 引用 |
| `dubbo.consumer` / `dubbo.consumers` | 单 / 多 | 消费端默认；map key → `id`，可被 `@DubboReference(consumer=...)` 引用 |
| `dubbo.monitor` | 单 · 可选 | 监控中心 |
| `dubbo.config-center` | 单 · 可选 | 配置中心 |
| `dubbo.metadata-report` | 单 · 可选 | 元数据中心 |
| `dubbo.solon.block` | Solon 特有 | 进程保活开关 |

> 未在上表中的前缀（如 metrics、module 等）当前**不绑定**。

### 3.2 绑定规则

1. **子键 = Dubbo Config 属性**——例如 `dubbo.registry.address` → `RegistryConfig.setAddress(...)`，与 Spring Boot 习惯一致。
2. **multi 优先于 single**——存在 `dubbo.registries` / `protocols` / `providers` / `consumers` 时，忽略对应单数形式。
3. **map key → id**——非数字 key 且未显式写 `id` 时，key 写入 config 的 `id`（便于注解引用）。
4. **list 索引 key**——支持 `registries.0` / `registries[0]`；按索引排序，稳定有序。
5. **缺省补全**——仅对 registry（address）、protocol（name/port）、application（name）做插件级默认；provider/consumer 未配置时不创建默认实例。

### 3.3 常用字段速查

| 配置 | 常用字段 |
|------|----------|
| `application` | `name`, `owner`, `logger`, `qos-enable`, `qos-port` |
| `registry` | `address`, `username`, `password`, `group`, `timeout`, `check`, `simplified`, `parameters` |
| `protocol` | `name`, `port`, `host`, `threads`, `serialization`, `transporter` |
| `provider` | `group`, `version`, `timeout`, `retries`, `filter`, `token`, `delay` |
| `consumer` | `group`, `version`, `timeout`, `check`, `retries`, `filter`, `scope`, `loadbalance` |
| `monitor` | `address`, `protocol` |
| `config-center` | `address`, `protocol`, `namespace`, `group` |
| `metadata-report` | `address`, `protocol`, `group` |

---

## 4、多配置（Map 风格）

支持为不同服务指定不同的注册中心、协议和提供者/消费者默认值。

### 4.1 配置示例

```yaml
dubbo:
  registries:
    reg1:
      address: zookeeper://127.0.0.1:2181
    reg2:
      address: nacos://127.0.0.1:8848
  protocols:
    dubbo:
      name: dubbo
      port: 20880
    triple:
      name: tri
      port: 50051
  providers:
    p1:
      group: g1
      timeout: 3000
    p2:
      group: g2
      filter: solonTracing
  consumers:
    c1:
      check: false
      timeout: 2000
    c2:
      check: false
      timeout: 5000
```

### 4.2 注解引用

Map key 自动写入 `id`，可在注解中按 id 引用：

```java
@DubboService(
    provider = "p1",
    registry = {"reg1"},
    protocol = {"dubbo"}
)
public class HelloServiceImpl implements HelloService {
    // ...
}

@DubboReference(
    consumer = "c1",
    registry = {"reg1"},
    check = false
)
HelloService helloService;
```

| 注解属性 | 结果 |
|----------|------|
| `@DubboService(provider = "p1")` | 设置 `providerIds = p1`；id 不存在则启动失败 |
| `@DubboReference(consumer = "c1")` | 查找 `ConsumerConfig(id=c1)` 并关联；不存在则启动失败 |
| `registry = {"reg1", "reg2"}` | 设置 `registryIds = reg1,reg2`；缺失 id 直接 fail-fast |
| `@DubboService(protocol = {"dubbo", "tri"})` | 设置 `protocolIds = dubbo,tri`（仅 Service 侧；Reference 用字符串 `protocol`） |

---

## 5、本地调试

### 5.1 不连注册中心（本地模式）

```yaml
dubbo:
  registry:
    address: N/A
  consumer:
    scope: local    # 同进程本地调用
    check: false
```

### 5.2 双进程直连调试（推荐）

服务端配置：

```yaml
# demo-server.yml
solon.app:
  group: demo
  name: dubbo-provider

dubbo:
  application:
    name: dubbo-provider
    logger: slf4j
  registry:
    address: N/A
  protocol:
    name: dubbo
    port: 20880
  provider:
    group: demo
```

```java
@EnableDubbo
public class DubboProviderApp {
    public static void main(String[] args) {
        Solon.start(DubboProviderApp.class, args, app -> {
            app.enableHttp(false);  // 纯 Provider，由插件保活
        });
    }
}

@DubboService(group = "demo")
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "hello, " + name;
    }
}
```

客户端配置：

```yaml
# demo-client.yml
solon.app:
  group: demo
  name: dubbo-consumer

dubbo:
  application:
    name: dubbo-consumer
    logger: slf4j
  registry:
    address: N/A
  consumer:
    check: false
    timeout: 3000
  solon:
    block: false   # 一次性 Consumer，执行完后退出

demo:
  hello:
    group: demo
    url: dubbo://127.0.0.1:20880
```

```java
@EnableDubbo
public class DubboConsumeApp {
    @DubboReference(
        group = "${demo.hello.group}",
        url = "${demo.hello.url}",
        check = false
    )
    HelloService helloService;

    public static void main(String[] args) {
        Solon.start(DubboConsumeApp.class, args, app -> {
            app.enableHttp(false);
        });

        DubboConsumeApp app = Solon.context().getBean(DubboConsumeApp.class);
        String result = app.helloService.sayHello("noear");
        System.out.println("result = " + result);

        Solon.stopBlock(false, 0);  // 执行完毕，退出进程
    }
}
```

> 客户端通过 `${...}` 模板从配置文件中动态读取 group 和 url，无需硬编码。

---

## 6、进程保活

纯 Dubbo Provider 进程（不开 HTTP）需要插件保活，否则 main 线程结束后进程会退出。

| 场景 | 行为 |
|------|------|
| `dubbo.solon.block = true/false` | 显式开关，优先 |
| `enableHttp(false)` / 无 HTTP | 默认后台线程保活（纯 Provider） |
| HTTP 开启（Solon 默认） | 不额外保活，依赖 HTTP 服务器线程 |

建议：

- **纯 Provider**：配置 `app.enableHttp(false)`，插件自动保活
- **一次性 Consumer**：配置 `dubbo.solon.block: false`，避免 main 结束后仍被保活

---

## 7、高级注解特性

### 7.1 `${...}` 配置模板

所有 String 类型的注解属性（如 `group`、`version`、`url`、`token` 等）都支持 `${...}` 模板，从配置文件中取值：

```yaml
# app.yml
demo.service.group: g1
demo.service.version: 1.0.0
```

```java
@DubboService(
    group = "${demo.service.group}",
    version = "${demo.service.version}"
)
public class HelloServiceImpl implements HelloService { ... }
```

### 7.2 parameters（参数）

支持灵活的格式：

```java
@DubboService(
    group = "demo",
    parameters = {"token", "abc", "route=gray"}  // → {token=abc, route=gray}
)
public class HelloServiceImpl implements HelloService { ... }

@DubboReference(
    group = "demo",
    parameters = {"tag:gray"}  // 也支持冒号分隔
)
HelloService helloService;
```

| 写法 | 结果 |
|------|------|
| `{"a","b"}` | `{a=b}` |
| `{"a=b"}` | `{a=b}` |
| `{"a:b"}` | `{a=b}` |
| `{"a=b","c","d"}` | `{a=b, c=d}` |
| `{"a","a:b"}` | `{a=a:b}` |

奇数不成对会报错；值支持 `${...}` 模板。

### 7.3 methods（方法级配置）

```java
@DubboService(
    group = "demo",
    methods = {
        @Method(name = "sayHello", timeout = 1000, retries = 0)
    }
)
public class HelloServiceImpl implements HelloService { ... }

@DubboReference(
    group = "demo",
    methods = {
        @Method(name = "sayHello", timeout = 2000)
    }
)
HelloService helloService;
```

### 7.4 远程引用建议

启动期创建代理时若强依赖注册中心，可能导致启动失败。建议：

- 配置 `dubbo.consumer.check: false`，或
- 注解 `check = false`，或
- 显式 `url = "dubbo://host:port"` 直连

---

## 8、行为说明

| 项 | 说明 |
|----|------|
| 服务导出 | 扫描阶段只注册 `ServiceConfig`；统一在 lifecycle 中 `DubboBootstrap.start()` 真正导出 |
| 接口解析 | 优先 `interfaceName` / `interfaceClass`；否则取实现类直接业务接口（不展开父接口；多接口时保留最具体者）；0 个或仍有歧义则启动失败 |
| 默认 `application.name` | `solon.app.group` + `-` + `solon.app.name` |
| 默认 registry | 未配置 address → `N/A` |
| 默认 protocol | 未配置 name → `dubbo`；未配置 port → `server.port + 20000` |
| multi 引用 | 注解 `provider` / `consumer` / `registry[]` / Service `protocol[]` 按 id 关联；缺失 fail-fast |
| Reference 缓存 | 相同 interface / group / version / url / protocol / scope / consumerId / registryIds / filter 等关键参数复用代理 |
| 停机 | `preStop` 时 `bootstrap.stop()` 并清理引用缓存 |

---

## 9、Tracing（链路追踪）

已注册 Dubbo Filter SPI：`solonTracing`。

存在 OpenTracing `Tracer` bean 时，可通过 filter 启用：

```yaml
dubbo:
  provider:
    filter: solonTracing
  consumer:
    filter: solonTracing
```

也可写在 multi `providers.*` / `consumers.*` 的 `filter` 上。

---

## 10、原生编译（AOT / GraalVM Native Image）

> ⚠️ 当前未完全验证通过，以下为参考配置。

添加 dubbo-native 依赖包（提供 AOT 处理实现）。dubbo-native AOT 编译时还会用到 spring-context。

```xml
<dependency>
    <groupId>org.apache.dubbo</groupId>
    <artifactId>dubbo-native</artifactId>
    <version>${dubbo3.version}</version>
</dependency>

<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>6.2.10</version>
    <scope>provided</scope>
</dependency>
```

添加 dubbo-maven-plugin 构建插件：

```xml
<profiles>
    <profile>
        <id>native-dubbo</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.dubbo</groupId>
                    <artifactId>dubbo-maven-plugin</artifactId>
                    <version>${dubbo3.version}</version>
                    <configuration>
                        <mainClass>com.example.nativedemo.NativeDemoApplication</mainClass>
                    </configuration>
                    <executions>
                        <execution>
                            <phase>process-sources</phase>
                            <goals>
                                <goal>dubbo-process-aot</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

运行命令（细节需参考 Dubbo 官网教程及配套示例）：

```bash
mvn clean native:compile -P native -P native-dubbo -DskipTests
```

---

## 11、代码演示

[https://gitee.com/noear/solon-examples/tree/main/7.Solon-Remoting-Rpc/demo7014-rpc_dubbo3_sml](https://gitee.com/noear/solon-examples/tree/main/7.Solon-Remoting-Rpc/demo7014-rpc_dubbo3_sml)
