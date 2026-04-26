package io.github.cozyoct.minirpc.serialize;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cozyoct.minirpc.common.RpcException;

public class JsonSerializer implements Serializer {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(Object value) {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (Exception e) {
            throw new RpcException("JSON serialize failed", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> targetType) {
        try {
            return objectMapper.readValue(bytes, targetType);
        } catch (Exception e) {
            throw new RpcException("JSON deserialize failed", e);
        }
    }

    @Override
    public SerializerType type() {
        return SerializerType.JSON;
    }
}
