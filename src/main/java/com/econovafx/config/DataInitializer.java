package com.econovafx.config;

import com.econovafx.model.Company;
import com.econovafx.model.User;
import com.econovafx.security.PasswordService;
import io.ebean.DB;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inicializador de datos de demostración.
 * Crea un usuario admin y una empresa demo si no existen.
 */
@Singleton
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    public DataInitializer() {
        initialize();
    }

    private void initialize() {
        log.info("Inicializando datos del sistema...");

        PasswordService passwordService = new PasswordService();

        // Crear empresa demo si no existe
        Company demoCompany = DB.find(Company.class)
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
            
            DB.save(demoCompany);
            log.info("Empresa demo creada: {}", demoCompany.getName());
        } else {
            log.info("Empresa demo ya existe: {}", demoCompany.getName());
        }

        // Crear usuario admin si no existe
        User adminUser = DB.find(User.class)
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
            
            DB.save(adminUser);
            log.info("Usuario admin creado: {}", adminUser.getUsername());
        } else {
            log.info("Usuario admin ya existe: {}", adminUser.getUsername());
        }

        log.info("Inicialización completada.");
    }
}
