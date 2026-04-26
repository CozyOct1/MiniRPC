package io.github.cozyoct.minirpc.serialize;

public enum SerializerType {
    JSON((byte) 1),
    KRYO((byte) 2);

    private final byte code;

    SerializerType(byte code) {
        this.code = code;
    }

    public byte code() {
        return code;
    }

    public static SerializerType fromCode(byte code) {
        for (SerializerType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported serializer type: " + code);
    }
}
