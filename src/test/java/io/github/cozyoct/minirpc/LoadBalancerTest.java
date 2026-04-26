package io.github.cozyoct.minirpc;

import io.github.cozyoct.minirpc.loadbalance.RoundRobinLoadBalancer;
import io.github.cozyoct.minirpc.registry.ServiceInstance;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoadBalancerTest {
    @Test
    void roundRobinSelectsInstancesInOrder() {
        ServiceInstance first = new ServiceInstance("svc", "127.0.0.1", 9001, 100, Instant.now(), Map.of());
        ServiceInstance second = new ServiceInstance("svc", "127.0.0.1", 9002, 100, Instant.now(), Map.of());
        RoundRobinLoadBalancer loadBalancer = new RoundRobinLoadBalancer();

        assertEquals(first, loadBalancer.select("svc", List.of(first, second), "a"));
        assertEquals(second, loadBalancer.select("svc", List.of(first, second), "a"));
        assertEquals(first, loadBalancer.select("svc", List.of(first, second), "a"));
    }
}
