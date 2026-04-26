package io.github.cozyoct.minirpc.tolerant;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CircuitBreaker {
    private final int failureThreshold;
    private final long openMillis;
    private final AtomicInteger failures = new AtomicInteger();
    private final AtomicLong openedAt = new AtomicLong(0);

    public CircuitBreaker(int failureThreshold, Duration openDuration) {
        this.failureThreshold = failureThreshold;
        this.openMillis = openDuration.toMillis();
    }

    public boolean allowRequest() {
        long opened = openedAt.get();
        return opened == 0 || System.currentTimeMillis() - opened > openMillis;
    }

    public void recordSuccess() {
        failures.set(0);
        openedAt.set(0);
    }

    public void recordFailure() {
        if (failures.incrementAndGet() >= failureThreshold) {
            openedAt.compareAndSet(0, System.currentTimeMillis());
        }
    }
}
