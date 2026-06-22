package com.econovafx.config;

import com.econovafx.domain.Company;
import io.ebean.Database;
import io.ebean.DatabaseBuilder;
import io.ebean.DatabaseFactory;
import io.ebean.datasource.DataSourceConfig;
import io.ebean.datasource.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuración de base de datos con soporte multi-tenant.
 * Gestiona una base de datos maestra para empresas y bases de datos separadas por tenant.
 */
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    
    // Base de datos maestra (gestión de empresas)
    private static Database masterDatabase;
    
    // Cache de bases de datos por empresa (tenant)
    private static final ConcurrentHashMap<Long, Database> tenantDatabases = new ConcurrentHashMap<>();

    /**
     * Inicializa la base de datos maestra para gestión de empresas.
     */
    public static void initialize() {
        initializeMaster();
    }

    public static void initializeMaster() {
        try {
            Properties props = loadProperties();

            DataSourceConfig dsConfig = new DataSourceConfig();
            dsConfig.setDriver(props.getProperty("ebean.datasource.master.driver", "org.h2.Driver"));
            dsConfig.setUrl(props.getProperty("ebean.datasource.master.url",
                    "jdbc:h2:./db/econova_master;DB_CLOSE_DELAY=-1"));
            dsConfig.setUsername(props.getProperty("ebean.datasource.master.username", "sa"));
            dsConfig.setPassword(props.getProperty("ebean.datasource.master.password", ""));
            dsConfig.setMinConnections(1);
            dsConfig.setMaxConnections(10);

            DataSource dataSource = DataSourceFactory.create("econova-master", dsConfig);

            DatabaseBuilder builder = Database.builder();
            builder.name("econova-master")
                .dataSource(dataSource)
                .addPackage("com.econovafx.domain")
                .ddlGenerate(true)
                .ddlRun(true)
                .setDefaultServer(true)
                .databasePlatform(new io.ebean.platform.h2.H2Platform());
            
            Database masterDb = builder.build();
            
            masterDatabase = masterDb;

            logger.info("Master database initialized successfully");

        } catch (IOException e) {
            logger.error("Failed to initialize master database", e);
            throw new RuntimeException("Master database initialization failed", e);
        }
    }

    /**
     * Inicializa o recupera la base de datos de un tenant específico.
     * @param company La empresa (tenant) para la cual inicializar la BD
     * @return La instancia de Database para el tenant
     */
    public static Database getOrCreateTenantDatabase(Company company) {
        return tenantDatabases.computeIfAbsent(company.getId(), id -> {
            logger.info("Creating database for tenant: {} ({})", company.getName(), company.getCode());
            
            try {
                DataSourceConfig dsConfig = new DataSourceConfig();
                dsConfig.setDriver("org.h2.Driver");
                dsConfig.setUrl(company.getDatabaseUrl());
                
                if (company.getDatabaseUser() != null && !company.getDatabaseUser().isEmpty()) {
                    dsConfig.setUsername(company.getDatabaseUser());
                    dsConfig.setPassword(""); // Se puede mejorar con encriptación
                } else {
                    dsConfig.setUsername("sa");
                    dsConfig.setPassword("");
                }
                
                dsConfig.setMinConnections(1);
                dsConfig.setMaxConnections(10);

                DataSource dataSource = DataSourceFactory.create("econova-tenant-" + company.getCode(), dsConfig);

                DatabaseBuilder builder = Database.builder();
                builder.name("econova-tenant-" + company.getCode())
                    .dataSource(dataSource)
                    .addPackage("com.econovafx.domain")
                    .addPackage("com.econovafx.model")
                    .ddlGenerate(true)
                    .ddlRun(true);
                
                Database tenantDb = builder.build();
                    
                logger.info("Tenant database created successfully for: {}", company.getCode());
                return tenantDb;

            } catch (Exception e) {
                logger.error("Failed to create tenant database for {}", company.getCode(), e);
                throw new RuntimeException("Tenant database creation failed", e);
            }
        });
    }

    /**
     * Obtiene la base de datos del tenant actual desde el contexto.
     * @return La base de datos del tenant activo
     * @throws IllegalStateException si no hay un tenant configurado
     */
    public static Database getCurrentTenantDatabase() {
        Company currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null) {
            throw new IllegalStateException("No tenant configured in current context");
        }
        return getOrCreateTenantDatabase(currentTenant);
    }

    /**
     * Obtiene la base de datos maestra.
     * @return La base de datos maestra
     */
    public static Database getMasterDatabase() {
        if (masterDatabase == null) {
            initializeMaster();
        }
        return masterDatabase;
    }

    /**
     * Obtiene el servidor de base de datos por defecto.
     * @return La base de datos por defecto
     */
    public static Database getServer() {
        return getMasterDatabase();
    }

    private static Properties loadProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream input = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("ebean.properties")) {
            if (input != null) {
                props.load(input);
            }
        }
        return props;
    }

    /**
     * Cierra la base de datos de un tenant específico.
     * @param companyId ID de la empresa
     */
    public static void closeTenantDatabase(Long companyId) {
        Database db = tenantDatabases.remove(companyId);
        if (db != null) {
            db.shutdown();
            logger.info("Tenant database closed for company ID: {}", companyId);
        }
    }

    /**
     * Cierra todas las conexiones de base de datos.
     */
    public static void shutdown() {
        // Cerrar todas las bases de datos de tenants
        tenantDatabases.values().forEach(Database::shutdown);
        tenantDatabases.clear();
        
        // Cerrar base de datos maestra
        if (masterDatabase != null) {
            masterDatabase.shutdown();
            logger.info("Master database shutdown complete");
        }
        
        logger.info("All databases shutdown complete");
    }
}
