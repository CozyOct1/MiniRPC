package io.github.cozyoct.minirpc.protocol;

public final class RpcProtocol {
    public static final int MAGIC = 0x4D525043;
    public static final byte VERSION = 1;
    public static final int HEADER_LENGTH = 22;

    private RpcProtocol() {
    }
}
