# MiniRPC 轻量级高并发 RPC 框架

![MiniRPC](minirpc_LOGO.png)

MiniRPC 是一个基于 Java 21 + Netty 实现的轻量级 RPC 框架，覆盖服务注册发现、动态代理调用、自定义二进制协议、序列化扩展、负载均衡、连接复用、心跳检测、超时重试、熔断降级和 Prometheus 指标暴露。项目重点验证分布式服务通信、网络协议设计和高并发调用链路的工程实现能力。

## 技术栈

- Java 21：服务端业务执行器默认使用 Virtual Threads，降低高并发 I/O 场景下的线程维护成本。
- Netty：负责客户端与服务端长连接、编解码 pipeline、心跳和 TCP 通信。
- 自定义协议：包含 Magic Number、Version、Serializer、MessageType、RequestId、BodyLength。
- 序列化：内置 JSON 与 Kryo，可通过 `SerializerFactory.register` 扩展 Hessian、Protobuf 等实现。
- 注册发现：内置本地注册表，保留 Redis、Nacos、Zookeeper 等注册中心的扩展接口。
- 负载均衡：随机、轮询、一致性 Hash。
- 容错治理：超时控制、失败重试、熔断保护、异常兜底。
- 可观测性：Prometheus text format 指标，统计请求量、失败量、重试量、平均耗时和 P95 延迟。

## 项目结构

```text
src/main/java/io/github/cozyoct/minirpc
├── client        # JDK 动态代理与调用链路
├── codec         # 自定义协议编解码
├── config        # 客户端与服务端配置
├── example       # Echo 示例服务
├── loadbalance   # 负载均衡策略
├── metrics       # Prometheus 指标暴露
├── protocol      # RPC 消息、请求、响应模型
├── registry      # 注册发现抽象与本地实现
├── remoting      # Netty 客户端连接池与服务端
├── serialize     # JSON / Kryo 序列化
└── tolerant      # 熔断器
```

## 快速开始

环境要求：

- JDK 21+
- Maven 3.9+

运行测试：

```bash
mvn test
```

启动示例：

```bash
mvn -q -DskipTests package
java -jar target/minirpc-1.0.0.jar
```

示例会在本地启动 RPC Server `127.0.0.1:9000`，并暴露 Prometheus 指标：

```text
http://127.0.0.1:9100/metrics
```

## 核心用法

```java
LocalServiceRegistry registry = new LocalServiceRegistry();
RpcServer server = new RpcServer(RpcServerConfig.defaults(9000), registry, new ServiceProvider());
server.publish(EchoService.class, new EchoServiceImpl());
server.start();

try (RpcClient client = new RpcClient(registry)) {
    EchoService echoService = client.create(EchoService.class);
    String result = echoService.echo("hello");
}
```

## 协议设计

MiniRPC 使用固定 22 字节协议头解决 TCP 粘包拆包问题：

| 字段 | 长度 | 说明 |
| --- | ---: | --- |
| Magic Number | 4B | 固定 `0x4D525043`，快速识别 MiniRPC 报文 |
| Version | 1B | 协议版本 |
| Serializer | 1B | JSON、Kryo 等序列化类型 |
| MessageType | 1B | Request、Response、Heartbeat |
| Reserved | 1B | 预留扩展位 |
| RequestId | 8B | 请求唯一 ID，用于异步响应匹配 |
| BodyLength | 4B | 消息体长度 |
| Reserved | 2B | 预留扩展位 |
| Body | N | 序列化后的请求或响应 |

## 简历写法

### MiniRPC 轻量级高并发 RPC 框架

基于 Java + Netty 实现轻量级 RPC 框架，支持服务注册发现、动态代理调用、自定义二进制协议、序列化扩展、负载均衡、连接复用、心跳检测、超时重试、熔断降级和 Prometheus 指标暴露，重点验证分布式服务通信与高并发调用链路的工程能力。

- 基于 JDK 动态代理封装远程调用流程，将接口方法调用转换为 RPC 请求，实现服务消费者像调用本地方法一样调用远程服务。
- 基于 Netty 实现客户端与服务端通信链路，设计自定义二进制协议，包含魔数、版本号、序列化类型、请求 ID、消息类型和消息体长度，解决 TCP 粘包拆包问题。
- 设计服务注册与发现模块，支持本地注册表和 Redis / Nacos 扩展，实现服务实例上线、下线、心跳续约和客户端缓存刷新。
- 实现轮询、随机和一致性 Hash 负载均衡策略，并支持超时控制、失败重试、熔断降级和异常兜底，提高远程调用稳定性。
- 接入 Prometheus 指标暴露，统计接口级 QPS、平均耗时、P95 延迟、失败率和重试次数，通过压测对比连接复用、序列化方式和线程模型对吞吐的影响。

## GitHub Pages

静态介绍页位于 `docs/`，仓库 Pages 可选择 `main` 分支的 `/docs` 目录作为发布源。
