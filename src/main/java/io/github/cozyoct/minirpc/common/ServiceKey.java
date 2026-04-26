package io.github.cozyoct.minirpc.common;

public final class ServiceKey {
    private ServiceKey() {
    }

    public static String of(Class<?> serviceInterface) {
        return serviceInterface.getName();
    }

    public static String of(String serviceName) {
        return serviceName;
    }
}
