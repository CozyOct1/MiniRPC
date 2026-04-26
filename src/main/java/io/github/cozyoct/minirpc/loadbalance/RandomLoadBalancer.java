package io.github.cozyoct.minirpc.loadbalance;

import io.github.cozyoct.minirpc.common.RpcException;
import io.github.cozyoct.minirpc.registry.ServiceInstance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomLoadBalancer implements LoadBalancer {
    @Override
    public ServiceInstance select(String serviceName, List<ServiceInstance> instances, String routingKey) {
        if (instances.isEmpty()) {
            throw new RpcException("No provider found for " + serviceName);
        }
        return instances.get(ThreadLocalRandom.current().nextInt(instances.size()));
    }
}
