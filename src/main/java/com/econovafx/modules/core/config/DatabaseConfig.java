package com.econovafx.modules.core.config;

import com.econovafx.modules.core.model.Company;
import io.ebean.Database;
import io.ebean.DatabaseBuilder;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.config.TenantDataSourceProvider;
import io.ebean.config.TenantMode;
import io.ebean.datasource.DataSourceConfig;
import io.ebean.datasource.DataSourceFactory;
import io.ebean.platform.h2.H2Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuración de base de datos con soporte multi-tenant nativo de Ebean.
 * Usa TenantMode.DB con CurrentTenantProvider y TenantDataSourceProvider para
 * gestionar bases de datos separadas por tenant de forma nativa en Ebean.
 */
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    
    // Base de datos maestra (gestión de empresas)
    private static Database masterDatabase;
    
    // Base de datos multi-tenant configurada nativamente
    private static Database tenantDatabase;
    
    // Cache de DataSources por empresa (tenant)
    private static final ConcurrentHashMap<Long, DataSource> tenantDataSources = new ConcurrentHashMap<>();

    /**
     * Inicializa la configuración multi-tenant nativa de Ebean.
     */
    public static void initialize() {
        initializeMaster();
        initializeMultiTenant();
    }

    /**
     * Inicializa solo la base de datos maestra, sin multi-tenant.
     * Útil para tests que no requieren aislamiento de tenants.
     */
    public static void initializeMasterOnly() {
        initializeMaster();
    }
    
    /**
     * Inicializa la configuración para tests aislados.
     * Usa una base de datos en memoria separada para evitar conflictos.
     * Deshabilita DDL automático para evitar errores de sintaxis en H2.
     */
    public static void initializeForTest() {
        // Solo inicializar master si no está ya inicializado
        if (masterDatabase == null) {
            DataSourceConfig config = new DataSourceConfig();
            config.setUsername("sa");
            config.setPassword("");
            config.setUrl("jdbc:h2:mem:econova-test-master;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
            
            io.ebean.config.DatabaseConfig dbConfig = new io.ebean.config.DatabaseConfig();
            dbConfig.setName("econova-master");
            dbConfig.setDataSourceConfig(config);
            dbConfig.setDefaultServer(true);
            dbConfig.setDdlGenerate(false);  // Deshabilitar generación DDL
            dbConfig.setDdlRun(false);       // Deshabilitar ejecución DDL
            
            masterDatabase = io.ebean.DatabaseFactory.create(dbConfig);
            logger.info("Master database initialized for test (DDL disabled)");
        }
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
                .addPackage("com.econovafx.modules.core.model")
                .addPackage("com.econovafx.modules.accounting.model")
                .addPackage("com.econovafx.modules.billing.model")
                .addPackage("com.econovafx.modules.bank.model")
                .addPackage("com.econovafx.modules.cash.model")
                .addPackage("com.econovafx.modules.inventory.model")
                .addPackage("com.econovafx.modules.fixedassets.model")
                .ddlGenerate(true)
                .ddlRun(true)
                .databasePlatform(new H2Platform());
            
            Database masterDb = builder.build();
            
            masterDatabase = masterDb;

            logger.info("Master database initialized successfully");

        } catch (IOException e) {
            logger.error("Failed to initialize master database", e);
            throw new RuntimeException("Master database initialization failed", e);
        }
    }

    /**
     * Inicializa la base de datos multi-tenant usando configuración nativa de Ebean.
     * Configura TenantMode.DB con CurrentTenantProvider y TenantDataSourceProvider.
     */
    public static void initializeMultiTenant() {
        try {
            // CurrentTenantProvider: obtiene el tenant actual del contexto
            CurrentTenantProvider tenantProvider = () -> {
                Company currentTenant = TenantContext.getCurrentTenant();
                return currentTenant != null ? currentTenant.getId() : null;
            };

            // TenantDataSourceProvider: obtiene el DataSource según el tenant
            TenantDataSourceProvider dataSourceProvider = tenantId -> {
                if (tenantId == null) {
                    throw new IllegalStateException("No tenant ID provided");
                }
                Long companyId = (Long) tenantId;
                return getOrCreateDataSource(companyId);
            };

            DatabaseBuilder builder = Database.builder();
            builder.name("econova-multi-tenant")
                .loadFromProperties()
                .setRegister(true)
                .setDefaultServer(true)
                .setTenantMode(TenantMode.DB)
                .setCurrentTenantProvider(tenantProvider)
                .setTenantDataSourceProvider(dataSourceProvider)
                .setDatabasePlatform(new H2Platform())
                .addPackage("com.econovafx.modules.core.model")
                .addPackage("com.econovafx.modules.accounting.model")
                .addPackage("com.econovafx.modules.billing.model")
                .addPackage("com.econovafx.modules.bank.model")
                .addPackage("com.econovafx.modules.cash.model")
                .addPackage("com.econovafx.modules.inventory.model")
                .addPackage("com.econovafx.modules.fixedassets.model")
                .ddlGenerate(true)
                .ddlRun(true);
            
            tenantDatabase = builder.build();
            
            logger.info("Multi-tenant database initialized successfully with TenantMode.DB");

        } catch (Exception e) {
            logger.error("Failed to initialize multi-tenant database", e);
            throw new RuntimeException("Multi-tenant database initialization failed", e);
        }
    }

    /**
     * Obtiene o crea un DataSource para un tenant específico.
     * @param companyId ID de la empresa
     * @return DataSource configurado para el tenant
     */
    private static DataSource getOrCreateDataSource(Long companyId) {
        return tenantDataSources.computeIfAbsent(companyId, id -> {
            Company company = getCompanyById(id);
            if (company == null) {
                throw new RuntimeException("Company not found for ID: " + id);
            }
            
            logger.info("Creating DataSource for tenant: {} ({})", company.getName(), company.getCode());
            
            try {
                DataSourceConfig dsConfig = new DataSourceConfig();
                dsConfig.setDriver("org.h2.Driver");
                dsConfig.setUrl(company.getDatabaseUrl());
                
                if (company.getDatabaseUser() != null && !company.getDatabaseUser().isEmpty()) {
                    dsConfig.setUsername(company.getDatabaseUser());
                    dsConfig.setPassword("");
                } else {
                    dsConfig.setUsername("sa");
                    dsConfig.setPassword("");
                }
                
                dsConfig.setMinConnections(1);
                dsConfig.setMaxConnections(10);

                DataSource dataSource = DataSourceFactory.create("econova-tenant-" + company.getCode(), dsConfig);
                logger.info("DataSource created successfully for: {}", company.getCode());
                return dataSource;

            } catch (Exception e) {
                logger.error("Failed to create DataSource for {}", company.getCode(), e);
                throw new RuntimeException("DataSource creation failed", e);
            }
        });
    }

    /**
     * Obtiene una empresa por su ID desde la base de datos maestra.
     * @param companyId ID de la empresa
     * @return La empresa o null si no existe
     */
    private static Company getCompanyById(Long companyId) {
        if (masterDatabase == null) {
            initializeMaster();
        }
        return masterDatabase.find(Company.class, companyId);
    }

    /**
     * Cambia el contexto al tenant especificado.
     * Ebean automáticamente usará el DataSource correcto vía TenantDataSourceProvider.
     * @param company La empresa a establecer como tenant activo
     */
    public static void switchToTenant(Company company) {
        TenantContext.setCurrentTenant(company);
        logger.debug("Switched to tenant: {} ({})", company.getCode(), company.getId());
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
     * Obtiene la base de datos multi-tenant.
     * @return La base de datos multi-tenant
     */
    public static Database getTenantDatabase() {
        if (tenantDatabase == null) {
            initializeMultiTenant();
        }
        return tenantDatabase;
    }

    /**
     * Obtiene el servidor de base de datos por defecto.
     * Usa la base de datos maestra si no hay multi-tenant inicializado.
     * @return La base de datos por defecto
     */
    public static Database getServer() {
        // Return master database if tenant database is not initialized
        if (tenantDatabase == null && masterDatabase != null) {
            return masterDatabase;
        }
        return getTenantDatabase();
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
     * Cierra el DataSource de un tenant específico.
     * @param companyId ID de la empresa
     */
    public static void closeTenantDataSource(Long companyId) {
        DataSource ds = tenantDataSources.remove(companyId);
        if (ds != null) {
            logger.info("DataSource closed for company ID: {}", companyId);
        }
    }

    /**
     * Cierra todas las conexiones de base de datos.
     */
    public static void shutdown() {
        // Cerrar base de datos multi-tenant
        if (tenantDatabase != null) {
            tenantDatabase.shutdown();
            logger.info("Multi-tenant database shutdown complete");
        }
        
        // Cerrar base de datos maestra
        if (masterDatabase != null) {
            masterDatabase.shutdown();
            logger.info("Master database shutdown complete");
        }
        
        // Limpiar cache de DataSources
        tenantDataSources.clear();
        
        logger.info("All databases and DataSources shutdown complete");
    }
}
