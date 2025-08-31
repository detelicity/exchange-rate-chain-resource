package com.mize.infrastructure;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public interface Storage<T> {
    CompletableFuture<T> getValue();
    CompletableFuture<Void> setValue(T value);
    Duration getExpiration();
    boolean canWrite();
}