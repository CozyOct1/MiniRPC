package io.github.cozyoct.minirpc.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LocalServiceRegistry implements ServiceRegistry {
    private final ConcurrentMap<String, List<ServiceInstance>> services = new ConcurrentHashMap<>();

    @Override
    public void register(ServiceInstance instance) {
        services.compute(instance.serviceName(), (key, current) -> {
            List<ServiceInstance> next = current == null ? new ArrayList<>() : new ArrayList<>(current);
            next.remove(instance);
            next.add(instance.heartbeat());
            return List.copyOf(next);
        });
    }

    @Override
    public void unregister(ServiceInstance instance) {
        services.computeIfPresent(instance.serviceName(), (key, current) ->
                current.stream().filter(item -> !item.equals(instance)).toList());
    }

    @Override
    public void heartbeat(ServiceInstance instance) {
        register(instance);
    }

    @Override
    public List<ServiceInstance> discover(String serviceName) {
        return services.getOrDefault(serviceName, List.of());
    }
}
