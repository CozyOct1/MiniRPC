package io.github.cozyoct.minirpc.metrics;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.DoubleSummaryStatistics;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class RpcMetrics {
    private final ConcurrentMap<String, AtomicLong> total = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AtomicLong> failures = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AtomicLong> retries = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CopyOnWriteArrayList<Long>> latencies = new ConcurrentHashMap<>();

    public void record(String service, String method, long costMillis, boolean success, int retryCount) {
        String key = key(service, method);
        total.computeIfAbsent(key, ignored -> new AtomicLong()).incrementAndGet();
        latencies.computeIfAbsent(key, ignored -> new CopyOnWriteArrayList<>()).add(costMillis);
        retries.computeIfAbsent(key, ignored -> new AtomicLong()).addAndGet(retryCount);
        if (!success) {
            failures.computeIfAbsent(key, ignored -> new AtomicLong()).incrementAndGet();
        }
    }

    public String scrape() {
        StringBuilder builder = new StringBuilder();
        appendHelp(builder, "minirpc_requests_total", "Total RPC requests.");
        appendHelp(builder, "minirpc_failures_total", "Total RPC failures.");
        appendHelp(builder, "minirpc_retries_total", "Total RPC retries.");
        appendHelp(builder, "minirpc_latency_avg_millis", "Average RPC latency in milliseconds.");
        appendHelp(builder, "minirpc_latency_p95_millis", "P95 RPC latency in milliseconds.");
        for (Map.Entry<String, AtomicLong> entry : total.entrySet()) {
            String label = label(entry.getKey());
            builder.append("minirpc_requests_total").append(label).append(' ').append(entry.getValue().get()).append('\n');
            builder.append("minirpc_failures_total").append(label).append(' ').append(failures.getOrDefault(entry.getKey(), new AtomicLong()).get()).append('\n');
            builder.append("minirpc_retries_total").append(label).append(' ').append(retries.getOrDefault(entry.getKey(), new AtomicLong()).get()).append('\n');
            CopyOnWriteArrayList<Long> values = latencies.getOrDefault(entry.getKey(), new CopyOnWriteArrayList<>());
            DoubleSummaryStatistics stats = values.stream().mapToLong(Long::longValue).summaryStatistics();
            builder.append("minirpc_latency_avg_millis").append(label).append(' ').append(stats.getCount() == 0 ? 0 : stats.getAverage()).append('\n');
            builder.append("minirpc_latency_p95_millis").append(label).append(' ').append(p95(values)).append('\n');
        }
        return builder.toString();
    }

    public HttpServer startHttpServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/metrics", exchange -> {
            byte[] body = scrape().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; version=0.0.4");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(body);
            }
        });
        server.start();
        return server;
    }

    private void appendHelp(StringBuilder builder, String name, String help) {
        builder.append("# HELP ").append(name).append(' ').append(help).append('\n');
        builder.append("# TYPE ").append(name).append(" counter\n");
    }

    private String key(String service, String method) {
        return service + "#" + method;
    }

    private String label(String key) {
        String[] parts = key.split("#", 2);
        return "{service=\"" + parts[0] + "\",method=\"" + parts[1] + "\"}";
    }

    private long p95(CopyOnWriteArrayList<Long> values) {
        if (values.isEmpty()) {
            return 0;
        }
        Long[] sorted = values.toArray(Long[]::new);
        java.util.Arrays.sort(sorted);
        int index = Math.min(sorted.length - 1, (int) Math.ceil(sorted.length * 0.95) - 1);
        return sorted[index];
    }
}
