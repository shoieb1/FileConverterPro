package com.fileconverter.model;

import lombok.Data;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class ConversionStats {
    private final Map<String, AtomicLong> categoryCount = new ConcurrentHashMap<>();
    private final AtomicLong totalConversions = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong failureCount = new AtomicLong(0);

    public void record(String category, boolean success) {
        categoryCount.computeIfAbsent(category, k -> new AtomicLong(0)).incrementAndGet();
        totalConversions.incrementAndGet();
        if (success)
            successCount.incrementAndGet();
        else
            failureCount.incrementAndGet();
    }

    public Map<String, Long> getCategoryMap() {
        Map<String, Long> result = new java.util.LinkedHashMap<>();
        categoryCount.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }

    // Convenience getters for Thymeleaf templates (return long instead of
    // AtomicLong)
    public long getTotalConversionsValue() {
        return totalConversions.get();
    }

    public long getSuccessCountValue() {
        return successCount.get();
    }

    public long getFailureCountValue() {
        return failureCount.get();
    }
}
