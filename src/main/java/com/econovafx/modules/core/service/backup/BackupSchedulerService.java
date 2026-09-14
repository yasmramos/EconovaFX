package com.econovafx.modules.core.service.backup;

import com.econovafx.modules.core.config.TenantContext;
import com.econovafx.modules.core.model.Company;
import com.econovafx.modules.core.model.SystemConfiguration;
import com.econovafx.modules.core.service.CompanyService;
import com.econovafx.modules.core.service.SystemConfigService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Servicio para programar y ejecutar backups automáticos periódicos.
 * Lee la configuración del sistema y ejecuta backups según la frecuencia configurada.
 * 
 * Nota: En una aplicación JavaFX de escritorio, los backups solo se ejecutan mientras
 * la aplicación está abierta. Para backups con la app cerrada, se requeriría un servicio
 * externo o tarea programada del sistema operativo.
 */
@Singleton
public class BackupSchedulerService {
    
    private static final Logger log = LoggerFactory.getLogger(BackupSchedulerService.class);
    
    private final SystemConfigService systemConfigService;
    private final TenantBackupService tenantBackupService;
    private final CompanyService companyService;
    
    private ScheduledExecutorService scheduler;
    private boolean isScheduled = false;
    
    @Inject
    public BackupSchedulerService(
            SystemConfigService systemConfigService,
            TenantBackupService tenantBackupService,
            CompanyService companyService) {
        this.systemConfigService = systemConfigService;
        this.tenantBackupService = tenantBackupService;
        this.companyService = companyService;
    }
    
    /**
     * Inicia el scheduler de backups automáticos.
     * Lee la configuración del sistema y programa la tarea si autoBackupEnabled es true.
     */
    public void startScheduler() {
        log.info("Iniciando scheduler de backups automáticos");
        
        try {
            SystemConfiguration config = systemConfigService.getCurrentConfig();
            
            if (config == null || !Boolean.TRUE.equals(config.getAutoBackupEnabled())) {
                log.info("Backups automáticos deshabilitados en la configuración");
                isScheduled = false;
                return;
            }
            
            Integer frequencyDays = config.getBackupFrequencyDays();
            if (frequencyDays == null || frequencyDays <= 0) {
                frequencyDays = 7; // Default a 7 días
            }
            
            // Crear scheduler con un solo hilo
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "BackupScheduler");
                thread.setDaemon(true);
                return thread;
            });
            
            // Programar la tarea inicial con un delay de 1 minuto para permitir que la app termine de iniciar
            long initialDelayMinutes = 1;
            long periodMinutes = TimeUnit.DAYS.toMinutes(frequencyDays);
            
            scheduler.scheduleAtFixedRate(
                this::executeScheduledBackup,
                initialDelayMinutes,
                periodMinutes,
                TimeUnit.MINUTES
            );
            
            isScheduled = true;
            log.info("Scheduler de backups iniciado: frecuencia = {} días", frequencyDays);
            
        } catch (Exception e) {
            log.error("Error al iniciar el scheduler de backups: {}", e.getMessage(), e);
            isScheduled = false;
        }
    }
    
    /**
     * Detiene el scheduler de backups limpiamente en un hilo separado para no bloquear la UI.
     */
    public void stopScheduler() {
        log.info("Deteniendo scheduler de backups automáticos");
        
        if (scheduler != null && !scheduler.isShutdown()) {
            // Capturar la referencia actual para no afectar a un scheduler recreado
            final ScheduledExecutorService current = scheduler;
            scheduler = null;
            // Ejecutar en un hilo separado para no bloquear el hilo de la UI
            Thread shutdownThread = new Thread(() -> {
                current.shutdown();
                try {
                    // Esperar hasta 30 segundos a que las tareas pendientes terminen
                    if (!current.awaitTermination(30, TimeUnit.SECONDS)) {
                        log.warn("El scheduler no terminó limpiamente, forzando shutdown");
                        current.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    log.warn("Interrumpido mientras esperaba el shutdown del scheduler");
                    current.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }, "BackupScheduler-Shutdown");
            
            shutdownThread.setDaemon(true);
            shutdownThread.start();
        }
        
        isScheduled = false;
        log.info("Scheduler de backups detenido");
    }
    
    /**
     * Re-evalúa la configuración y reprograma el scheduler si es necesario.
     * Debe llamarse cuando el usuario cambia la configuración de backups.
     */
    public void reconfigureScheduler() {
        log.info("Re-evaluando configuración de backups");
        
        // Detener scheduler actual si existe
        if (isScheduled) {
            stopScheduler();
        }
        
        // Iniciar nuevo scheduler con la configuración actualizada
        startScheduler();
    }
    
    /**
     * Ejecuta el backup programado para todas las compañías activas.
     * Este método es invocado periódicamente por el scheduler.
     */
    private void executeScheduledBackup() {
        log.info("Ejecutando backup automático programado - {}", LocalDateTime.now());
        
        try {
            // Obtener todas las compañías activas
            List<Company> companies = companyService.findAllActive();
            
            if (companies == null || companies.isEmpty()) {
                log.warn("No hay compañías activas para realizar backup");
                return;
            }
            
            SystemConfiguration config = systemConfigService.getCurrentConfig();
            String customBackupPath = config != null ? config.getBackupPath() : null;
            
            for (Company company : companies) {
                try {
                    // Establecer el contexto del tenant
                    TenantContext.setCurrentTenant(company);
                    
                    log.info("Realizando backup de la compañía: {} (ID: {})", 
                             company.getName(), company.getId());
                    
                    String backupFilePath;
                    
                    if (customBackupPath != null && !customBackupPath.trim().isEmpty()) {
                        // Usar ruta personalizada de la configuración
                        backupFilePath = performBackupWithPath(customBackupPath, company.getId());
                    } else {
                        // Usar ruta por defecto del TenantBackupService
                        backupFilePath = tenantBackupService.backupCurrentTenant();
                    }
                    
                    log.info("Backup completado exitosamente para {}: {}", 
                             company.getName(), backupFilePath);
                    
                } catch (Exception e) {
                    log.error("Error realizando backup de la compañía {}: {}", 
                              company.getName(), e.getMessage(), e);
                } finally {
                    // Limpiar el contexto del tenant
                    TenantContext.clear();
                }
            }
            
            log.info("Backup automático programado finalizado");
            
        } catch (Exception e) {
            log.error("Error general en el backup automático: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Realiza un backup usando una ruta personalizada.
     */
    private String performBackupWithPath(String customPath, Long companyId) throws IOException, SQLException {
        // Crear directorio si no existe
        Path backupDir = Paths.get(customPath);
        if (!java.nio.file.Files.exists(backupDir)) {
            java.nio.file.Files.createDirectories(backupDir);
            log.debug("Directorio de backup creado: {}", backupDir);
        }
        
        // El TenantBackupService usa su propio BACKUP_DIR hardcodeado
        // Para usar una ruta custom, necesitamos modificar temporalmente
        // o crear un método alternativo. Por simplicidad, usamos el método
        // existente y luego movemos el archivo si es necesario.
        
        String originalBackupPath = tenantBackupService.backupCurrentTenant();
        
        // Mover el archivo a la ruta personalizada si es diferente
        Path originalPath = Paths.get(originalBackupPath);
        Path newPath = backupDir.resolve(originalPath.getFileName());
        
        if (!backupDir.equals(originalPath.getParent())) {
            java.nio.file.Files.move(originalPath, newPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            log.debug("Backup movido de {} a {}", originalPath, newPath);
            return newPath.toString();
        }
        
        return originalBackupPath;
    }
    
    /**
     * Verifica si el scheduler está actualmente activo.
     */
    public boolean isSchedulerActive() {
        return isScheduled && scheduler != null && !scheduler.isShutdown();
    }
}
