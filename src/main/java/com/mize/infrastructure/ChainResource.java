package com.mize.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChainResource<T> {
    private final List<Storage<T>> storages;

    public ChainResource(List<Storage<T>> storages) {
        this.storages = storages;
    }

    public CompletableFuture<T> getValue() {
        return getValueFromStorage(0);
    }

    private CompletableFuture<T> getValueFromStorage(int index) {
        if (index >= storages.size()) {
            return CompletableFuture.failedFuture(new RuntimeException("No storage available"));
        }

        Storage<T> storage = storages.get(index);
        
        return storage.getValue().thenCompose(value -> {
            // If we got a value, propagate it up and return it
            return propagateValueUp(value, index - 1).thenApply(v -> value);
        }).exceptionally(ex -> {
            // If this storage failed, try the next one
            return getValueFromStorage(index + 1).join();
        });
    }

    private CompletableFuture<Void> propagateValueUp(T value, int toIndex) {
        if (toIndex < 0) return CompletableFuture.completedFuture(null);
        
        Storage<T> storage = storages.get(toIndex);
        if (storage.canWrite()) {
            return storage.setValue(value);
        }
        return propagateValueUp(value, toIndex - 1);
    }
}