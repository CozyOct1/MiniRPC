package io.github.cozyoct.minirpc.config;

import io.github.cozyoct.minirpc.loadbalance.LoadBalancerType;
import io.github.cozyoct.minirpc.serialize.SerializerType;

import java.time.Duration;

public record RpcClientConfig(
        SerializerType serializerType,
        LoadBalancerType loadBalancerType,
        Duration timeout,
        int retries,
        int circuitFailureThreshold,
        Duration circuitOpenDuration
) {
    public static RpcClientConfig defaults() {
        return new RpcClientConfig(
                SerializerType.JSON,
                LoadBalancerType.ROUND_ROBIN,
                Duration.ofSeconds(3),
                2,
                5,
                Duration.ofSeconds(10)
        );
    }
}
