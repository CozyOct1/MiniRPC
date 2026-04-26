package io.github.cozyoct.minirpc.codec;

import io.github.cozyoct.minirpc.protocol.RpcMessage;
import io.github.cozyoct.minirpc.protocol.RpcProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage message, ByteBuf out) {
        byte[] body = message.body() == null ? new byte[0] : message.body();
        out.writeInt(RpcProtocol.MAGIC);
        out.writeByte(message.version());
        out.writeByte(message.serializerType().code());
        out.writeByte(message.messageType().code());
        out.writeByte(0);
        out.writeLong(message.requestId());
        out.writeInt(body.length);
        out.writeShort(0);
        out.writeBytes(body);
    }
}
