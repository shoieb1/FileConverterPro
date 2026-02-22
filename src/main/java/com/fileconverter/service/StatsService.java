package com.fileconverter.service;

import com.fileconverter.model.ConversionStats;
import org.springframework.stereotype.Service;

@Service
public class StatsService {
    private final ConversionStats stats = new ConversionStats();

    public void record(String category, boolean success) {
        stats.record(category, success);
    }

    public ConversionStats getStats() {
        return stats;
    }
}
