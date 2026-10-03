package com.econovafx.modules.core.service.backup;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Transaction;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import com.econovafx.modules.core.config.TenantContext;
import com.econovafx.modules.core.config.DatabaseConfig;

/**
 * Servicio interno para backup y restore de datos por tenant.
 * NO expone endpoints REST ni CLI, se invoca desde la lógica interna del sistema.
 */
@Singleton
public class TenantBackupService {

    private static final Logger log = LoggerFactory.getLogger(TenantBackupService.class);
    private static final String BACKUP_DIR = System.getProperty("user.home") + "/econova_backups";

    /**
     * Realiza un backup completo del tenant actual en contexto.
     * Genera un archivo SQL con INSERTs para todas las tablas del tenant.
     * 
     * @return Ruta absoluta del archivo de backup generado
     * @throws IOException si falla la escritura del archivo
     * @throws SQLException si falla la conexión a la BD
     */
    public String backupCurrentTenant() throws IOException, SQLException {
        Long tenantId = getCurrentTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("No hay tenant activo en el contexto para realizar backup");
        }

        Path backupPath = Paths.get(BACKUP_DIR);
        if (!Files.exists(backupPath)) {
            Files.createDirectories(backupPath);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "backup_" + tenantId + "_" + timestamp + ".sql";
        Path fullPath = backupPath.resolve(fileName);

        log.info("Iniciando backup del tenant: {} en {}", tenantId, fullPath);

        // Obtener la base de datos específica del tenant actual
        Database db = DatabaseConfig.getTenantDatabase();
        DataSource ds = db.dataSource();
        
        try (BufferedWriter writer = Files.newBufferedWriter(fullPath)) {
            writeHeader(writer, tenantId.toString());
            
            try (Connection conn = ds.getConnection()) {
                // Obtener lista de tablas (ajustar según PostgreSQL/MySQL)
                List<String> tables = getTenantTables(conn);
                
                for (String table : tables) {
                    log.debug("Exportando tabla: {}", table);
                    exportTableData(conn, writer, table);
                }
            }
        }

        log.info("Backup completado exitosamente: {}", fullPath);
        return fullPath.toString();
    }

    /**
     * Restaura datos desde un archivo de backup al tenant actual en contexto.
     * Ejecuta el script SQL dentro de una transacción.
     * 
     * @param backupFilePath Ruta del archivo SQL a restaurar
     * @throws IOException si no se encuentra el archivo
     * @throws SQLException si falla la ejecución del script
     */
    public void restoreCurrentTenant(String backupFilePath) throws IOException, SQLException {
        Long tenantId = getCurrentTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("No hay tenant activo en el contexto para realizar restore");
        }

        Path filePath = Paths.get(backupFilePath);
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("Archivo de backup no encontrado: " + backupFilePath);
        }

        log.warn("Iniciando RESTAURE del tenant: {} desde {}", tenantId, backupFilePath);
        log.warn("ADVERTENCIA: Esta operación puede sobreescribir datos existentes");

        // Obtener la base de datos específica del tenant actual
        Database db = DatabaseConfig.getTenantDatabase();
        DataSource ds = db.dataSource();
        
        // Ejecutar en una sola transacción grande
        try (Transaction transaction = db.beginTransaction()) {
            try (Connection conn = ds.getConnection()) {
                // Deshabilitar temporalmente constraints si es necesario
                disableConstraints(conn);
                
                try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                    String line;
                    StringBuilder sqlBuffer = new StringBuilder();
                    
                    while ((line = reader.readLine()) != null) {
                        // Ignorar comentarios y líneas vacías
                        if (line.trim().isEmpty() || line.trim().startsWith("--")) {
                            continue;
                        }
                        
                        sqlBuffer.append(line).append("\n");
                        
                        // Ejecutar cuando encontramos punto y coma (fin de statement)
                        if (line.trim().endsWith(";")) {
                            String sql = sqlBuffer.toString().trim();
                            if (!sql.isEmpty()) {
                                try (var stmt = conn.createStatement()) {
                                    stmt.execute(sql);
                                } catch (SQLException e) {
                                    log.error("Error ejecutando SQL: {}", sql, e);
                                    throw e;
                                }
                            }
                            sqlBuffer.setLength(0);
                        }
                    }
                }
                
                // Restaurar constraints
                enableConstraints(conn);
            }
            transaction.commit();
        }

        log.info("Restore completado exitosamente para tenant: {}", tenantId);
    }

    /**
     * Lista todos los backups disponibles para un tenant específico.
     * 
     * @param tenantId ID del tenant
     * @return Lista de rutas absolutas a archivos de backup
     */
    public List<String> listBackupsForTenant(Long tenantId) {
        List<String> backups = new ArrayList<>();
        Path backupDir = Paths.get(BACKUP_DIR);
        
        if (!Files.exists(backupDir)) {
            return backups;
        }

        try (var stream = Files.list(backupDir)) {
            stream.filter(path -> path.getFileName().toString().startsWith("backup_" + tenantId + "_"))
                  .filter(path -> path.getFileName().toString().endsWith(".sql"))
                  .forEach(path -> backups.add(path.toString()));
        } catch (IOException e) {
            log.error("Error listando backups para tenant: {}", tenantId, e);
        }

        return backups;
    }

    // Métodos auxiliares privados

    private Long getCurrentTenantId() {
        // Llamada directa al método estático de TenantContext sin reflexión
        return TenantContext.getCurrentTenantId();
    }

    private void writeHeader(BufferedWriter writer, String tenantId) throws IOException {
        writer.write("-- Backup generado por EconovaFX\n");
        writer.write("-- Tenant: " + tenantId + "\n");
        writer.write("-- Fecha: " + LocalDateTime.now() + "\n");
        writer.write("-- ===========================================\n\n");
    }

    private List<String> getTenantTables(Connection conn) throws SQLException {
        List<String> tables = new ArrayList<>();
        var metadata = conn.getMetaData();
        
        // Obtener todas las tablas del catálogo actual
        try (var rs = metadata.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                // Excluir tablas del sistema o de migración
                if (!tableName.startsWith("flyway_") && !tableName.equals("db_migration")) {
                    tables.add(tableName);
                }
            }
        }
        
        return tables;
    }

    private void exportTableData(Connection conn, BufferedWriter writer, String tableName) 
            throws SQLException, IOException {
        
        // Construir query SELECT para obtener todos los datos de la tabla
        // En modo TenantMode.DB no hay columna tenant_id, ya que cada tenant tiene su propia BD
        String selectSql = "SELECT * FROM " + tableName;
        
        try (var stmt = conn.prepareStatement(selectSql)) {
            try (var rs = stmt.executeQuery()) {
                var rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount();
                
                // Escribir comentario con nombre de tabla
                writer.write("-- Datos de la tabla: " + tableName + "\n");
                
                while (rs.next()) {
                    StringBuilder insertSql = new StringBuilder();
                    insertSql.append("INSERT INTO ").append(tableName).append(" (");
                    
                    // Nombres de columnas
                    for (int i = 1; i <= columnCount; i++) {
                        if (i > 1) insertSql.append(", ");
                        insertSql.append(rsmd.getColumnName(i));
                    }
                    
                    insertSql.append(") VALUES (");
                    
                    // Valores
                    for (int i = 1; i <= columnCount; i++) {
                        if (i > 1) insertSql.append(", ");
                        
                        Object value = rs.getObject(i);
                        if (value == null) {
                            insertSql.append("NULL");
                        } else if (value instanceof String || 
                                   value instanceof java.sql.Date || 
                                   value instanceof java.sql.Timestamp) {
                            // Escapar comillas simples en strings
                            String escaped = value.toString().replace("'", "''");
                            insertSql.append("'").append(escaped).append("'");
                        } else if (value instanceof Boolean) {
                            insertSql.append(value.toString());
                        } else {
                            insertSql.append(value.toString());
                        }
                    }
                    
                    insertSql.append(");\n");
                    writer.write(insertSql.toString());
                }
                
                writer.write("\n");
            }
        }
    }

    private void disableConstraints(Connection conn) throws SQLException {
        // Implementación para H2 (motor usado por defecto)
        // H2 no soporta SET CONSTRAINTS ALL DEFERRED como PostgreSQL
        // Se usa sintaxis compatible con H2
        try (var stmt = conn.createStatement()) {
            // En H2, las constraints se pueden deshabilitar temporalmente
            // pero la sintaxis es diferente. Usamos un enfoque tolerante.
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
        } catch (SQLException e) {
            log.debug("No se pudieron deshabilitar constraints (puede ser normal): {}", e.getMessage());
        }
    }

    private void enableConstraints(Connection conn) throws SQLException {
        try (var stmt = conn.createStatement()) {
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
        } catch (SQLException e) {
            log.debug("No se pudieron habilitar constraints: {}", e.getMessage());
        }
    }
}
