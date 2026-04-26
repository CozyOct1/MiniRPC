package io.github.cozyoct.minirpc.remoting;

import io.github.cozyoct.minirpc.codec.RpcMessageDecoder;
import io.github.cozyoct.minirpc.codec.RpcMessageEncoder;
import io.github.cozyoct.minirpc.config.RpcServerConfig;
import io.github.cozyoct.minirpc.registry.ServiceInstance;
import io.github.cozyoct.minirpc.registry.ServiceProvider;
import io.github.cozyoct.minirpc.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RpcServer implements AutoCloseable {
    private final RpcServerConfig config;
    private final ServiceRegistry registry;
    private final ServiceProvider serviceProvider;
    private final ExecutorService businessExecutor;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;

    public RpcServer(RpcServerConfig config, ServiceRegistry registry, ServiceProvider serviceProvider) {
        this.config = config;
        this.registry = registry;
        this.serviceProvider = serviceProvider;
        this.businessExecutor = config.virtualThreads()
                ? Executors.newVirtualThreadPerTaskExecutor()
                : Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    public ChannelFuture start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new RpcMessageDecoder())
                                .addLast(new RpcMessageEncoder())
                                .addLast(new ServerRequestHandler(serviceProvider, businessExecutor));
                    }
                });
        ChannelFuture future = bootstrap.bind(config.host(), config.port()).sync();
        channel = future.channel();
        return future;
    }

    public <T> void publish(Class<T> serviceInterface, T implementation) {
        serviceProvider.addService(serviceInterface, implementation);
        registry.register(new ServiceInstance(
                serviceInterface.getName(),
                config.host(),
                config.port(),
                100,
                Instant.now(),
                Map.of("serializer", config.serializerType().name(), "virtualThreads", String.valueOf(config.virtualThreads()))));
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        businessExecutor.close();
    }
}
