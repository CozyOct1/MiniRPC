package io.github.cozyoct.minirpc.example;

public interface EchoService {
    String echo(String message);

    int add(int left, int right);
}
