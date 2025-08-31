package com.mize.infrastructure;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public class MemoryStorage<T> implements Storage<T> {
    private T value;
    private LocalDateTime lastUpdated;
    private final Duration expiration;

    public MemoryStorage(Duration expiration) {
        this.expiration = expiration;
    }

    @Override
    public CompletableFuture<T> getValue() {
        if (value != null && !isExpired()) {
            return CompletableFuture.completedFuture(value);
        }
        return CompletableFuture.failedFuture(new RuntimeException("No valid data"));
    }

    @Override
    public CompletableFuture<Void> setValue(T value) {
        this.value = value;
        this.lastUpdated = LocalDateTime.now();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public Duration getExpiration() {
        return expiration;
    }

    @Override
    public boolean canWrite() {
        return true;
    }

    private boolean isExpired() {
        return lastUpdated == null || 
               LocalDateTime.now().isAfter(lastUpdated.plus(expiration));
    }
}