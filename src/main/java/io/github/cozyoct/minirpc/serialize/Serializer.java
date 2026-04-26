package io.github.cozyoct.minirpc.serialize;

public interface Serializer {
    byte[] serialize(Object value);

    <T> T deserialize(byte[] bytes, Class<T> targetType);

    SerializerType type();
}
