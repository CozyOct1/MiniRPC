package io.github.cozyoct.minirpc;

import io.github.cozyoct.minirpc.protocol.RpcRequest;
import io.github.cozyoct.minirpc.serialize.JsonSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProtocolCodecTest {
    @Test
    void jsonSerializerRoundTripsRequest() {
        JsonSerializer serializer = new JsonSerializer();
        RpcRequest request = new RpcRequest("svc", "echo", new String[]{"java.lang.String"}, new Object[]{"hello"});

        RpcRequest decoded = serializer.deserialize(serializer.serialize(request), RpcRequest.class);

        assertEquals("svc", decoded.serviceName());
        assertEquals("echo", decoded.methodName());
        assertEquals("hello", decoded.args()[0]);
    }
}
