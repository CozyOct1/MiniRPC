package io.github.cozyoct.minirpc.remoting;

import io.github.cozyoct.minirpc.protocol.RpcMessage;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Sharable
public class ClientResponseHandler extends SimpleChannelInboundHandler<RpcMessage> {
    private final Map<Long, CompletableFuture<RpcMessage>> pending = new ConcurrentHashMap<>();

    public CompletableFuture<RpcMessage> register(long requestId) {
        CompletableFuture<RpcMessage> future = new CompletableFuture<>();
        pending.put(requestId, future);
        return future;
    }

    public void remove(long requestId) {
        pending.remove(requestId);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage message) {
        CompletableFuture<RpcMessage> future = pending.remove(message.requestId());
        if (future != null) {
            future.complete(message);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.writeAndFlush(RpcMessage.heartbeat(io.github.cozyoct.minirpc.serialize.SerializerType.JSON, System.nanoTime()));
            return;
        }
        super.userEventTriggered(ctx, evt);
    }
}
