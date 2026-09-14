package com.econovafx.modules.core.service;

import com.econovafx.modules.core.model.SystemConfiguration;
import com.econovafx.modules.core.service.SystemConfigService;
import com.econovafx.modules.core.repository.SystemConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para SystemConfigService
 */
@ExtendWith(MockitoExtension.class)
class SystemConfigServiceTest {

    @Mock
    private SystemConfigRepository repository;

    private SystemConfigService service;

    @BeforeEach
    void setUp() {
        service = new SystemConfigService(repository);
    }

    @Test
    void testGetCurrentConfig_WhenExists_ReturnsConfig() {
        // Arrange
        SystemConfiguration expectedConfig = new SystemConfiguration();
        expectedConfig.setEntityName("Entidad Test");
        expectedConfig.setBaseCurrency("CUP");
        
        when(repository.getCurrent()).thenReturn(Optional.of(expectedConfig));

        // Act
        SystemConfiguration result = service.getCurrentConfig();

        // Assert
        assertNotNull(result);
        assertEquals("Entidad Test", result.getEntityName());
        assertEquals("CUP", result.getBaseCurrency());
        verify(repository, times(1)).getCurrent();
    }

    @Test
    void testGetCurrentConfig_WhenNotExists_InitializesDefaults() {
        // Arrange
        SystemConfiguration defaultConfig = new SystemConfiguration();
        defaultConfig.setEntityName("Entidad Demo");
        
        when(repository.getCurrent()).thenReturn(Optional.empty());
        when(repository.initializeDefaults()).thenReturn(defaultConfig);

        // Act
        SystemConfiguration result = service.getCurrentConfig();

        // Assert
        assertNotNull(result);
        verify(repository, times(1)).getCurrent();
        verify(repository, times(1)).initializeDefaults();
    }

    @Test
    void testSaveConfig_ValidConfig_Success() {
        // Arrange
        SystemConfiguration config = new SystemConfiguration();
        config.setEntityName("Entidad Válida");
        config.setBaseCurrency("CUP");
        config.setFiscalYearStartMonth(1);
        config.setFiscalYearEndMonth(12);
        config.setPasswordMinLength(6);
        config.setMaxLoginAttempts(5);
        config.setSessionTimeoutMinutes(30);
        config.setAuditRetentionYears(5);
        config.setDecimalPrecision(2);
        config.setInventoryValuationMethod("PROMEDIO_PONDERADO");
        config.setRegimeType("MONO");

        when(repository.save(config)).thenReturn(config);

        // Act
        SystemConfiguration result = service.saveConfig(config);

        // Assert
        assertNotNull(result);
        assertEquals("Entidad Válida", result.getEntityName());
        verify(repository, times(1)).save(config);
    }

    @Test
    void testSaveConfig_NullEntityName_ThrowsException() {
        // Arrange
        SystemConfiguration config = new SystemConfiguration();
        config.setEntityName(null);
        config.setBaseCurrency("CUP");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.saveConfig(config);
        });
        verify(repository, never()).save(any());
    }

    @Test
    void testSaveConfig_EmptyEntityName_ThrowsException() {
        // Arrange
        SystemConfiguration config = new SystemConfiguration();
        config.setEntityName("");
        config.setBaseCurrency("CUP");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.saveConfig(config);
        });
        verify(repository, never()).save(any());
    }

    @Test
    void testSaveConfig_NullBaseCurrency_ThrowsException() {
        // Arrange
        SystemConfiguration config = new SystemConfiguration();
        config.setEntityName("Entidad Test");
        config.setBaseCurrency(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.saveConfig(config);
        });
        verify(repository, never()).save(any());
    }

    @Test
    void testSaveConfig_InvalidFiscalYearStart_ThrowsException() {
        // Arrange
        SystemConfiguration config = new SystemConfiguration();
        config.setEntityName("Entidad Test");
        config.setBaseCurrency("CUP");
        config.setFiscalYearStartMonth(13); // Inválido
        config.setFiscalYearEndMonth(12);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.saveConfig(config);
        });
        verify(repository, never()).save(any());
    }

    @Test
    void testSaveConfig_InvalidPasswordLength_ThrowsException() {
        // Arrange
        SystemConfiguration config = new SystemConfiguration();
        config.setEntityName("Entidad Test");
        config.setBaseCurrency("CUP");
        config.setFiscalYearStartMonth(1);
        config.setFiscalYearEndMonth(12);
        config.setPasswordMinLength(2); // Muy corto

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.saveConfig(config);
        });
        verify(repository, never()).save(any());
    }

    @Test
    void testSaveConfig_InvalidValuationMethod_ThrowsException() {
        // Arrange
        SystemConfiguration config = new SystemConfiguration();
        config.setEntityName("Entidad Test");
        config.setBaseCurrency("CUP");
        config.setFiscalYearStartMonth(1);
        config.setFiscalYearEndMonth(12);
        config.setInventoryValuationMethod("INVALIDO"); // Método inválido

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.saveConfig(config);
        });
        verify(repository, never()).save(any());
    }

    @Test
    void testSaveConfig_ValidPEPSMethod_Success() {
        // Arrange
        SystemConfiguration config = new SystemConfiguration();
        config.setEntityName("Entidad Test");
        config.setBaseCurrency("CUP");
        config.setFiscalYearStartMonth(1);
        config.setFiscalYearEndMonth(12);
        config.setInventoryValuationMethod("PEPS"); // Método válido
        config.setPasswordMinLength(6);
        config.setMaxLoginAttempts(5);
        config.setSessionTimeoutMinutes(30);
        config.setAuditRetentionYears(5);
        config.setDecimalPrecision(2);
        config.setRegimeType("MONO");

        when(repository.save(config)).thenReturn(config);

        // Act
        SystemConfiguration result = service.saveConfig(config);

        // Assert
        assertNotNull(result);
        assertEquals("PEPS", result.getInventoryValuationMethod());
        verify(repository, times(1)).save(config);
    }

    @Test
    void testIsInitialized_WhenExists_ReturnsTrue() {
        // Arrange
        when(repository.isInitialized()).thenReturn(true);

        // Act
        boolean result = service.isInitialized();

        // Assert
        assertTrue(result);
        verify(repository, times(1)).isInitialized();
    }

    @Test
    void testIsInitialized_WhenNotExists_ReturnsFalse() {
        // Arrange
        when(repository.isInitialized()).thenReturn(false);

        // Act
        boolean result = service.isInitialized();

        // Assert
        assertFalse(result);
        verify(repository, times(1)).isInitialized();
    }

    @Test
    void testGetConfigValue_EntityName_ReturnsCorrectValue() {
        // Arrange
        SystemConfiguration config = new SystemConfiguration();
        config.setEntityName("Mi Entidad");
        when(repository.getCurrent()).thenReturn(Optional.of(config));

        // Act
        Object result = service.getConfigValue("entityName");

        // Assert
        assertEquals("Mi Entidad", result);
    }

    @Test
    void testGetConfigValue_BaseCurrency_ReturnsCorrectValue() {
        // Arrange
        SystemConfiguration config = new SystemConfiguration();
        config.setBaseCurrency("USD");
        when(repository.getCurrent()).thenReturn(Optional.of(config));

        // Act
        Object result = service.getConfigValue("baseCurrency");

        // Assert
        assertEquals("USD", result);
    }

    @Test
    void testGetConfigValue_UnknownKey_ReturnsNull() {
        // Arrange
        SystemConfiguration config = new SystemConfiguration();
        when(repository.getCurrent()).thenReturn(Optional.of(config));

        // Act
        Object result = service.getConfigValue("unknownKey");

        // Assert
        assertNull(result);
    }

    @Test
    void testResetToDefaults_Success() {
        // Arrange
        SystemConfiguration defaultConfig = new SystemConfiguration();
        defaultConfig.setEntityName("Entidad Demo");
        
        when(repository.getCurrent()).thenReturn(Optional.of(new SystemConfiguration()));
        when(repository.initializeDefaults()).thenReturn(defaultConfig);

        // Act
        SystemConfiguration result = service.resetToDefaults();

        // Assert
        assertNotNull(result);
        assertEquals("Entidad Demo", result.getEntityName());
        verify(repository, times(1)).initializeDefaults();
    }
}
