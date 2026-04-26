package io.github.cozyoct.minirpc.codec;

import io.github.cozyoct.minirpc.protocol.MessageType;
import io.github.cozyoct.minirpc.protocol.RpcMessage;
import io.github.cozyoct.minirpc.protocol.RpcProtocol;
import io.github.cozyoct.minirpc.serialize.SerializerType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class RpcMessageDecoder extends ByteToMessageDecoder {
    private static final int MAX_FRAME_LENGTH = 16 * 1024 * 1024;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < RpcProtocol.HEADER_LENGTH) {
            return;
        }
        in.markReaderIndex();
        int magic = in.readInt();
        if (magic != RpcProtocol.MAGIC) {
            ctx.close();
            return;
        }
        byte version = in.readByte();
        SerializerType serializerType = SerializerType.fromCode(in.readByte());
        MessageType messageType = MessageType.fromCode(in.readByte());
        in.readByte();
        long requestId = in.readLong();
        int bodyLength = in.readInt();
        in.readShort();
        if (bodyLength < 0 || bodyLength > MAX_FRAME_LENGTH) {
            ctx.close();
            return;
        }
        if (in.readableBytes() < bodyLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] body = new byte[bodyLength];
        in.readBytes(body);
        out.add(new RpcMessage(version, serializerType, messageType, requestId, body));
    }
}
