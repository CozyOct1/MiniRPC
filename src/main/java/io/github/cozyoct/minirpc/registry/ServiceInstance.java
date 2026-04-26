package io.github.cozyoct.minirpc.registry;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record ServiceInstance(
        String serviceName,
        String host,
        int port,
        int weight,
        Instant lastHeartbeat,
        Map<String, String> metadata
) {
    public InetSocketAddress address() {
        return new InetSocketAddress(host, port);
    }

    public ServiceInstance heartbeat() {
        return new ServiceInstance(serviceName, host, port, weight, Instant.now(), metadata);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServiceInstance that)) {
            return false;
        }
        return port == that.port
                && Objects.equals(serviceName, that.serviceName)
                && Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, host, port);
    }
}
