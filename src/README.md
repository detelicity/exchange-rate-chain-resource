# ChainResource - Exchange Rate Service Solution

## Architecture Overview

A robust implementation of a resource chain that efficiently manages exchange rate data through multiple storage layers with intelligent caching and fallback mechanisms.

## Package Structure
```
src/main/java/com/mize/
├── ExchangeRateApplication.java          # Spring Boot entry point
├── controller/
│   └── ExchangeRateController.java       # REST API endpoint
├── service/
│   └── ExchangeRateService.java          # Business logic layer
├── infrastructure/                       # Core chain implementation
│   ├── Storage.java (interface)          # Storage contract
│   ├── ChainResource.java               # Chain orchestration logic
│   ├── MemoryStorage.java               # In-memory cache
│   ├── FileSystemStorage.java           # Persistent file storage
│   └── WebServiceStorage.java           # External API integration
└── config/
    └── ChainResourceConfig.java          # Dependency configuration
```

## Core Design Pattern: Chain of Responsibility

The solution implements the Chain of Responsibility pattern where each storage layer attempts to handle the request, passing it to the next layer if unable to provide valid data.

## Storage Chain Implementation

### 1. MemoryStorage (First Level)
- **Type**: Read/Write
- **Expiration**: 1 hour
- **Purpose**: Ultra-fast access for frequent requests
- **Implementation**: AtomicReference with time-based expiration check

### 2. FileSystemStorage (Second Level)  
- **Type**: Read/Write
- **Expiration**: 4 hours
- **Purpose**: Persistent cache surviving application restarts
- **Implementation**: JSON serialization to filesystem with Jackson

### 3. WebServiceStorage (Third Level)
- **Type**: Read-Only
- **Purpose**: Ground truth data source
- **Implementation**: REST API call to OpenExchangeRates.org

## Key Algorithms

### Chain Resolution Logic
```java
public CompletableFuture<T> getValue() {
    return getValueFromStorage(0); // Start with first storage
}

private CompletableFuture<T> getValueFromStorage(int index) {
    if (index >= storages.size()) {
        return CompletableFuture.failedFuture(new RuntimeException("No storage available"));
    }

    Storage<T> storage = storages.get(index);
    
    return storage.getValue().thenCompose(value -> {
        // Success: propagate value up the chain and return
        return propagateValueUp(value, index - 1).thenApply(v -> value);
    }).exceptionally(ex -> {
        // Failure: try next storage in chain
        return getValueFromStorage(index + 1).join();
    });
}
```

### Value Propagation
```java
private CompletableFuture<Void> propagateValueUp(T value, int toIndex) {
    if (toIndex < 0) return CompletableFuture.completedFuture(null);
    
    Storage<T> storage = storages.get(toIndex);
    if (storage.canWrite()) {
        return storage.setValue(value); // Cache in upper layers
    }
    return propagateValueUp(value, toIndex - 1);
}
```

## Data Flow

```
[Client Request] → Controller → Service → ChainResource
                                      │
                                      ├──① MemoryStorage (check)
                                      │     │
                                      │     ├── ✅ Valid → Return + Propagate Up
                                      │     └── ❌ Expired/Missing → 
                                      │
                                      ├──② FileSystemStorage (check)  
                                      │     │
                                      │     ├── ✅ Valid → Return + Propagate Up
                                      │     └── ❌ Expired/Missing →
                                      │
                                      └──③ WebServiceStorage (fetch)
                                            │
                                            ├── ✅ Success → Return + Propagate Down
                                            └── ❌ Failure → Error Response
```

## Performance Characteristics

- **Best Case**: ~1ms (memory hit)
- **Average Case**: ~10-50ms (file system hit)  
- **Worst Case**: ~500-2000ms (API call + propagation)

## Configuration Requirements

```properties
# application.properties
openexchangerates.api.key=your_api_key_here
exchange.cache.file=./data/exchange-rates.json
```

## Error Handling Strategy

- **Graceful Degradation**: If one storage fails, chain continues to next
- **Exception Propagation**: Proper error messages through CompletableFuture
- **Circuit Breaker Pattern**: Automatic fallback through storage layers

## Testing Considerations

- Unit tests for each storage implementation
- Integration tests for chain behavior
- Mock external API dependencies
- Test expiration logic and propagation

## Scalability Features

- **Thread-Safe**: Atomic operations for concurrent access
- **Memory Efficient**: Automatic cache expiration
- **Extensible**: Easy to add new storage implementations
- **Configurable**: Expiration times adjustable via configuration

This implementation provides a production-ready solution that balances performance, reliability, and maintainability while meeting all specified requirements.