# dubbo3-solon-plugin

Solon 与 Apache Dubbo 3 的集成插件。覆盖迁移常用的配置绑定、服务导出、服务引用与进程保活；子配置属性与 Dubbo 官方 Config 字段一致，**不全量抄写**字段手册。

完整字段含义见：[Dubbo 配置参考](https://dubbo.apache.org/zh-cn/overview/mannual/java-sdk/reference-manual/config/)

## 启用

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
|---|---|
| `@DubboService` | 暴露服务（兼容旧 `@Service`） |
| `@DubboReference` | 注入消费代理（兼容旧 `@Reference`） |

仅 `@DubboService` 即可被扫描注册，无需再加 `@Component` / `@Managed`。

---

## 配置总览

### 支持的配置前缀

| 前缀 | 形态 | 说明 |
|---|---|---|
| `dubbo.application` | 单 | 应用信息；缺 `name` 时用 `solon.app.group` + `-` + `solon.app.name` |
| `dubbo.registry` / `dubbo.registries` | 单 / 多 | 注册中心；缺 `address` 时补 `N/A` |
| `dubbo.protocol` / `dubbo.protocols` | 单 / 多 | 协议；缺 `name` 补 `dubbo`，缺 `port` 补 `server.port + 20000` |
| `dubbo.provider` / `dubbo.providers` | 单 / 多 | 服务端默认；map key → `id`，可被 `@DubboService(provider)` 引用 |
| `dubbo.consumer` / `dubbo.consumers` | 单 / 多 | 消费端默认；map key → `id`，可被 `@DubboReference(consumer)` 引用 |
| `dubbo.monitor` | 单 · 可选 | 监控中心 |
| `dubbo.config-center` | 单 · 可选 | 配置中心 |
| `dubbo.metadata-report` | 单 · 可选 | 元数据中心 |
| `dubbo.ssl` | 单 · 可选 | SSL |
| `dubbo.solon.block` | Solon 特有 | 进程保活开关，见下文 |

未在上表中的前缀（如 metrics / module 等）当前**不绑定**。

### 绑定规则

1. **子键 = Dubbo Config 属性**  
   例如 `dubbo.registry.address` → `RegistryConfig.setAddress(...)`，与 Spring Boot 习惯一致。
2. **multi 优先于 single**  
   存在 `dubbo.registries` / `protocols` / `providers` / `consumers` 时，忽略对应 single。
3. **map key → id**  
   非数字 key 且未显式写 `id` 时，key 写入 config `id`（便于注解引用）。
4. **list 索引 key**  
   支持 `registries.0` / `registries[0]`；按索引排序，稳定有序。
5. **缺省补全**  
   仅对 registry（address）、protocol（name/port）、application（name）做插件级默认；provider/consumer 未配置时不造默认实例。

### 常用字段速查（非全量）

完整字段以 Dubbo 官方为准；以下为迁移高频项：

| 配置 | 常用字段 |
|---|---|
| `application` | `name`, `owner`, `logger`, `qos-enable`, `qos-port` |
| `registry` | `address`, `username`, `password`, `group`, `timeout`, `check`, `simplified`, `parameters` |
| `protocol` | `name`, `port`, `host`, `threads`, `serialization`, `transporter` |
| `provider` | `group`, `version`, `timeout`, `retries`, `filter`, `token`, `delay` |
| `consumer` | `group`, `version`, `timeout`, `check`, `retries`, `filter`, `scope`, `loadbalance` |
| `monitor` | `address`, `protocol` |
| `config-center` | `address`, `protocol`, `namespace`, `group` |
| `metadata-report` | `address`, `protocol`, `group` |
| `ssl` | `server-key-cert-chain-path`, `server-private-key-path`, `client-key-cert-chain-path`, `client-private-key-path`, `client-trust-cert-collection-path` |

YAML 里推荐 **kebab-case**（如 `qos-enable`）；绑定到 Java 属性时与 Dubbo Config 一致。

---

## 配置示例

### 单配置（常用）

```yaml
solon.app:
  group: demo
  name: demoapp

dubbo:
  application:
    name: hello-provider
    owner: noear
  registry:
    address: zookeeper://127.0.0.1:2181
  protocol:
    name: dubbo
    port: 20880
  consumer:
    check: false
    timeout: 3000
```

本地联调可不连注册中心：

```yaml
dubbo:
  registry:
    address: N/A
  consumer:
    scope: local
    check: false
```

### 多配置 - Map 风格（推荐迁移）

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

Map key 写入 `id` 后，可在注解中引用：

```java
@DubboService(provider = "p1", registry = {"reg1"}, protocol = {"dubbo"})
public class HelloServiceImpl implements HelloService { ... }

@DubboReference(consumer = "c1", registry = {"reg1"}, check = false)
HelloService helloService;
```

| 注解属性 | 结果 |
|---|---|
| `@DubboService(provider="p1")` | `providerIds=p1`；id 不存在则启动失败 |
| `@DubboReference(consumer="c1")` | 查找 `ConsumerConfig(id=c1)` 并关联；不存在则启动失败 |
| `registry={"reg1","reg2"}` | `registryIds=reg1,reg2`；缺失 id fail-fast |
| `@DubboService(protocol={"dubbo","tri"})` | `protocolIds=dubbo,tri`（**仅 Service**；Reference 用字符串 `protocol`） |

### 多配置 - List 风格（兼容）

推荐索引扁平写法（顺序稳定）：

```yaml
dubbo:
  registries.0.address: zookeeper://127.0.0.1:2181
  registries.1.address: nacos://127.0.0.1:8848
  protocols.0.name: dubbo
  protocols.0.port: 20880
  providers.0.group: g1
  consumers.0.check: false
```

YAML 数组（`-`）在部分加载路径下会扁平为 `0/1` key，一般可用；遇兼容问题请改用索引写法。

### 可选扩展配置

```yaml
dubbo:
  monitor:
    address: zookeeper://127.0.0.1:2181
  config-center:
    address: zookeeper://127.0.0.1:2181
  metadata-report:
    address: zookeeper://127.0.0.1:2181
  ssl:
    server-key-cert-chain-path: /path/to/server.pem
    server-private-key-path: /path/to/server.key
    client-trust-cert-collection-path: /path/to/ca.pem
```

### 进程保活

| 场景 | 行为 |
|---|---|
| `dubbo.solon.block=true/false` | 显式开关，优先 |
| `enableHttp(false)` / 无 HTTP | 默认后台线程保活（纯 Provider） |
| HTTP 开启（Solon 默认） | 不额外保活，依赖 HTTP 线程 |

纯 Dubbo Provider 建议：

```java
Solon.start(App.class, args, app -> app.enableHttp(false));
// 或配置：
// dubbo.solon.block: true
```

一次性 Consumer 进程可设 `dubbo.solon.block: false`，避免 main 结束后仍被保活。

---

## 注解

### 基本用法

```java
@DubboService(group = "demo", version = "1.0.0")
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}

public class HelloController {
    @DubboReference(group = "demo", version = "1.0.0", check = false)
    HelloService helloService;
}
```

### parameters / methods

对齐 Spring 转换语义，并额外支持 `${...}` 模板：

```java
@DubboService(
    group = "demo",
    parameters = {"token", "abc", "route=gray"},
    methods = {
        @Method(name = "sayHello", timeout = 1000, retries = 0)
    }
)
public class HelloServiceImpl implements HelloService { ... }

@DubboReference(
    group = "demo",
    parameters = {"tag:gray"},
    methods = {
        @Method(name = "sayHello", timeout = 2000)
    }
)
HelloService helloService;
```

| 能力 | 说明 |
|---|---|
| `parameters` | 支持 `{"a","b"}` / `{"a=b"}` / `{"a:b"}` 混写；奇数不成对会报错；值支持 `${...}` |
| `methods` | 转为 `MethodConfig`（timeout / retries / loadbalance / parameters 等） |
| 构造期安全 | 插件在 `appendAnnotation` 之后单独写入，避免 `toStringMap` 因 `k=v` 形态直接失败 |

### 远程引用建议

启动期创建代理时若强依赖注册中心，可能导致启动失败。建议：

- 配置 `dubbo.consumer.check: false`，或
- 注解 `check = false`，或
- 显式 `url = "dubbo://host:port"` 直连

---

## 行为说明

| 项 | 说明 |
|---|---|
| 服务导出 | 扫描阶段只注册 `ServiceConfig`；统一在 lifecycle 中 `DubboBootstrap.start()` 导出 |
| 接口解析 | 优先 `interfaceName` / `interfaceClass`；否则取实现类**直接**业务接口（不展开父接口；多接口时保留最具体者）；0 个或仍歧义则启动失败 |
| 默认 application.name | `solon.app.group` + `-` + `solon.app.name` |
| 默认 registry | 未配置 address → `N/A` |
| 默认 protocol | 未配置 name → `dubbo`；未配置 port → `server.port + 20000` |
| multi 引用 | 注解 `provider` / `consumer` / `registry[]` / Service `protocol[]` 按 id 关联；缺失 fail-fast |
| Reference 缓存 | 相同 interface / group / version / url / protocol / scope / consumerId / registryIds / filter 等关键参数复用代理 |
| 停机 | `preStop` 时 `bootstrap.stop()` 并清理引用缓存 |

---

## Tracing

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

## Demo 联调（双进程）

测试源码中提供可直接启动的示例：

| 启动类 | 配置文件 |
|---|---|
| `demo.server.DubboProviderApp` | `classpath:demo-server.yml` |
| `demo.client.DubboConsumeApp` | `classpath:demo-client.yml` |

1. 先启动 `DubboProviderApp`  
   - `enableHttp(false)`，插件保活  
   - 导出 `dubbo://127.0.0.1:20880`，group=`demo`
2. 再启动 `DubboConsumeApp`  
   - `@DubboReference(group="${demo.hello.group}", url="${demo.hello.url}")` 直连  
   - 打印结果后退出（`dubbo.solon.block: false`）

单测使用 `app.yml`；同进程 local 联调见 `DubboLocalScopeIT`。

---

## 与 Spring 的差异（简要）

| 项 | 说明 |
|---|---|
| 启用方式 | Spring 多靠 `dubbo.enabled` + 自动配置；本插件靠 `@EnableDubbo` |
| 包扫描 | 无 `scanBasePackages`；依赖 Solon 启动类包扫描 / `beanMake` |
| id 校验 | provider / consumer / registry / protocol 缺失时**启动期 fail-fast** |
| 配置范围 | 对齐迁移主路径；不做 metrics 全家桶、Actuator、module multi 等 |
| parameters | 额外支持 `${...}` 模板 |

按「关键能力对齐、不全量复刻」原则维护；需要完整字段字典时请查阅 Dubbo 官方文档。
