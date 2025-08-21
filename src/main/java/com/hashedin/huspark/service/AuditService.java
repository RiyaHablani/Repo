package com.hashedin.huspark.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashedin.huspark.entity.AuditLog;
import com.hashedin.huspark.entity.User;
import com.hashedin.huspark.repository.AuditLogRepository;
import com.hashedin.huspark.repository.UserRepository;
import com.hashedin.huspark.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.hashedin.huspark.dto.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;
    private final ObjectMapper objectMapper;

    public void logAction(String action, String entityType, Long entityId, String description, Object details) {
        try {
            User currentUser = getCurrentUser();
            String detailsJson = objectMapper.writeValueAsString(details);
            
            // Check if details contain sensitive information
            boolean isEncrypted = containsSensitiveData(details);
            if (isEncrypted) {
                detailsJson = encryptionUtil.encrypt(detailsJson);
            }

            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(currentUser.getId());
            auditLog.setUserEmail(currentUser.getEmail());
            auditLog.setAction(action);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setDescription(description);
            auditLog.setDetails(detailsJson);
            auditLog.setEncrypted(isEncrypted);
            auditLog.setIpAddress(getClientIpAddress());
            auditLog.setUserAgent(getUserAgent());

            auditLogRepository.save(auditLog);
            log.info("Audit log created: {} - {} - {}", action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    public void logUserRegistration(User user) {
        Map<String, Object> details = Map.of(
            "userId", user.getId(),
            "email", user.getEmail(),
            "name", user.getName(),
            "role", user.getRole()
        );
        logAction("USER_REGISTRATION", "USER", user.getId(), 
                 "New user registered: " + user.getEmail(), details);
    }

    public void logBookCreation(Long bookId, String bookTitle) {
        Map<String, Object> details = Map.of(
            "bookId", bookId,
            "title", bookTitle
        );
        logAction("BOOK_CREATED", "BOOK", bookId, 
                 "Book created: " + bookTitle, details);
    }

    public void logBookUpdate(Long bookId, String bookTitle, Map<String, Object> changes) {
        Map<String, Object> details = Map.of(
            "bookId", bookId,
            "title", bookTitle,
            "changes", changes
        );
        logAction("BOOK_UPDATED", "BOOK", bookId, 
                 "Book updated: " + bookTitle, details);
    }

    public void logBookDeletion(Long bookId, String bookTitle) {
        Map<String, Object> details = Map.of(
            "bookId", bookId,
            "title", bookTitle
        );
        logAction("BOOK_DELETED", "BOOK", bookId, 
                 "Book deleted: " + bookTitle, details);
    }

    public void logBookBorrow(Long bookId, Long userId, Long transactionId) {
        Map<String, Object> details = Map.of(
            "bookId", bookId,
            "userId", userId,
            "transactionId", transactionId
        );
        logAction("BOOK_BORROWED", "BORROWING_TRANSACTION", transactionId, 
                 "Book borrowed", details);
    }

    public void logBookReturn(Long bookId, Long userId, Long transactionId) {
        Map<String, Object> details = Map.of(
            "bookId", bookId,
            "userId", userId,
            "transactionId", transactionId
        );
        logAction("BOOK_RETURNED", "BORROWING_TRANSACTION", transactionId, 
                 "Book returned", details);
    }

    public PaginatedResponse<AuditLog> getAuditLogs(Pageable pageable) {
        Page<AuditLog> auditLogPage = auditLogRepository.findAll(pageable);
        return convertToPaginatedResponse(auditLogPage);
    }

    public PaginatedResponse<AuditLog> getAuditLogsByUser(Long userId, Pageable pageable) {
        Page<AuditLog> auditLogPage = auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
        return convertToPaginatedResponse(auditLogPage);
    }

    public PaginatedResponse<AuditLog> getAuditLogsByEntity(String entityType, Long entityId, Pageable pageable) {
        Page<AuditLog> auditLogPage = auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId, pageable);
        return convertToPaginatedResponse(auditLogPage);
    }

    public PaginatedResponse<AuditLog> getAuditLogsByAction(String action, Pageable pageable) {
        Page<AuditLog> auditLogPage = auditLogRepository.findByActionOrderByTimestampDesc(action, pageable);
        return convertToPaginatedResponse(auditLogPage);
    }

    public PaginatedResponse<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<AuditLog> auditLogPage = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate, pageable);
        return convertToPaginatedResponse(auditLogPage);
    }

    public PaginatedResponse<AuditLog> getAuditLogsByUserEmail(String userEmail, Pageable pageable) {
        Page<AuditLog> auditLogPage = auditLogRepository.findByUserEmailOrderByTimestampDesc(userEmail, pageable);
        return convertToPaginatedResponse(auditLogPage);
    }

    private PaginatedResponse<AuditLog> convertToPaginatedResponse(Page<AuditLog> auditLogPage) {
        List<AuditLog> content = auditLogPage.getContent();
        
        return new PaginatedResponse<>(
            content,
            auditLogPage.getNumber(),
            auditLogPage.getSize(),
            auditLogPage.getTotalElements(),
            auditLogPage.getTotalPages(),
            auditLogPage.hasNext(),
            auditLogPage.hasPrevious(),
            auditLogPage.isFirst(),
            auditLogPage.isLast()
        );
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        } else if (authentication != null && authentication.getPrincipal() instanceof String) {
            // If principal is a string (username), fetch the user from repository
            String username = (String) authentication.getPrincipal();
            return userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
        }
        throw new RuntimeException("Unable to get current user from security context");
    }

    private boolean containsSensitiveData(Object details) {
        if (details instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) details;
            return map.keySet().stream().anyMatch(key -> 
                key.toString().toLowerCase().contains("password") ||
                key.toString().toLowerCase().contains("token") ||
                key.toString().toLowerCase().contains("secret")
            );
        }
        return false;
    }

    private String getClientIpAddress() {
        // This would typically be extracted from the HTTP request
        // For now, return a placeholder
        return "127.0.0.1";
    }

    private String getUserAgent() {
        // This would typically be extracted from the HTTP request
        // For now, return a placeholder
        return "Unknown";
    }
}
