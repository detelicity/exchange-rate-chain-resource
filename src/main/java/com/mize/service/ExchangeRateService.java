package com.mize.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

import com.mize.infrastructure.ChainResource;

@Service
public class ExchangeRateService {
    private final ChainResource<String> chainResource;
    
    public ExchangeRateService(ChainResource<String> chainResource) {
        this.chainResource = chainResource;
    }

    public CompletableFuture<String> getExchangeRates(String base) {
        // Pass the base parameter through the chain
        return chainResource.getValue(base).thenApply(data -> {
            // The base filtering is now handled by the storage layers and API call
            // We just need to ensure the response is properly formatted
            return ensureProperFormat(data, base);
        });
    }

    private String ensureProperFormat(String jsonData, String base) {
        // Verify that the response contains the correct base currency
        String expectedBase = (base != null && !base.trim().isEmpty()) ? base.toUpperCase() : "USD";
        
        // Extract base from the JSON response
        String actualBase = extractBaseFromJson(jsonData);
        
        if (!actualBase.equalsIgnoreCase(expectedBase)) {
            throw new RuntimeException("Response base " + actualBase + 
                                     " does not match requested base " + expectedBase);
        }
        
        return jsonData;
    }

    private String extractBaseFromJson(String json) {
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