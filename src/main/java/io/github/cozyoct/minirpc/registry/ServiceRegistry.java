package io.github.cozyoct.minirpc.registry;

import java.util.List;

public interface ServiceRegistry {
    void register(ServiceInstance instance);

    void unregister(ServiceInstance instance);

    void heartbeat(ServiceInstance instance);

    List<ServiceInstance> discover(String serviceName);
}
