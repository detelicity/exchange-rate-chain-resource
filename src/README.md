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


## Error Handling Strategy

- **Graceful Degradation**: If one storage fails, chain continues to next
- **Exception Propagation**: Proper error messages through CompletableFuture
- **Circuit Breaker Pattern**: Automatic fallback through storage layers
