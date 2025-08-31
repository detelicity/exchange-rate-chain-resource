package com.mize.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

import com.mize.service.ExchangeRateService;

@RestController
@RequestMapping("/api/exchange-rates")
public class ExchangeRateController {
    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<String>> getExchangeRates() {
        return exchangeRateService.getExchangeRates()
            .thenApply(ResponseEntity::ok)
            .exceptionally(ex -> ResponseEntity.internalServerError().body("Error: " + ex.getMessage()));
    }
}