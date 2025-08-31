package com.mize.config;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mize.infrastructure.ChainResource;
import com.mize.infrastructure.FileSystemStorage;
import com.mize.infrastructure.MemoryStorage;
import com.mize.infrastructure.Storage;
import com.mize.infrastructure.WebServiceStorage;

@Configuration
public class ChainResourceConfig {

    @Value("${openexchangerates.api.key}")
    private String apiKey;

    @Value("${exchange.cache.file:./data/exchange-rates.json}")
    private String cacheFilePath;

    @Bean
    public ChainResource<String> exchangeRateChainResource() {
        // Create storage chain: Memory -> FileSystem -> WebService
        List<Storage<String>> storageChain = List.of(
            new MemoryStorage<String>(Duration.ofHours(1)),
            new FileSystemStorage(cacheFilePath, Duration.ofHours(4)),
            new WebServiceStorage(apiKey)
        );

        return new ChainResource<>(storageChain);
    }
}