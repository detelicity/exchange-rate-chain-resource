package com.mize.infrastructure;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryStorage<T> implements Storage<T> {
    private Map<String, T> values; // Store values by base currency
    private Map<String, LocalDateTime> lastUpdatedMap; // Store timestamps by base currency
    private final Duration expiration;

    public MemoryStorage(Duration expiration) {
        this.expiration = expiration;
        this.values = new ConcurrentHashMap<>();
        this.lastUpdatedMap = new ConcurrentHashMap<>();
    }

    @Override
    public CompletableFuture<T> getValue() {
        return getValue("USD");
    }

    @Override
    public CompletableFuture<T> getValue(String base) {
        String baseKey = (base != null && !base.trim().isEmpty()) ? base.toUpperCase() : "USD";
        
        T value = values.get(baseKey);
        LocalDateTime lastUpdated = lastUpdatedMap.get(baseKey);
        
        if (value != null && !isExpired(lastUpdated)) {
            // Verify the stored data is for the correct base
            if (value instanceof String) {
                String json = (String) value;
                String storedBase = extractBaseFromJson(json);
                if (!storedBase.equalsIgnoreCase(baseKey)) {
                    return CompletableFuture.failedFuture(
                        new RuntimeException("Stored data is for base " + storedBase + ", requested " + baseKey)
                    );
                }
            }
            System.out.println("✅ MemoryStorage: Providing fresh data for base " + baseKey);
            return CompletableFuture.completedFuture(value);
        }
        return CompletableFuture.failedFuture(new RuntimeException("No valid data for base: " + baseKey));
    }

    @Override
    public CompletableFuture<Void> setValue(T value) {
        return setValue(value, "USD");
    }

    @Override
    public CompletableFuture<Void> setValue(T value, String base) {
        String baseKey = (base != null && !base.trim().isEmpty()) ? base.toUpperCase() : "USD";
        
        // Verify the data being stored matches the specified base
        if (value instanceof String) {
            String json = (String) value;
            String dataBase = extractBaseFromJson(json);
            if (!dataBase.equalsIgnoreCase(baseKey)) {
                return CompletableFuture.failedFuture(
                    new RuntimeException("Data base " + dataBase + " does not match specified base " + baseKey)
                );
            }
        }
        
        values.put(baseKey, value);
        lastUpdatedMap.put(baseKey, LocalDateTime.now());
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

    private boolean isExpired(LocalDateTime lastUpdated) {
        return lastUpdated == null || 
               LocalDateTime.now().isAfter(lastUpdated.plus(expiration));
    }
}