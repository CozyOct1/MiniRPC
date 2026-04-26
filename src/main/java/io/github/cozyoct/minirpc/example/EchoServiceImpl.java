package io.github.cozyoct.minirpc.example;

public class EchoServiceImpl implements EchoService {
    @Override
    public String echo(String message) {
        return "MiniRPC echo: " + message;
    }

    @Override
    public int add(int left, int right) {
        return left + right;
    }
}
