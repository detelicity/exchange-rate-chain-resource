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
        return getValue("USD");
    }

    @Override
    public CompletableFuture<String> getValue(String base) {
        return CompletableFuture.supplyAsync(() -> {
            String baseCurrency = (base != null && !base.trim().isEmpty()) ? base : "USD";
            String url = "https://openexchangerates.org/api/latest.json?app_id=" + apiKey + "&base=" + baseCurrency;
            try {
                String result = new RestTemplate().getForObject(url, String.class);
                
                // Verify the API returned the correct base
                String returnedBase = extractBaseFromJson(result);
                if (!returnedBase.equalsIgnoreCase(baseCurrency)) {
                    throw new RuntimeException("API returned base " + returnedBase + 
                                              " but requested base " + baseCurrency);
                }
                System.out.println("✅WebServiceStorage: Providing fresh data for base " + base);

                return result;
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch from web service for base " + baseCurrency + 
                                         ": " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<Void> setValue(String value) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> setValue(String value, String base) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public Duration getExpiration() {
        return Duration.ZERO;
    }

    @Override
    public boolean canWrite() {
        return false;
    }
}