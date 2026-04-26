package io.github.cozyoct.minirpc.loadbalance;

import io.github.cozyoct.minirpc.common.RpcException;
import io.github.cozyoct.minirpc.registry.ServiceInstance;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer implements LoadBalancer {
    private final ConcurrentMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    @Override
    public ServiceInstance select(String serviceName, List<ServiceInstance> instances, String routingKey) {
        if (instances.isEmpty()) {
            throw new RpcException("No provider found for " + serviceName);
        }
        int index = Math.floorMod(counters.computeIfAbsent(serviceName, key -> new AtomicInteger()).getAndIncrement(), instances.size());
        return instances.get(index);
    }
}
