# MiniRPC Examples

当前示例代码位于：

```text
src/main/java/io/github/cozyoct/minirpc/example
```

运行方式：

```bash
mvn -q -DskipTests package
java -jar target/minirpc-1.0.0.jar
```

示例启动本地 RPC Server，发布 `EchoService`，再通过 `RpcClient` 动态代理完成一次远程调用。
