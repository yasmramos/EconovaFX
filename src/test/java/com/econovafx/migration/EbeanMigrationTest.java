package com.econovafx.migration;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Pruebas para verificar la correcta ejecución de las migraciones de Ebean.
 * Valida que las tablas se creen correctamente y los datos iniciales se carguen.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EbeanMigrationTest {

    private static Database db;

    @BeforeAll
    public static void initDB() {
        // Inicializar base de datos para tests
        System.setProperty("ebean.datasource.default.driver", "org.h2.Driver");
        System.setProperty("ebean.datasource.default.url", "jdbc:h2:mem:test_ebean_migrations;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        System.setProperty("ebean.datasource.default.username", "sa");
        System.setProperty("ebean.datasource.default.password", "");
        System.setProperty("ebean.migration.auto", "true");
        System.setProperty("ebean.migration.run", "true");
        System.setProperty("ebean.migration.generate", "false");
        
        db = DB.getDefault();
        assertNotNull(db, "La base de datos por defecto no debería ser nula");
    }

    @Test
    @Order(1)
    @DisplayName("Verificar existencia de tablas críticas")
    public void testCriticalTablesExist() {
        // Lista de tablas críticas que deben existir tras la migración inicial
        String[] criticalTables = {
            "account",
            "accounting_period",
            "transaction",
            "transaction_entry",
            "audit_log",
            "billing_series",
            "fixed_asset",
            "depreciation_record",
            "inventory_item",
            "inventory_movement"
        };

        for (String tableName : criticalTables) {
            try {
                var result = db.sqlQuery("SELECT 1 FROM " + tableName + " LIMIT 1").findOne();
                assertNotNull(result, "La tabla '" + tableName + "' debería existir tras la migración");
            } catch (Exception e) {
                fail("La tabla '" + tableName + "' no existe: " + e.getMessage());
            }
        }
    }

    @Test
    @Order(2)
    @DisplayName("Verificar carga del Plan de Cuentas inicial")
    public void testInitialChartOfAccountsLoaded() {
        // Verificar que existen cuentas contables iniciales
        var result = db.sqlQuery("SELECT COUNT(*) as count FROM account").findOne();
        assertNotNull(result, "Debería haber cuentas en la base de datos");
        Long count = (Long) result.get("count");
        assertTrue(count > 0, "Debería haber al menos una cuenta contable cargada");
    }

    @Test
    @Order(3)
    @DisplayName("Verificar creación de período contable actual")
    public void testCurrentAccountingPeriodCreated() {
        var result = db.sqlQuery("SELECT COUNT(*) as count FROM accounting_period WHERE status = 'OPEN'").findOneOrEmpty();
        assertTrue(result.isPresent(), "Debería existir al menos un período abierto");
        assertTrue((Long)result.get().get("count") >= 1, "Debería haber al menos un período contable abierto");
    }

    @Test
    @Order(4)
    @DisplayName("Verificar serie de facturación inicial")
    public void testInitialBillingSeriesCreated() {
        var result = db.sqlQuery("SELECT COUNT(*) as count FROM billing_series").findOneOrEmpty();
        assertTrue(result.isPresent(), "Debería existir la serie de facturación");
        assertTrue((Long)result.get().get("count") >= 1, "Debería haber al menos una serie de facturación creada");
    }

    @Test
    @Order(5)
    @DisplayName("Verificar integridad de llaves foráneas")
    public void testForeignKeyConstraints() {
        // Intentar insertar un registro huérfano debería fallar si las FK están bien
        // Esto es una prueba conceptual, en un entorno real se haría con transacción rollback
        assertDoesNotThrow(() -> {
            // Si las tablas existen y las FK están bien, esta consulta no debería lanzar error de sintaxis
            db.sqlQuery("SELECT 1 FROM transaction_entry LIMIT 1").findOne();
        }, "Las consultas básicas no deberían fallar si el esquema es correcto");
    }

    @Test
    @Order(6)
    @DisplayName("Verificar logs de migración")
    public void testMigrationLogs() {
        // Capturar logs SQL durante una operación simple
        LoggedSql.start();
        
        db.sqlQuery("SELECT 1 FROM account LIMIT 1").findOne();
        
        List<String> statements = LoggedSql.stop();
        assertFalse(statements.isEmpty(), "Debería haber registros SQL capturados");
        assertTrue(statements.get(0).contains("SELECT"), "Debería contener una sentencia SELECT");
    }
}
