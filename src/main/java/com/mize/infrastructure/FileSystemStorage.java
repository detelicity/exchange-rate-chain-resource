package com.mize.infrastructure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class FileSystemStorage implements Storage<String> {
    private final Path baseDirectory;
    private final Duration expiration;
    private final String baseFileName;

    public FileSystemStorage(String filePath, Duration expiration) {
        Path fullPath = Path.of(filePath);
        this.baseDirectory = fullPath.getParent();
        this.baseFileName = fullPath.getFileName().toString().replace(".json", "");
        this.expiration = expiration;
    }

    @Override
    public CompletableFuture<String> getValue() {
        return getValue("USD");
    }

    @Override
    public CompletableFuture<String> getValue(String base) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path filePath = getFilePathForBase(base);
                if (Files.exists(filePath)) {
                    String jsonData = Files.readString(filePath);
                    
                    // Verify the stored data is for the correct base
                    String storedBase = extractBaseFromJson(jsonData);
                    String expectedBase = (base != null && !base.trim().isEmpty()) ? base.toUpperCase() : "USD";
                    
                    if (!storedBase.equalsIgnoreCase(expectedBase)) {
                        throw new RuntimeException("File contains data for base " + storedBase + 
                                                  ", but requested base " + expectedBase);
                    }
                    System.out.println("✅ FileSystemStorage: Providing fresh data for base " + base);
                    return jsonData;
                } else {
                    throw new RuntimeException("File not found for base: " + base);
                }
            } catch (IOException e) {
                throw new RuntimeException("File read failed for base: " + base + ": " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<Void> setValue(String value) {
        return setValue(value, "USD");
    }

    @Override
    public CompletableFuture<Void> setValue(String value, String base) {
        return CompletableFuture.runAsync(() -> {
            try {
                String baseKey = (base != null && !base.trim().isEmpty()) ? base.toUpperCase() : "USD";
                
                // Verify the data being stored matches the specified base
                String dataBase = extractBaseFromJson(value);
                if (!dataBase.equalsIgnoreCase(baseKey)) {
                    throw new RuntimeException("Data base " + dataBase + " does not match specified base " + baseKey);
                }
                
                Path filePath = getFilePathForBase(base);
                Files.createDirectories(filePath.getParent());
                Files.writeString(filePath, value);
            } catch (IOException e) {
                throw new RuntimeException("File write failed for base: " + base + ": " + e.getMessage());
            } catch (RuntimeException e) {
                throw e; // Re-throw validation errors
            }
        });
    }

    @Override
    public Duration getExpiration() {
        return expiration;
    }

    @Override
    public boolean canWrite() {
        return true;
    }

    private Path getFilePathForBase(String base) {
        String baseKey = (base != null && !base.trim().isEmpty()) ? base.toUpperCase() : "USD";
        String fileName = baseFileName + "-" + baseKey.toLowerCase() + ".json";
        return baseDirectory.resolve(fileName);
    }
}