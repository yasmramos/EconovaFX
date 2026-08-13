package com.econovafx.modules.core.config;

import com.econovafx.modules.core.model.*;
import com.econovafx.modules.core.security.PasswordService;
import io.ebean.Database;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inicializador de datos de demostración.
 * Crea usuario admin y empresa demo.
 */
@Singleton
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private boolean initialized = false;

    public DataInitializer() {
        // Defer initialization to avoid running during DI construction
        // Initialization will be triggered explicitly when needed
        log.info("DataInitializer created, waiting for explicit initialization");
    }

    /**
     * Explicit initialization method to be called after database is fully ready
     */
    public void initialize() {
        if (initialized) {
            log.debug("DataInitializer already initialized, skipping");
            return;
        }
        
        log.info("Inicializando datos del sistema...");

        // Get database instance
        Database db;
        try {
            db = DatabaseConfig.getMasterDatabase();
        } catch (Exception e) {
            log.warn("Skipping DataInitializer - no database available: {}", e.getMessage());
            return;
        }

        PasswordService passwordService = new PasswordService();

        // Crear empresa demo si no existe
        Company demoCompany = db.find(Company.class)
            .where().eq("name", "Empresa Demo")
            .findOne();

        if (demoCompany == null) {
            demoCompany = new Company();
            demoCompany.setName("Empresa Demo");
            demoCompany.setCode("DEMO001");
            demoCompany.setNif("00000000001");
            demoCompany.setAddress("Dirección Demo");
            demoCompany.setPhone("000-000-0000");
            demoCompany.setEmail("demo@econovafx.com");
            demoCompany.setStatus("ACTIVE");
            
            db.save(demoCompany);
            log.info("Empresa demo creada: {}", demoCompany.getName());
        } else {
            log.info("Empresa demo ya existe: {}", demoCompany.getName());
        }

        // Crear usuario admin si no existe
        User adminUser = db.find(User.class)
            .where().eq("email", "admin@econovafx.com")
            .findOne();

        if (adminUser == null) {
            adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@econovafx.com");
            adminUser.setFullName("Administrador");
            adminUser.setPassword(passwordService.hashPassword("admin"));
            adminUser.setRole(User.UserRole.ADMIN);
            adminUser.setCompany(demoCompany);
            adminUser.setStatus("ACTIVE");
            
            db.save(adminUser);
            log.info("Usuario admin creado: {}", adminUser.getUsername());
        } else {
            log.info("Usuario admin ya existe: {}", adminUser.getUsername());
        }

        initialized = true;
        log.info("Inicialización completada.");
    }
}
