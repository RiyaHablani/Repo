package com.hashedin.huspark.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for system health and monitoring reports
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemHealthReport {
    
    private String status;
    private LocalDateTime timestamp;
    private String applicationName;
    private String version;
    private Map<String, Object> databaseStatus;
    private Map<String, Object> memoryStatus;
    private Map<String, Object> diskStatus;
    private Map<String, Object> customMetrics;
    
    public SystemHealthReport(String status, String applicationName, String version) {
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.applicationName = applicationName;
        this.version = version;
    }
}
