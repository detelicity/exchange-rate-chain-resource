package com.mize.infrastructure;

import org.springframework.web.client.RestTemplate;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class WebServiceStorage implements Storage<String> {
    private final String apiKey;

    public WebServiceStorage(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public CompletableFuture<String> getValue() {
        return CompletableFuture.supplyAsync(() -> {
            String url = "https://openexchangerates.org/api/latest.json?app_id=" + apiKey;
            return new RestTemplate().getForObject(url, String.class);
        });
    }

    @Override
    public CompletableFuture<Void> setValue(String value) {
        return CompletableFuture.completedFuture(null); // Read-only
    }

    @Override
    public Duration getExpiration() {
        return Duration.ZERO; // Always fresh
    }

    @Override
    public boolean canWrite() {
        return false; // Read-only
    }
}