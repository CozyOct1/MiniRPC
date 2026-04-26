package io.github.cozyoct.minirpc.remoting;

import io.github.cozyoct.minirpc.protocol.MessageType;
import io.github.cozyoct.minirpc.protocol.RpcMessage;
import io.github.cozyoct.minirpc.protocol.RpcRequest;
import io.github.cozyoct.minirpc.protocol.RpcResponse;
import io.github.cozyoct.minirpc.registry.ServiceProvider;
import io.github.cozyoct.minirpc.serialize.Serializer;
import io.github.cozyoct.minirpc.serialize.SerializerFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

public class ServerRequestHandler extends SimpleChannelInboundHandler<RpcMessage> {
    private final ServiceProvider serviceProvider;
    private final ExecutorService businessExecutor;

    public ServerRequestHandler(ServiceProvider serviceProvider, ExecutorService businessExecutor) {
        this.serviceProvider = serviceProvider;
        this.businessExecutor = businessExecutor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage message) {
        if (message.messageType() == MessageType.HEARTBEAT) {
            ctx.writeAndFlush(RpcMessage.heartbeat(message.serializerType(), message.requestId()));
            return;
        }
        businessExecutor.submit(() -> handle(ctx, message));
    }

    private void handle(ChannelHandlerContext ctx, RpcMessage message) {
        Serializer serializer = SerializerFactory.get(message.serializerType());
        RpcResponse response;
        try {
            RpcRequest request = serializer.deserialize(message.body(), RpcRequest.class);
            Object service = serviceProvider.getService(request.serviceName());
            if (service == null) {
                throw new IllegalStateException("Service not found: " + request.serviceName());
            }
            Method method = service.getClass().getMethod(request.methodName(), parameterTypes(request.parameterTypeNames()));
            Object result = method.invoke(service, request.args());
            response = RpcResponse.ok(result);
        } catch (Exception e) {
            response = RpcResponse.fail(e.getMessage());
        }
        byte[] body = serializer.serialize(response);
        ctx.writeAndFlush(RpcMessage.response(message.serializerType(), message.requestId(), body));
    }

    private Class<?>[] parameterTypes(String[] typeNames) throws ClassNotFoundException {
        Class<?>[] types = new Class<?>[typeNames.length];
        for (int i = 0; i < typeNames.length; i++) {
            types[i] = switch (typeNames[i]) {
                case "boolean" -> boolean.class;
                case "byte" -> byte.class;
                case "short" -> short.class;
                case "int" -> int.class;
                case "long" -> long.class;
                case "float" -> float.class;
                case "double" -> double.class;
                case "char" -> char.class;
                default -> Class.forName(typeNames[i]);
            };
        }
        return types;
    }
}
