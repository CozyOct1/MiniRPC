package io.github.cozyoct.minirpc.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.github.cozyoct.minirpc.common.RpcException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class KryoSerializer implements Serializer {
    private final ThreadLocal<Kryo> kryo = ThreadLocal.withInitial(() -> {
        Kryo instance = new Kryo();
        instance.setRegistrationRequired(false);
        instance.setReferences(true);
        return instance;
    });

    @Override
    public byte[] serialize(Object value) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             Output output = new Output(outputStream)) {
            kryo.get().writeClassAndObject(output, value);
            output.flush();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RpcException("Kryo serialize failed", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> targetType) {
        try (Input input = new Input(new ByteArrayInputStream(bytes))) {
            Object value = kryo.get().readClassAndObject(input);
            return targetType.cast(value);
        } catch (Exception e) {
            throw new RpcException("Kryo deserialize failed", e);
        }
    }

    @Override
    public SerializerType type() {
        return SerializerType.KRYO;
    }
}
