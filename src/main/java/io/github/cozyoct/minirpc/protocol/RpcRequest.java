package io.github.cozyoct.minirpc.protocol;

public record RpcRequest(
        String serviceName,
        String methodName,
        String[] parameterTypeNames,
        Object[] args
) {
}
