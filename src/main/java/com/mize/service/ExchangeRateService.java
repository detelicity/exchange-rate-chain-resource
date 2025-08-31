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

    public CompletableFuture<String> getExchangeRates() {
        return chainResource.getValue();
    }
}