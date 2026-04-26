package io.github.cozyoct.minirpc.config;

import io.github.cozyoct.minirpc.serialize.SerializerType;

public record RpcServerConfig(String host, int port, SerializerType serializerType, boolean virtualThreads) {
    public static RpcServerConfig defaults(int port) {
        return new RpcServerConfig("127.0.0.1", port, SerializerType.JSON, true);
    }
}
