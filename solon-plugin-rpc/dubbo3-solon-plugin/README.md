# dubbo3-solon-plugin

Solon 与 Apache Dubbo 3 的集成插件。

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

服务实现使用 `@DubboService`，消费方使用 `@DubboReference`（兼容旧注解 `@Service` / `@Reference`）。

仅 `@DubboService` 即可被扫描注册，无需再额外加 `@Component` / `@Managed`。

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
    port: 20880   # 缺省 port = server.port + 20000；缺省 name = dubbo
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

### 多配置 - Spring Boot Map 风格（推荐迁移）

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

Map 的 key 会写入 config 的 `id`（若未显式配置 id），便于注解按名称引用：

```java
@DubboService(provider = "p1")
public class HelloServiceImpl implements HelloService { ... }

@DubboReference(consumer = "c1", check = false)
HelloService helloService;
```

单配置 `dubbo.provider` / `dubbo.consumer` 仍可用；若同时存在 multi（`providers`/`consumers`），**优先 multi**。

### 多配置 - List 风格（兼容）

推荐使用索引扁平写法（顺序稳定）：

```yaml
dubbo:
  registries.0.address: zookeeper://127.0.0.1:2181
  registries.1.address: nacos://127.0.0.1:8848
  protocols.0.name: dubbo
  protocols.0.port: 20880
  providers.0.group: g1
  consumers.0.check: false
```

YAML 数组写法（`-`）在部分加载路径下也会扁平化为 `0/1` key，一般可用；若遇兼容问题请改用上面的索引写法。

### 可选关键配置

```yaml
dubbo:
  monitor:
    address: ...
  config-center:
    address: ...
  metadata-report:
    address: ...
  ssl:
    server-key-cert-chain-path: ...
    server-private-key-path: ...
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
// 或
// dubbo.solon.block: true
```

## 行为说明

| 项 | 说明 |
|---|---|
| 服务导出 | 由 `DubboBootstrap.start()` 统一导出；扫描阶段只注册 `ServiceConfig` |
| 接口解析 | 优先 `interfaceName` / `interfaceClass`；否则取实现类层次中**直接**业务接口（不展开父接口；多接口并存时保留最具体者）；仍有 0 个或多接口时启动失败 |
| parameters | 注解 `parameters` 转为 Map，兼容 `{"a","b"}` / `{"a=b"}` / `{"a:b"}` 混写；值支持 `${...}` 模板 |
| methods | 注解 `methods=@Method(...)` 转为 `MethodConfig`（timeout/retries/loadbalance/parameters 等）；字符串字段支持 `${...}` |
| multi providers/consumers | 支持 `dubbo.providers.*` / `dubbo.consumers.*`（及 single 回退）；`@DubboService(provider)` / `@DubboReference(consumer)` 按 id 关联 |
| Reference 缓存 | 相同 interface/group/version/url/protocol/scope/tag/filter 等参数复用代理 |
| 默认注册中心 | 未配置 address 时补 `N/A` |
| 默认协议 | 未配置 name 时补 `dubbo`；未配置 port 时补 `server.port + 20000` |
| 远程引用 | 建议 `consumer.check=false` 或显式 url，避免启动期强依赖注册中心 |

### 注解 parameters / methods 示例

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

## Tracing

已注册 Dubbo Filter SPI：`solonTracing`。

在存在 OpenTracing `Tracer` bean 时，可通过配置启用：

```yaml
dubbo:
  provider:
    filter: solonTracing
  consumer:
    filter: solonTracing
```

## Demo 联调（双进程）

测试源码里提供了可直接启动的示例，配置已拆分：

| 启动类 | 配置文件 |
|---|---|
| `demo.server.DubboProviderApp` | `classpath:demo-server.yml` |
| `demo.client.DubboConsumeApp` | `classpath:demo-client.yml` |

1. 先启动 `demo.server.DubboProviderApp`
   - 加载 `demo-server.yml`
   - `enableHttp(false)`，插件保活，进程不退出
   - 导出 `dubbo://127.0.0.1:20880`，group=`demo`
2. 再启动 `demo.client.DubboConsumeApp`
   - 加载 `demo-client.yml`
   - `@DubboReference(group="${demo.hello.group}", url="${demo.hello.url}")` 直连调用
   - 打印结果后自动退出

单测仍使用 `app.yml`；同进程 local 联调见 `DubboLocalScopeIT`。

