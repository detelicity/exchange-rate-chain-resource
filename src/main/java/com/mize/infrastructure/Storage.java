package com.mize.infrastructure;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public interface Storage<T> {
    CompletableFuture<T> getValue();
    CompletableFuture<T> getValue(String base);
    CompletableFuture<Void> setValue(T value);
    CompletableFuture<Void> setValue(T value, String base); 
    Duration getExpiration();
    boolean canWrite();
    
    // Helper method to extract base from JSON (default implementation)
    default String extractBaseFromJson(String json) {
        if (json == null) return "USD";
        try {
            // Simple extraction - look for "base":"CUR" pattern
            int baseIndex = json.indexOf("\"base\":\"");
            if (baseIndex != -1) {
                int start = baseIndex + 8; // Length of "\"base\":\""
                int end = json.indexOf("\"", start);
                if (end != -1) {
                    return json.substring(start, end);
                }
            }
        } catch (Exception e) {
            // Fall through to default
        }
        return "USD"; // Default if not found or error
    }
}