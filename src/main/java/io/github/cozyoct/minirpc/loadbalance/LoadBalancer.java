package io.github.cozyoct.minirpc.loadbalance;

import io.github.cozyoct.minirpc.registry.ServiceInstance;

import java.util.List;

public interface LoadBalancer {
    ServiceInstance select(String serviceName, List<ServiceInstance> instances, String routingKey);
}
