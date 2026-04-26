package io.github.cozyoct.minirpc.registry;

import io.github.cozyoct.minirpc.common.ServiceKey;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ServiceProvider {
    private final ConcurrentMap<String, Object> services = new ConcurrentHashMap<>();

    public <T> void addService(Class<T> serviceInterface, T implementation) {
        services.put(ServiceKey.of(serviceInterface), implementation);
    }

    public Object getService(String serviceName) {
        return services.get(serviceName);
    }
}
