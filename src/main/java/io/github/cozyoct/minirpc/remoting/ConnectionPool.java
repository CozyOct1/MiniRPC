package io.github.cozyoct.minirpc.remoting;

import io.github.cozyoct.minirpc.codec.RpcMessageDecoder;
import io.github.cozyoct.minirpc.codec.RpcMessageEncoder;
import io.github.cozyoct.minirpc.common.RpcException;
import io.github.cozyoct.minirpc.protocol.RpcMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ConnectionPool implements AutoCloseable {
    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    private final ClientResponseHandler responseHandler;
    private final Map<String, Channel> channels = new ConcurrentHashMap<>();

    public ConnectionPool(ClientResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    public Channel get(InetSocketAddress address) {
        String key = address.getHostString() + ":" + address.getPort();
        Channel current = channels.get(key);
        if (current != null && current.isActive()) {
            return current;
        }
        return channels.compute(key, (ignored, oldChannel) -> {
            if (oldChannel != null && oldChannel.isActive()) {
                return oldChannel;
            }
            return connect(address);
        });
    }

    private Channel connect(InetSocketAddress address) {
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new IdleStateHandler(0, 20, 0, TimeUnit.SECONDS))
                                    .addLast(new RpcMessageDecoder())
                                    .addLast(new RpcMessageEncoder())
                                    .addLast(responseHandler);
                        }
                    });
            return bootstrap.connect(address).sync().channel();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RpcException("Connect interrupted: " + address, e);
        } catch (Exception e) {
            throw new RpcException("Connect failed: " + address, e);
        }
    }

    @Override
    public void close() {
        for (Channel channel : channels.values()) {
            channel.close();
        }
        eventLoopGroup.shutdownGracefully();
    }
}
