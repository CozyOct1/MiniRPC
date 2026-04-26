package io.github.cozyoct.minirpc.client;

import io.github.cozyoct.minirpc.common.RpcException;
import io.github.cozyoct.minirpc.config.RpcClientConfig;
import io.github.cozyoct.minirpc.loadbalance.LoadBalancer;
import io.github.cozyoct.minirpc.loadbalance.LoadBalancerFactory;
import io.github.cozyoct.minirpc.metrics.RpcMetrics;
import io.github.cozyoct.minirpc.protocol.RpcMessage;
import io.github.cozyoct.minirpc.protocol.RpcRequest;
import io.github.cozyoct.minirpc.protocol.RpcResponse;
import io.github.cozyoct.minirpc.registry.ServiceInstance;
import io.github.cozyoct.minirpc.registry.ServiceRegistry;
import io.github.cozyoct.minirpc.remoting.ClientResponseHandler;
import io.github.cozyoct.minirpc.remoting.ConnectionPool;
import io.github.cozyoct.minirpc.serialize.Serializer;
import io.github.cozyoct.minirpc.serialize.SerializerFactory;
import io.github.cozyoct.minirpc.tolerant.CircuitBreaker;
import io.netty.channel.Channel;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class RpcClient implements AutoCloseable {
    private final RpcClientConfig config;
    private final ServiceRegistry registry;
    private final Serializer serializer;
    private final LoadBalancer loadBalancer;
    private final RpcMetrics metrics;
    private final ClientResponseHandler responseHandler = new ClientResponseHandler();
    private final ConnectionPool connectionPool = new ConnectionPool(responseHandler);
    private final AtomicLong requestIds = new AtomicLong(1);
    private final Map<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();

    public RpcClient(ServiceRegistry registry) {
        this(registry, RpcClientConfig.defaults(), new RpcMetrics());
    }

    public RpcClient(ServiceRegistry registry, RpcClientConfig config, RpcMetrics metrics) {
        this.registry = registry;
        this.config = config;
        this.serializer = SerializerFactory.get(config.serializerType());
        this.loadBalancer = LoadBalancerFactory.get(config.loadBalancerType());
        this.metrics = metrics;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> serviceInterface) {
        Object proxy = Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class<?>[]{serviceInterface},
                (ignored, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return method.invoke(this, args);
                    }
                    RpcRequest request = new RpcRequest(
                            serviceInterface.getName(),
                            method.getName(),
                            parameterTypeNames(method.getParameterTypes()),
                            args == null ? new Object[0] : args
                    );
                    return invoke(request);
                });
        return (T) proxy;
    }

    public Object invoke(RpcRequest request) {
        String breakerKey = request.serviceName() + "#" + request.methodName();
        CircuitBreaker circuitBreaker = circuitBreakers.computeIfAbsent(
                breakerKey,
                ignored -> new CircuitBreaker(config.circuitFailureThreshold(), config.circuitOpenDuration()));
        if (!circuitBreaker.allowRequest()) {
            throw new RpcException("Circuit breaker is open for " + breakerKey);
        }
        long start = System.nanoTime();
        int retryCount = 0;
        boolean success = false;
        try {
            for (int attempt = 0; attempt <= config.retries(); attempt++) {
                try {
                    if (attempt > 0) {
                        retryCount++;
                    }
                    Object result = doInvoke(request);
                    circuitBreaker.recordSuccess();
                    success = true;
                    return result;
                } catch (Exception e) {
                    if (attempt >= config.retries()) {
                        circuitBreaker.recordFailure();
                        throw e;
                    }
                }
            }
            throw new RpcException("RPC invoke failed after retries");
        } finally {
            long costMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            metrics.record(request.serviceName(), request.methodName(), costMillis, success, retryCount);
        }
    }

    private Object doInvoke(RpcRequest request) throws Exception {
        List<ServiceInstance> instances = registry.discover(request.serviceName());
        ServiceInstance instance = loadBalancer.select(request.serviceName(), instances, request.methodName());
        long requestId = requestIds.getAndIncrement();
        byte[] body = serializer.serialize(request);
        RpcMessage message = RpcMessage.request(config.serializerType(), requestId, body);
        CompletableFuture<RpcMessage> future = responseHandler.register(requestId);
        try {
            Channel channel = connectionPool.get(instance.address());
            channel.writeAndFlush(message).sync();
            RpcMessage responseMessage = future.get(config.timeout().toMillis(), TimeUnit.MILLISECONDS);
            RpcResponse response = SerializerFactory.get(responseMessage.serializerType())
                    .deserialize(responseMessage.body(), RpcResponse.class);
            if (!response.success()) {
                throw new RpcException(response.errorMessage());
            }
            return response.data();
        } finally {
            responseHandler.remove(requestId);
        }
    }

    public RpcMetrics metrics() {
        return metrics;
    }

    private String[] parameterTypeNames(Class<?>[] parameterTypes) {
        String[] names = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            names[i] = parameterTypes[i].getName();
        }
        return names;
    }

    @Override
    public void close() {
        connectionPool.close();
    }
}
