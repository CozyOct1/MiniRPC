package io.github.cozyoct.minirpc.protocol;

public record RpcResponse(Object data, String errorMessage) {
    public static RpcResponse ok(Object data) {
        return new RpcResponse(data, null);
    }

    public static RpcResponse fail(String errorMessage) {
        return new RpcResponse(null, errorMessage);
    }

    public boolean success() {
        return errorMessage == null || errorMessage.isBlank();
    }
}
