package com.mize.infrastructure;

import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@AllArgsConstructor
public class StorageResult<T> {
    private boolean hasValue;
    private T value;
    private LocalDateTime storedAt;

    public T getValue() {
        return value;
    }

    public LocalDateTime getStoredAt() {
        return storedAt;
    }


    public boolean isExpired(LocalDateTime now, java.time.Duration expirationInterval) {
        if (!hasValue || storedAt == null) {
            return true;
        }
        return now.isAfter(storedAt.plus(expirationInterval));
    }
}