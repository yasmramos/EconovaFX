package com.econovafx.service;

import com.econovafx.modules.core.model.AuditLog;
import com.econovafx.modules.core.model.User;
import com.econovafx.modules.core.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import com.econovafx.modules.core.service.AuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuditService
 */
@ExtendWith(MockitoExtension.class)
public class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditService auditService;

    @BeforeEach
    public void setUp() {
        auditService = new AuditService(auditLogRepository);
    }

    @Test
    public void testLogSuccess_ShouldSaveAuditLog() {
        // Arrange
        String username = "testuser";
        AuditLog.OperationType operationType = AuditLog.OperationType.CREATE;
        String entityType = "Transaction";
        Long entityId = 1L;
        String description = "Created transaction";

        // Act
        auditService.logSuccess(username, operationType, entityType, entityId, description);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();
        assertEquals(username, savedLog.getUsername());
        assertEquals(operationType, savedLog.getOperationType());
        assertEquals(entityType, savedLog.getEntityType());
        assertEquals(entityId, savedLog.getEntityId());
        assertEquals(description, savedLog.getDescription());
        assertTrue(savedLog.getSuccess());
        assertNull(savedLog.getErrorMessage());
    }

    @Test
    public void testLogFailure_ShouldSaveAuditLogWithFailure() {
        // Arrange
        String username = "testuser";
        AuditLog.OperationType operationType = AuditLog.OperationType.DELETE;
        String entityType = "User";
        Long entityId = 2L;
        String description = "Delete user failed";
        String errorMessage = "User not found";

        // Act
        auditService.logFailure(username, operationType, entityType, entityId, description, errorMessage);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();
        assertEquals(username, savedLog.getUsername());
        assertEquals(operationType, savedLog.getOperationType());
        assertEquals(entityType, savedLog.getEntityType());
        assertEquals(entityId, savedLog.getEntityId());
        assertEquals(description, savedLog.getDescription());
        assertFalse(savedLog.getSuccess());
        assertEquals(errorMessage, savedLog.getErrorMessage());
    }

    @Test
    public void testLogWithValues_ShouldSaveAuditLogWithOldAndNewValues() {
        // Arrange
        String username = "admin";
        AuditLog.OperationType operationType = AuditLog.OperationType.UPDATE;
        String entityType = "Account";
        Long entityId = 3L;
        String description = "Updated account name";
        String oldValues = "{\"name\": \"Old Account\"}";
        String newValues = "{\"name\": \"New Account\"}";

        // Act
        auditService.logWithValues(username, operationType, entityType, entityId, description, oldValues, newValues);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();
        assertEquals(username, savedLog.getUsername());
        assertEquals(operationType, savedLog.getOperationType());
        assertEquals(entityType, savedLog.getEntityType());
        assertEquals(entityId, savedLog.getEntityId());
        assertEquals(description, savedLog.getDescription());
        assertEquals(oldValues, savedLog.getOldValues());
        assertEquals(newValues, savedLog.getNewValues());
        assertTrue(savedLog.getSuccess());
    }

    @Test
    public void testGetAllAuditLogs_ShouldReturnAllLogs() {
        // Arrange
        List<AuditLog> expectedLogs = Arrays.asList(
                createAuditLog(1L, "user1", AuditLog.OperationType.CREATE),
                createAuditLog(2L, "user2", AuditLog.OperationType.UPDATE)
        );
        when(auditLogRepository.findAll()).thenReturn(expectedLogs);

        // Act
        List<AuditLog> result = auditService.getAllAuditLogs();

        // Assert
        assertEquals(2, result.size());
        verify(auditLogRepository).findAll();
    }

    @Test
    public void testGetAuditLogsByUser_ShouldReturnLogsForUser() {
        // Arrange
        String username = "testuser";
        List<AuditLog> expectedLogs = Arrays.asList(
                createAuditLog(1L, username, AuditLog.OperationType.LOGIN),
                createAuditLog(2L, username, AuditLog.OperationType.CREATE)
        );
        when(auditLogRepository.findByUsername(username)).thenReturn(expectedLogs);

        // Act
        List<AuditLog> result = auditService.getAuditLogsByUser(username);

        // Assert
        assertEquals(2, result.size());
        assertEquals(username, result.get(0).getUsername());
        verify(auditLogRepository).findByUsername(username);
    }

    @Test
    public void testGetAuditLogsByOperationType_ShouldReturnLogsForOperation() {
        // Arrange
        AuditLog.OperationType operationType = AuditLog.OperationType.DELETE;
        List<AuditLog> expectedLogs = Arrays.asList(
                createAuditLog(1L, "user1", operationType),
                createAuditLog(2L, "user2", operationType)
        );
        when(auditLogRepository.findByOperationType(operationType)).thenReturn(expectedLogs);

        // Act
        List<AuditLog> result = auditService.getAuditLogsByOperationType(operationType);

        // Assert
        assertEquals(2, result.size());
        assertEquals(operationType, result.get(0).getOperationType());
        verify(auditLogRepository).findByOperationType(operationType);
    }

    @Test
    public void testGetAuditLogsByEntityType_ShouldReturnLogsForEntity() {
        // Arrange
        String entityType = "Transaction";
        List<AuditLog> expectedLogs = Arrays.asList(
                createAuditLog(1L, "user1", AuditLog.OperationType.CREATE, entityType)
        );
        when(auditLogRepository.findByEntityType(entityType)).thenReturn(expectedLogs);

        // Act
        List<AuditLog> result = auditService.getAuditLogsByEntityType(entityType);

        // Assert
        assertEquals(1, result.size());
        assertEquals(entityType, result.get(0).getEntityType());
        verify(auditLogRepository).findByEntityType(entityType);
    }

    @Test
    public void testGetAuditLogsByDateRange_ShouldReturnLogsInRange() {
        // Arrange
        Instant startDate = Instant.parse("2024-01-01T00:00:00Z");
        Instant endDate = Instant.parse("2024-12-31T23:59:59Z");
        List<AuditLog> expectedLogs = Arrays.asList(
                createAuditLog(1L, "user1", AuditLog.OperationType.CREATE)
        );
        when(auditLogRepository.findByDateRange(startDate, endDate)).thenReturn(expectedLogs);

        // Act
        List<AuditLog> result = auditService.getAuditLogsByDateRange(startDate, endDate);

        // Assert
        assertEquals(1, result.size());
        verify(auditLogRepository).findByDateRange(startDate, endDate);
    }

    @Test
    public void testGetFailedOperations_ShouldReturnOnlyFailedLogs() {
        // Arrange
        List<AuditLog> expectedLogs = Arrays.asList(
                createFailedAuditLog(1L, "user1", AuditLog.OperationType.LOGIN)
        );
        when(auditLogRepository.findFailedOperations()).thenReturn(expectedLogs);

        // Act
        List<AuditLog> result = auditService.getFailedOperations();

        // Assert
        assertEquals(1, result.size());
        assertFalse(result.get(0).getSuccess());
        verify(auditLogRepository).findFailedOperations();
    }

    @Test
    public void testGetAuditLogById_ShouldReturnOptionalWithLog() {
        // Arrange
        Long id = 1L;
        AuditLog expectedLog = createAuditLog(id, "user1", AuditLog.OperationType.CREATE);
        when(auditLogRepository.findById(id)).thenReturn(Optional.of(expectedLog));

        // Act
        Optional<AuditLog> result = auditService.getAuditLogById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(auditLogRepository).findById(id);
    }

    @Test
    public void testGetAuditLogById_ShouldReturnEmptyWhenNotFound() {
        // Arrange
        Long id = 999L;
        when(auditLogRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<AuditLog> result = auditService.getAuditLogById(id);

        // Assert
        assertFalse(result.isPresent());
        verify(auditLogRepository).findById(id);
    }

    @Test
    public void testGetAuditLogCount_ShouldReturnCount() {
        // Arrange
        long expectedCount = 42L;
        when(auditLogRepository.count()).thenReturn(expectedCount);

        // Act
        long result = auditService.getAuditLogCount();

        // Assert
        assertEquals(expectedCount, result);
        verify(auditLogRepository).count();
    }

    @Test
    public void testGetOperationCountByUser_ShouldReturnCountForUser() {
        // Arrange
        String username = "testuser";
        long expectedCount = 15L;
        when(auditLogRepository.countByUser(username)).thenReturn(expectedCount);

        // Act
        long result = auditService.getOperationCountByUser(username);

        // Assert
        assertEquals(expectedCount, result);
        verify(auditLogRepository).countByUser(username);
    }

    @Test
    public void testExportToCSV_ShouldGenerateValidCSV() {
        // Arrange
        List<AuditLog> logs = Arrays.asList(
                createAuditLog(1L, "user1", AuditLog.OperationType.CREATE),
                createAuditLog(2L, "user2", AuditLog.OperationType.UPDATE)
        );

        // Act
        String csv = auditService.exportToCSV(logs);

        // Assert
        assertNotNull(csv);
        assertTrue(csv.startsWith("ID,Username,Operation Type,Entity Type,Entity ID,Description,Success,Created At"));
        assertTrue(csv.contains("1,user1,CREATE"));
        assertTrue(csv.contains("2,user2,UPDATE"));
        String[] lines = csv.split("\n");
        assertEquals(3, lines.length); // Header + 2 data rows
    }

    @Test
    public void testExportToCSV_ShouldHandleNullValues() {
        // Arrange
        AuditLog logWithNulls = new AuditLog();
        logWithNulls.setId(3L);
        logWithNulls.setUsername(null);
        logWithNulls.setOperationType(AuditLog.OperationType.OTHER);
        logWithNulls.setEntityType(null);
        logWithNulls.setEntityId(999L);
        logWithNulls.setDescription(null);
        logWithNulls.setSuccess(true);
        logWithNulls.setCreatedAt(Instant.now());

        List<AuditLog> logs = Arrays.asList(logWithNulls);

        // Act
        String csv = auditService.exportToCSV(logs);

        // Assert
        assertNotNull(csv);
        assertTrue(csv.contains("3,")); // Should handle null username gracefully
    }

    @Test
    public void testExportToCSV_ShouldEscapeSpecialCharacters() {
        // Arrange
        AuditLog logWithCommas = createAuditLog(1L, "user,with,commas", AuditLog.OperationType.CREATE);
        logWithCommas.setDescription("Description with \"quotes\" and, commas");
        List<AuditLog> logs = Arrays.asList(logWithCommas);

        // Act
        String csv = auditService.exportToCSV(logs);

        // Assert
        assertNotNull(csv);
        // Should escape quotes and wrap in quotes
        assertTrue(csv.contains("\"Description with \"\"quotes\"\" and, commas\""));
    }

    // Helper methods
    private AuditLog createAuditLog(Long id, String username, AuditLog.OperationType operationType) {
        return createAuditLog(id, username, operationType, "Transaction");
    }

    private AuditLog createAuditLog(Long id, String username, AuditLog.OperationType operationType, String entityType) {
        AuditLog log = new AuditLog();
        log.setId(id);
        log.setUsername(username);
        log.setOperationType(operationType);
        log.setEntityType(entityType);
        log.setEntityId(1L);
        log.setDescription("Test description");
        log.setSuccess(true);
        log.setCreatedAt(Instant.now());
        return log;
    }

    private AuditLog createFailedAuditLog(Long id, String username, AuditLog.OperationType operationType) {
        AuditLog log = createAuditLog(id, username, operationType);
        log.setSuccess(false);
        log.setErrorMessage("Test error");
        return log;
    }
}
