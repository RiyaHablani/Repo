package com.hashedin.huspark.controller;

import com.hashedin.huspark.dto.MostBorrowedBookReport;
import com.hashedin.huspark.dto.OverdueItemReport;
import com.hashedin.huspark.dto.SystemHealthReport;
import com.hashedin.huspark.service.ReportingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for generating various reports and monitoring system health
 */
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reporting", description = "Reporting and monitoring endpoints")
public class ReportingController {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportingController.class);
    
    @Autowired
    private ReportingService reportingService;
    
    /**
     * Get most borrowed books report
     */
    @GetMapping("/most-borrowed-books")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Get most borrowed books report", 
               description = "Returns a list of books ordered by their borrowing frequency")
    public ResponseEntity<List<MostBorrowedBookReport>> getMostBorrowedBooks(
            @Parameter(description = "Number of books to return (default: 10, max: 100)")
            @RequestParam(defaultValue = "10") int limit) {
        
        logger.info("Request received for most borrowed books report with limit: {}", limit);
        
        if (limit <= 0 || limit > 100) {
            logger.warn("Invalid limit requested: {}. Using default value of 10", limit);
            limit = 10;
        }
        
        try {
            List<MostBorrowedBookReport> report = reportingService.getMostBorrowedBooks(limit);
            logger.info("Successfully generated most borrowed books report with {} entries", report.size());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Error generating most borrowed books report", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get overdue items report
     */
    @GetMapping("/overdue-items")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Get overdue items report", 
               description = "Returns a list of all overdue borrowing transactions")
    public ResponseEntity<List<OverdueItemReport>> getOverdueItems() {
        
        logger.info("Request received for overdue items report");
        
        try {
            List<OverdueItemReport> report = reportingService.getOverdueItems();
            logger.info("Successfully generated overdue items report with {} entries", report.size());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Error generating overdue items report", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get system health report
     */
    @GetMapping("/system-health")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get system health report", 
               description = "Returns comprehensive system health and monitoring information")
    public ResponseEntity<SystemHealthReport> getSystemHealth() {
        
        logger.debug("Request received for system health report");
        
        try {
            SystemHealthReport report = reportingService.getSystemHealthReport();
            logger.debug("Successfully generated system health report");
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Error generating system health report", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get borrowing statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Get borrowing statistics", 
               description = "Returns key statistics about borrowing activities")
    public ResponseEntity<Map<String, Object>> getBorrowingStatistics() {
        
        logger.info("Request received for borrowing statistics");
        
        try {
            Map<String, Object> statistics = reportingService.getBorrowingStatistics();
            logger.info("Successfully generated borrowing statistics");
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error generating borrowing statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get overdue items count (lightweight endpoint)
     */
    @GetMapping("/overdue-count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Get overdue items count", 
               description = "Returns just the count of overdue items for quick monitoring")
    public ResponseEntity<Map<String, Object>> getOverdueCount() {
        
        logger.debug("Request received for overdue count");
        
        try {
            List<OverdueItemReport> overdueItems = reportingService.getOverdueItems();
            Map<String, Object> response = Map.of(
                "overdueCount", overdueItems.size(),
                "timestamp", java.time.LocalDateTime.now()
            );
            logger.debug("Successfully generated overdue count: {}", overdueItems.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating overdue count", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
