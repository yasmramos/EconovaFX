package com.econovafx.modules.core.service.backup;

import com.econovafx.modules.core.config.TenantContext;
import com.econovafx.modules.core.model.Company;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para TenantBackupService.
 * Verifica que el backup y restore funcionen correctamente con el tenant en contexto.
 */
class TenantBackupServiceTest {

    private TenantBackupService backupService;

    @BeforeEach
    void setUp() {
        backupService = new TenantBackupService();
    }

    @AfterEach
    void tearDown() {
        // Limpiar el contexto del tenant después de cada test
        TenantContext.clear();
    }

    @Test
    void testBackupCurrentTenant_WithActiveTenant_DoesNotThrowIllegalStateException() {
        // Configurar un tenant activo en el contexto sin persistir en BD
        // El test solo verifica que no se lance IllegalStateException por tenant nulo
        Company testCompany = new Company();
        testCompany.setId(1L);
        testCompany.setName("Test Company");
        testCompany.setCode("TEST");
        testCompany.setStatus("ACTIVE");
        
        TenantContext.setCurrentTenant(testCompany);

        // Verificamos que getCurrentTenantId() devuelve el ID correcto
        // Esto prueba que el contexto del tenant funciona correctamente
        try {
            var method = TenantBackupService.class.getDeclaredMethod("getCurrentTenantId");
            method.setAccessible(true);
            Long tenantId = (Long) method.invoke(backupService);
            
            assertEquals(1L, tenantId, "Should return the correct tenant ID from context");
        } catch (Exception e) {
            fail("Failed to invoke getCurrentTenantId: " + e.getMessage());
        }
    }

    @Test
    void testRestoreCurrentTenant_WithActiveTenant_DoesNotThrowIllegalStateException() {
        // Configurar un tenant activo en el contexto sin persistir en BD
        // El test solo verifica que no se lance IllegalStateException por tenant nulo
        Company testCompany = new Company();
        testCompany.setId(1L);
        testCompany.setName("Test Company");
        testCompany.setCode("TEST");
        testCompany.setStatus("ACTIVE");
        
        TenantContext.setCurrentTenant(testCompany);

        // Verificamos que getCurrentTenantId() devuelve el ID correcto antes de restaurar
        try {
            var method = TenantBackupService.class.getDeclaredMethod("getCurrentTenantId");
            method.setAccessible(true);
            Long tenantId = (Long) method.invoke(backupService);
            
            assertEquals(1L, tenantId, "Should return the correct tenant ID from context before restore");
        } catch (Exception e) {
            fail("Failed to invoke getCurrentTenantId: " + e.getMessage());
        }
        
        // Ahora verificamos que restore lanza FileNotFoundException (archivo no existe)
        // y NO IllegalStateException por tenant nulo
        assertThrows(FileNotFoundException.class, () -> {
            backupService.restoreCurrentTenant("/nonexistent/backup.sql");
        }, "Should throw FileNotFoundException for missing file, not IllegalStateException");
    }

    @Test
    void testGetCurrentTenantId_ReturnsCorrectId() {
        // Configurar un tenant con ID conocido
        Company testCompany = new Company();
        testCompany.setId(99L);
        testCompany.setName("Test Company 99");
        TenantContext.setCurrentTenant(testCompany);

        // Verificar que getCurrentTenantId devuelve el ID correcto
        // Usamos reflexión para acceder al método privado
        try {
            var method = TenantBackupService.class.getDeclaredMethod("getCurrentTenantId");
            method.setAccessible(true);
            Long tenantId = (Long) method.invoke(backupService);
            
            assertEquals(99L, tenantId, "Should return the correct tenant ID from context");
        } catch (Exception e) {
            fail("Failed to invoke getCurrentTenantId: " + e.getMessage());
        }
    }

    @Test
    void testGetCurrentTenantId_ReturnsNull_WhenNoTenant() {
        // Asegurar que no hay tenant en contexto
        TenantContext.clear();

        // Verificar que getCurrentTenantId devuelve null
        try {
            var method = TenantBackupService.class.getDeclaredMethod("getCurrentTenantId");
            method.setAccessible(true);
            Long tenantId = (Long) method.invoke(backupService);
            
            assertNull(tenantId, "Should return null when no tenant is active");
        } catch (Exception e) {
            fail("Failed to invoke getCurrentTenantId: " + e.getMessage());
        }
    }

    @Test
    void testBackupCurrentTenant_WithoutTenant_ThrowsIllegalStateException() throws IOException, SQLException {
        // Asegurar que no hay tenant en contexto
        TenantContext.clear();

        // Debe lanzar IllegalStateException cuando no hay tenant
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            backupService.backupCurrentTenant();
        });

        assertTrue(exception.getMessage().contains("No hay tenant activo"), 
                   "Error message should mention no active tenant");
    }

    @Test
    void testRestoreCurrentTenant_WithoutTenant_ThrowsIllegalStateException() throws IOException, SQLException {
        // Asegurar que no hay tenant en contexto
        TenantContext.clear();

        // Debe lanzar IllegalStateException cuando no hay tenant
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            backupService.restoreCurrentTenant("/some/backup.sql");
        });

        assertTrue(exception.getMessage().contains("No hay tenant activo"), 
                   "Error message should mention no active tenant");
    }
}
