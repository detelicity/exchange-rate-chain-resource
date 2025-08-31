package com.mize.infrastructure;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChainResource<T> {
    private final List<Storage<T>> storages;

    public ChainResource(List<Storage<T>> storages) {
        this.storages = storages;
    }

    public CompletableFuture<T> getValue() {
        return getValueFromStorage(0, null);
    }

    public CompletableFuture<T> getValue(String base) {
        return getValueFromStorage(0, base);
    }

    private CompletableFuture<T> getValueFromStorage(int index, String base) {
        if (index >= storages.size()) {
            return CompletableFuture.failedFuture(new RuntimeException("No storage available"));
        }

        Storage<T> storage = storages.get(index);
        String sourceName = storage.getClass().getSimpleName();
        
        CompletableFuture<T> valueFuture;
        
        // Use base-aware method if base is provided
        if (base != null) {
            valueFuture = storage.getValue(base);
        } else {
            valueFuture = storage.getValue();
        }
        
        return valueFuture.thenCompose(value -> {
            // Add source information to the value
            T valueWithSource = addSourceInfo(value, sourceName);
            // Propagate value up the chain with base parameter
            return propagateValueUp(valueWithSource, index - 1, base).thenApply(v -> valueWithSource);
        }).exceptionally(ex -> {
            // If this storage failed, try the next one
            return getValueFromStorage(index + 1, base).join();
        });
    }

    private CompletableFuture<Void> propagateValueUp(T value, int toIndex, String base) {
        if (toIndex < 0) return CompletableFuture.completedFuture(null);
        
        Storage<T> storage = storages.get(toIndex);
        if (storage.canWrite()) {
            // Use base-aware method if base is provided
            if (base != null) {
                return storage.setValue(value, base);
            } else {
                return storage.setValue(value);
            }
        }
        return propagateValueUp(value, toIndex - 1, base);
    }

    @SuppressWarnings("unchecked")
    private T addSourceInfo(T value, String sourceName) {
        if (value instanceof String) {
            String stringValue = (String) value;
            // Remove existing source if present to avoid duplication
            stringValue = stringValue.replaceAll(",\"source\":\"[^\"]*\"", "");
            // Add source information
            if (stringValue.endsWith("}")) {
                stringValue = stringValue.substring(0, stringValue.length() - 1) + 
                            ",\"source\":\"" + sourceName + "\"}";
            } else {
                stringValue += " - source: " + sourceName;
            }
            return (T) stringValue;
        }
        return value;
    }
}