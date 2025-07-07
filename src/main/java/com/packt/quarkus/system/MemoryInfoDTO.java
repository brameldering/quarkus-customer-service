package com.packt.quarkus.system;

// Java 16+ feature
public record MemoryInfoDTO(
        long initialHeapSizeBytes,
        long initialHeapSizeMB,
        long maxHeapSizeBytes,
        long maxHeapSizeMB,
        long usedHeapSizeBytes,
        long usedHeapSizeMB,
        long freeHeapSizeBytes,
        long freeHeapSizeMB,
        long totalHeapSizeBytes,
        long totalHeapSizeMB
) {
    // Compact constructor to calculate MB values
    public MemoryInfoDTO(long initialHeapSizeBytes, long maxHeapSizeBytes, long usedHeapSizeBytes) {
        this(
                initialHeapSizeBytes,
                initialHeapSizeBytes / (1024 * 1024),
                maxHeapSizeBytes,
                maxHeapSizeBytes / (1024 * 1024),
                usedHeapSizeBytes,
                usedHeapSizeBytes / (1024 * 1024),
                maxHeapSizeBytes - usedHeapSizeBytes,
                (maxHeapSizeBytes - usedHeapSizeBytes) / (1024 * 1024),
                Runtime.getRuntime().totalMemory(),
                Runtime.getRuntime().totalMemory() / (1024 * 1024)
        );
    }
}