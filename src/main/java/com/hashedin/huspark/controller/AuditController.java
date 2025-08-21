package com.hashedin.huspark.controller;

import com.hashedin.huspark.dto.PaginatedResponse;
import com.hashedin.huspark.entity.AuditLog;
import com.hashedin.huspark.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/logs")
    public ResponseEntity<PaginatedResponse<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditService.getAuditLogs(pageable));
    }

    @GetMapping("/logs/user/{userId}")
    public ResponseEntity<PaginatedResponse<AuditLog>> getAuditLogsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditService.getAuditLogsByUser(userId, pageable));
    }

    @GetMapping("/logs/entity/{entityType}/{entityId}")
    public ResponseEntity<PaginatedResponse<AuditLog>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditService.getAuditLogsByEntity(entityType, entityId, pageable));
    }

    @GetMapping("/logs/action/{action}")
    public ResponseEntity<PaginatedResponse<AuditLog>> getAuditLogsByAction(
            @PathVariable String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditService.getAuditLogsByAction(action, pageable));
    }

    @GetMapping("/logs/date-range")
    public ResponseEntity<PaginatedResponse<AuditLog>> getAuditLogsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditService.getAuditLogsByDateRange(start, end, pageable));
    }

    @GetMapping("/logs/user-email")
    public ResponseEntity<PaginatedResponse<AuditLog>> getAuditLogsByUserEmail(
            @RequestParam String userEmail,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditService.getAuditLogsByUserEmail(userEmail, pageable));
    }
}
