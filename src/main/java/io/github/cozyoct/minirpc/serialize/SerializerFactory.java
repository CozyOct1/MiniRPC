package io.github.cozyoct.minirpc.serialize;

import java.util.EnumMap;
import java.util.Map;

public final class SerializerFactory {
    private static final Map<SerializerType, Serializer> SERIALIZERS = new EnumMap<>(SerializerType.class);

    static {
        register(new JsonSerializer());
        register(new KryoSerializer());
    }

    private SerializerFactory() {
    }

    public static Serializer get(SerializerType type) {
        Serializer serializer = SERIALIZERS.get(type);
        if (serializer == null) {
            throw new IllegalArgumentException("Serializer not registered: " + type);
        }
        return serializer;
    }

    public static void register(Serializer serializer) {
        SERIALIZERS.put(serializer.type(), serializer);
    }
}
