# MiniRPC 架构说明

MiniRPC 当前采用单 Maven 模块、按职责拆包的结构。核心代码统一位于 `src/main/java/io/github/cozyoct/minirpc`，避免示例、协议、网络、注册发现和治理逻辑混在同一包中。

## 分层

| 层级 | 包路径 | 职责 |
| --- | --- | --- |
| API 调用层 | `client` | 动态代理、请求封装、超时重试、熔断和指标记录 |
| 协议层 | `protocol`、`codec` | RPC 消息模型、自定义协议头、Netty 编解码 |
| 传输层 | `remoting` | Netty 客户端连接池、服务端启动、请求处理器 |
| 注册发现层 | `registry` | 服务实例注册、注销、心跳和发现 |
| 路由层 | `loadbalance` | 随机、轮询、一致性 Hash 负载均衡 |
| 序列化层 | `serialize` | JSON、Kryo 以及后续 Hessian / Protobuf 扩展 |
| 治理层 | `tolerant`、`metrics` | 熔断保护和 Prometheus 指标 |
| 示例层 | `example` | 本地 EchoService 示例 |

## 调用流程

```text
Interface method
  -> JDK Proxy
  -> RpcRequest
  -> Registry discover
  -> LoadBalancer select
  -> ConnectionPool Channel
  -> RpcMessageEncoder
  -> Netty TCP
  -> RpcMessageDecoder
  -> ServerRequestHandler
  -> Virtual Thread
  -> Service implementation
```

## 扩展点

- 新增序列化：实现 `Serializer`，再通过 `SerializerFactory.register` 注册。
- 新增注册中心：实现 `ServiceRegistry`，替换客户端和服务端构造参数。
- 新增负载均衡：实现 `LoadBalancer`，并在 `LoadBalancerFactory` 中注册枚举映射。
- 新增治理能力：在 `RpcClient#invoke` 的调用链路中扩展限流、隔离或降级策略。
