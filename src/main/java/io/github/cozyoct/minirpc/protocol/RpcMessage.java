package io.github.cozyoct.minirpc.protocol;

import io.github.cozyoct.minirpc.serialize.SerializerType;

public record RpcMessage(
        byte version,
        SerializerType serializerType,
        MessageType messageType,
        long requestId,
        byte[] body
) {
    public static RpcMessage request(SerializerType serializerType, long requestId, byte[] body) {
        return new RpcMessage(RpcProtocol.VERSION, serializerType, MessageType.REQUEST, requestId, body);
    }

    public static RpcMessage response(SerializerType serializerType, long requestId, byte[] body) {
        return new RpcMessage(RpcProtocol.VERSION, serializerType, MessageType.RESPONSE, requestId, body);
    }

    public static RpcMessage heartbeat(SerializerType serializerType, long requestId) {
        return new RpcMessage(RpcProtocol.VERSION, serializerType, MessageType.HEARTBEAT, requestId, new byte[0]);
    }
}
