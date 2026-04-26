package io.github.cozyoct.minirpc.example;

import io.github.cozyoct.minirpc.client.RpcClient;
import io.github.cozyoct.minirpc.config.RpcServerConfig;
import io.github.cozyoct.minirpc.metrics.RpcMetrics;
import io.github.cozyoct.minirpc.registry.LocalServiceRegistry;
import io.github.cozyoct.minirpc.registry.ServiceProvider;
import io.github.cozyoct.minirpc.remoting.RpcServer;

public class DemoApplication {
    public static void main(String[] args) throws Exception {
        LocalServiceRegistry registry = new LocalServiceRegistry();
        RpcServer server = new RpcServer(RpcServerConfig.defaults(9000), registry, new ServiceProvider());
        server.publish(EchoService.class, new EchoServiceImpl());
        server.start();

        RpcMetrics metrics = new RpcMetrics();
        metrics.startHttpServer(9100);
        try (RpcClient client = new RpcClient(registry, io.github.cozyoct.minirpc.config.RpcClientConfig.defaults(), metrics)) {
            EchoService echoService = client.create(EchoService.class);
            System.out.println(echoService.echo("hello"));
            System.out.println("1 + 2 = " + echoService.add(1, 2));
            System.out.println("Prometheus metrics: http://127.0.0.1:9100/metrics");
        }
    }
}
