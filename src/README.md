# ChainResource - Exchange Rate Service Solution

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

- ## Why This Approach?

While the solution may seem overengineered, it was designed with extensibility and maintainability in mind:
	•	Pluggable Storage Chain
The ChainResourceConfig class centralizes the definition of storage layers. Adding a new source (e.g., a FasterCache) requires only updating the configuration, without modifying the service or ChainResource classes.
```
@Bean
public ChainResource<String> exchangeRateChainResource() {
    List<Storage<String>> storageChain = List.of(
        new FasterCache<>(Duration.ofHours(1)), // Added seamlessly
        new MemoryStorage<>(Duration.ofHours(2)),
        new FileSystemStorage(cacheFilePath, Duration.ofHours(4)),
        new WebServiceStorage(apiKey)
    );
    return new ChainResource<>(storageChain);
}
 ```
expiration times can also be adjusted directly in configuration if caching policies change.

	•	Separation of Concerns
	•	Controller Layer: Acts as the API interface, keeping request/response handling cleanly separated from business logic.
	•	ExchangeRateService: Ensures responses are properly formatted while delegating data retrieval to the chain.
	•	ChainResource: Encapsulates all chain management and fallback logic.

By isolating responsibilities across layers, the system remains easier to extend, maintain, and test. 

- ## Tests & Verification

To validate the solution, I tested it with different request scenarios using Postman.

Example Requests (Screenshots Placeholder) 
1.	✅ Fetching latest exchange rates with base=USD
    <img width="841" height="646" alt="image" src="https://github.com/user-attachments/assets/963503c5-7b3c-47f8-9c99-21536c887075" />
	
 1.1  ✅ Fetching latest exchange rates WITHOUT passing base parameter
    <img width="841" height="646" alt="image" src="https://github.com/user-attachments/assets/5cb147b8-b950-4566-b813-24f6cfc6fca9" />

2.	✅ Retrieving cached data from MemoryStorage after first request
<img width="841" height="646" alt="image" src="https://github.com/user-attachments/assets/50e4e4a0-72f4-47de-a184-ff0b4d0950a1" />

3.	✅ Fallback to FileSystemStorage when memory cache expired
<img width="841" height="646" alt="image" src="https://github.com/user-attachments/assets/532b040a-e1ac-401f-9f0b-843adb1093a0" />

API Limitation: Base Currency Restriction


When testing with different base currencies (e.g., base=EUR), the following error occurred:
<img width="841" height="374" alt="image" src="https://github.com/user-attachments/assets/398f7abf-d9c2-4034-9700-05cb0f59af58" />
(for future improvements id add an Error Handler to return the right exception, but for the purpose of understanding what happened I added a print line in the code and found out the following:
<img width="442" height="147" alt="image" src="https://github.com/user-attachments/assets/6f54bc14-2ed2-4fa3-a319-d563247b6257" />


This restriction means that on the free plan, only USD can be used as the base currency.

👉 If upgraded to a Developer or higher plan, the solution would also support dynamic currency conversion for any base currency, effectively turning the service into a general-purpose currency converter, not just a USD-based on

