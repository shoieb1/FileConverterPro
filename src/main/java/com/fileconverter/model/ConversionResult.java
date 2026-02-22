package com.fileconverter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionResult {
    private boolean success;
    private String message;
    private String fileName;
    private String downloadUrl;
    private String conversionType;
    private long fileSizeBytes;
    private long processingTimeMs;

    public String getFileSizeFormatted() {
        if (fileSizeBytes < 1024) return fileSizeBytes + " B";
        if (fileSizeBytes < 1024 * 1024) return String.format("%.1f KB", fileSizeBytes / 1024.0);
        return String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024));
    }
}
