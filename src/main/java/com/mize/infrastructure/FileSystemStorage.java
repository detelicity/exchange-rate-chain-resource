package com.mize.infrastructure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class FileSystemStorage implements Storage<String> {
    private final Path filePath;
    private final Duration expiration;

    public FileSystemStorage(String filePath, Duration expiration) {
        this.filePath = Path.of(filePath);
        this.expiration = expiration;
    }

    @Override
    public CompletableFuture<String> getValue() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Files.readString(filePath);
            } catch (IOException e) {
                throw new RuntimeException("File read failed");
            }
        });
    }

    @Override
    public CompletableFuture<Void> setValue(String value) {
        return CompletableFuture.runAsync(() -> {
            try {
                Files.createDirectories(filePath.getParent());
                Files.writeString(filePath, value);
            } catch (IOException e) {
                throw new RuntimeException("File write failed");
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
}