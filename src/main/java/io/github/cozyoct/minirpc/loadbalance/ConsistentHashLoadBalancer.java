package io.github.cozyoct.minirpc.loadbalance;

import io.github.cozyoct.minirpc.common.RpcException;
import io.github.cozyoct.minirpc.registry.ServiceInstance;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashLoadBalancer implements LoadBalancer {
    private static final int VIRTUAL_NODES = 64;

    @Override
    public ServiceInstance select(String serviceName, List<ServiceInstance> instances, String routingKey) {
        if (instances.isEmpty()) {
            throw new RpcException("No provider found for " + serviceName);
        }
        SortedMap<Long, ServiceInstance> ring = new TreeMap<>();
        for (ServiceInstance instance : instances) {
            String node = instance.host() + ":" + instance.port();
            for (int i = 0; i < VIRTUAL_NODES; i++) {
                ring.put(hash(node + "#" + i), instance);
            }
        }
        long keyHash = hash(routingKey == null ? serviceName : routingKey);
        SortedMap<Long, ServiceInstance> tail = ring.tailMap(keyHash);
        return tail.isEmpty() ? ring.get(ring.firstKey()) : tail.get(tail.firstKey());
    }

    private long hash(String value) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(value.getBytes(StandardCharsets.UTF_8));
            return ((long) (digest[0] & 0xff) << 24)
                    | ((long) (digest[1] & 0xff) << 16)
                    | ((long) (digest[2] & 0xff) << 8)
                    | (digest[3] & 0xff);
        } catch (Exception e) {
            return value.hashCode();
        }
    }
}
