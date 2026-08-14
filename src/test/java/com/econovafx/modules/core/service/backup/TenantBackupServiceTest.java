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
    void testBackupCurrentTenant_WithActiveTenant_DoesNotThrowIllegalStateException() throws IOException, SQLException {
        // Configurar un tenant activo en el contexto
        Company testCompany = new Company();
        testCompany.setId(1L);
        testCompany.setName("Test Company");
        TenantContext.setCurrentTenant(testCompany);

        // El backup no debe lanzar IllegalStateException por tenant nulo
        // Nota: Puede lanzar otras excepciones por la BD o archivos, pero no por tenant nulo
        assertDoesNotThrow(() -> {
            try {
                backupService.backupCurrentTenant();
            } catch (IOException | SQLException e) {
                // Es esperado que falle por la BD o archivos en tests unitarios,
                // pero NO por IllegalStateException de tenant nulo
                if (e instanceof IllegalStateException && 
                    e.getMessage().contains("No hay tenant activo")) {
                    fail("No debería lanzar IllegalStateException por tenant nulo cuando hay un tenant activo");
                }
                // Relanzar otras excepciones esperadas en entorno de test
                throw e;
            }
        }, "Should not throw IllegalStateException for null tenant when tenant is active");
    }

    @Test
    void testRestoreCurrentTenant_WithActiveTenant_DoesNotThrowIllegalStateException() throws IOException, SQLException {
        // Configurar un tenant activo en el contexto
        Company testCompany = new Company();
        testCompany.setId(1L);
        testCompany.setName("Test Company");
        TenantContext.setCurrentTenant(testCompany);

        // Intentar restaurar con un archivo inexistente (esperamos FileNotFoundException)
        // pero NO IllegalStateException por tenant nulo
        assertThrows(FileNotFoundException.class, () -> {
            backupService.restoreCurrentTenant("/nonexistent/backup.sql");
        }, "Should throw FileNotFoundException for missing file, not IllegalStateException");

        // Verificar que no lanzó IllegalStateException por tenant nulo
        TenantContext.clear();
        TenantContext.setCurrentTenant(testCompany);
        
        assertDoesNotThrow(() -> {
            try {
                backupService.restoreCurrentTenant("/nonexistent/backup.sql");
            } catch (FileNotFoundException e) {
                // Esperado - archivo no existe
                throw e;
            } catch (IOException | SQLException e) {
                // Es esperado que falle por otros motivos, pero NO por tenant nulo
                if (e instanceof IllegalStateException && 
                    e.getMessage().contains("No hay tenant activo")) {
                    fail("No debería lanzar IllegalStateException por tenant nulo cuando hay un tenant activo");
                }
                throw e;
            }
        }, "Should not throw IllegalStateException for null tenant when tenant is active");
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
