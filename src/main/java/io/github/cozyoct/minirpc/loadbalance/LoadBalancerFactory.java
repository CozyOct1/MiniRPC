package io.github.cozyoct.minirpc.loadbalance;

public final class LoadBalancerFactory {
    private LoadBalancerFactory() {
    }

    public static LoadBalancer get(LoadBalancerType type) {
        return switch (type) {
            case RANDOM -> new RandomLoadBalancer();
            case ROUND_ROBIN -> new RoundRobinLoadBalancer();
            case CONSISTENT_HASH -> new ConsistentHashLoadBalancer();
        };
    }
}
