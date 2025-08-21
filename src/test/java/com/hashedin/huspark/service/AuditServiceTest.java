package com.hashedin.huspark.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashedin.huspark.entity.AuditLog;
import com.hashedin.huspark.entity.Role;
import com.hashedin.huspark.entity.User;
import com.hashedin.huspark.repository.AuditLogRepository;
import com.hashedin.huspark.repository.UserRepository;
import com.hashedin.huspark.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EncryptionUtil encryptionUtil;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuditService auditService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setRole(Role.LIBRARIAN);
    }

    @Test
    void testLogAction_WhenValidAction_ShouldLogSuccessfully() throws Exception {
        // Given
        String action = "TEST_ACTION";
        String entityType = "TEST_ENTITY";
        Long entityId = 1L;
        String description = "Test description";
        Map<String, Object> details = new HashMap<>();
        details.put("key", "value");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"key\":\"value\"}");
        when(encryptionUtil.encrypt(anyString())).thenReturn("encrypted_data");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            auditService.logAction(action, entityType, entityId, description, details);

            // Then
            verify(auditLogRepository).save(any(AuditLog.class));
            verify(objectMapper).writeValueAsString(details);
        }
    }

    @Test
    void testLogAction_WhenExceptionOccurs_ShouldHandleGracefully() throws Exception {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("JSON error"));

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            auditService.logAction("TEST", "ENTITY", 1L, "Description", new HashMap<>());

            // Then
            verify(auditLogRepository, never()).save(any(AuditLog.class));
        }
    }

    @Test
    void testLogUserRegistration_ShouldLogUserRegistration() throws Exception {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"userId\":1}");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            auditService.logUserRegistration(testUser);

            // Then
            verify(auditLogRepository).save(any(AuditLog.class));
            verify(objectMapper).writeValueAsString(any(Map.class));
        }
    }

    @Test
    void testLogBookCreation_ShouldLogBookCreation() throws Exception {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"bookId\":1}");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            auditService.logBookCreation(1L, "Test Book");

            // Then
            verify(auditLogRepository).save(any(AuditLog.class));
            verify(objectMapper).writeValueAsString(any(Map.class));
        }
    }

    @Test
    void testLogBookUpdate_ShouldLogBookUpdate() throws Exception {
        // Given
        Map<String, Object> changes = new HashMap<>();
        changes.put("title", "Updated Title");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"bookId\":1}");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            auditService.logBookUpdate(1L, "Test Book", changes);

            // Then
            verify(auditLogRepository).save(any(AuditLog.class));
            verify(objectMapper).writeValueAsString(any(Map.class));
        }
    }

    @Test
    void testLogBookDeletion_ShouldLogBookDeletion() throws Exception {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"bookId\":1}");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            auditService.logBookDeletion(1L, "Test Book");

            // Then
            verify(auditLogRepository).save(any(AuditLog.class));
            verify(objectMapper).writeValueAsString(any(Map.class));
        }
    }
}
