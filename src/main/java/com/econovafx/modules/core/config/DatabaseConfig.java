package com.econovafx.modules.core.config;

import com.econovafx.modules.core.model.Company;
import io.ebean.Database;
import io.ebean.DatabaseBuilder;
import io.ebean.config.ClassLoadConfig;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.config.TenantDataSourceProvider;
import io.ebean.config.TenantMode;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.datasource.DataSourceConfig;
import io.ebean.datasource.DataSourceFactory;
import io.ebean.datasource.DataSourcePool;
import io.ebean.platform.h2.H2Platform;
import io.ebean.platform.postgres.PostgresPlatform;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuración de base de datos con soporte multi-tenant nativo de Ebean. Usa
 * TenantMode.DB con CurrentTenantProvider y TenantDataSourceProvider para
 * gestionar bases de datos separadas por tenant de forma nativa en Ebean.
 */
@Singleton
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    // Base de datos maestra (gestión de empresas)
    private static Database masterDatabase;

    // Base de datos multi-tenant configurada nativamente
    private static Database tenantDatabase;

    // Cache de DataSources por empresa (tenant)
    private static final ConcurrentHashMap<Long, DataSource> tenantDataSources = new ConcurrentHashMap<>();

    // Variables para testing
    public static boolean closeTenantDataSourceCalled = false;
    public static Long lastClosedTenantId = null;

    public DatabaseConfig() {
        this.initialize();
    }

    /**
     * Inicializa la configuración multi-tenant nativa de Ebean.
     */
    public static void initialize() {
        initializeMaster();
        initializeMultiTenant();
    }

    /**
     * Inicializa solo la base de datos maestra, sin multi-tenant. Útil para
     * tests que no requieren aislamiento de tenants.
     */
    public static void initializeMasterOnly() {
        initializeMaster();
    }

    public static void initializeMaster() {
        DataSourcePool pool = DataSourcePool.builder()
                .name("master")
                .driver(AppConfig.MASTER_DB_DRIVER)
                .url(AppConfig.MASTER_DB_URL)
                .username(AppConfig.MASTER_DB_USERNAME)
                .password(AppConfig.MASTER_DB_PASSWORD)
                .minConnections(1)
                .maxConnections(10)
                .build();

        DatabaseBuilder builder = Database.builder();
        builder.name("master")
                .dataSource(pool)
                .classLoadConfig(new ClassLoadConfig(Thread.currentThread().getContextClassLoader()))
                .ddlGenerate(true)
                .ddlRun(true)
                .databasePlatform(selectDatabasePlatform(AppConfig.DB_TYPE))
                .defaultDatabase(true);

        Database masterDb = builder.build();
        masterDatabase = masterDb;
        logger.info("Master database initialized successfully with platform: {}", AppConfig.DB_TYPE);
    }
    
    /**
     * Selects the appropriate database platform based on configuration.
     * @param dbType The database type ("h2" or "postgres")
     * @return The appropriate DatabasePlatform instance
     */
    private static DatabasePlatform selectDatabasePlatform(String dbType) {
        if ("postgres".equalsIgnoreCase(dbType)) {
            logger.info("Using PostgreSQL database platform");
            return new PostgresPlatform();
        } else {
            logger.info("Using H2 database platform");
            return new H2Platform();
        }
    }

    /**
     * Inicializa la base de datos multi-tenant usando configuración nativa de
     * Ebean. Configura TenantMode.DB con CurrentTenantProvider y
     * TenantDataSourceProvider.
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
                    .register(false)
                    .tenantMode(TenantMode.DB)
                    .currentTenantProvider(tenantProvider)
                    .tenantDataSourceProvider(dataSourceProvider)
                    .databasePlatform(selectDatabasePlatform(AppConfig.DB_TYPE))
                    .classLoadConfig(new ClassLoadConfig(Thread.currentThread().getContextClassLoader()))
                    .ddlGenerate(true)
                    .ddlRun(true);

            tenantDatabase = builder.build();

            logger.info("Multi-tenant database initialized successfully with TenantMode.DB and platform: {}", AppConfig.DB_TYPE);

        } catch (Exception e) {
            logger.error("Failed to initialize multi-tenant database", e);
            throw new RuntimeException("Multi-tenant database initialization failed", e);
        }
    }

    /**
     * Obtiene o crea un DataSource para un tenant específico.
     *
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
                
                // Determine driver and URL based on database type from config or company
                String dbType = AppConfig.DB_TYPE;
                String driver, url, username, password;
                
                // If company has explicit database URL, use it; otherwise build from config
                if (company.getDatabaseUrl() != null && !company.getDatabaseUrl().isEmpty()) {
                    url = company.getDatabaseUrl();
                    // Infer driver from URL if not explicitly set
                    if (url.startsWith("jdbc:postgresql:")) {
                        driver = "org.postgresql.Driver";
                    } else if (url.startsWith("jdbc:h2:")) {
                        driver = "org.h2.Driver";
                    } else {
                        driver = company.getDatabaseDriver() != null ? company.getDatabaseDriver() : dbType.equals("postgres") ? "org.postgresql.Driver" : "org.h2.Driver";
                    }
                } else {
                    // Build URL from configuration
                    if ("postgres".equalsIgnoreCase(dbType)) {
                        driver = "org.postgresql.Driver";
                        // Derive a per-tenant database name so each company gets its own
                        // PostgreSQL database (required by TenantMode.DB, which has no
                        // tenant_id discriminator column).
                        String tenantDatabaseName = String.format("%s_%s",
                                AppConfig.POSTGRES_DATABASE,
                                company.getCode());
                        url = String.format("jdbc:postgresql://%s:%d/%s?sslmode=%s",
                                AppConfig.POSTGRES_HOST,
                                AppConfig.POSTGRES_PORT,
                                tenantDatabaseName,
                                AppConfig.POSTGRES_SSLMODE);
                    } else {
                        driver = "org.h2.Driver";
                        url = String.format("jdbc:h2:./db/tenant-%s;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE", company.getCode());
                    }
                }
                
                dsConfig.setDriver(driver);
                dsConfig.setUrl(url);

                if (company.getDatabaseUser() != null && !company.getDatabaseUser().isEmpty()) {
                    username = company.getDatabaseUser();
                    // Use company password if available, otherwise use config default
                    password = company.getDatabasePassword() != null && !company.getDatabasePassword().isEmpty() 
                            ? company.getDatabasePassword() 
                            : (driver.contains("postgresql") ? AppConfig.POSTGRES_PASSWORD : "");
                } else {
                    username = driver.contains("postgresql") ? AppConfig.POSTGRES_USERNAME : "sa";
                    password = driver.contains("postgresql") ? AppConfig.POSTGRES_PASSWORD : "";
                }
                
                dsConfig.setUsername(username);
                dsConfig.setPassword(password);

                dsConfig.setMinConnections(1);
                dsConfig.setMaxConnections(10);

                String dbName = "econova-tenant-" + company.getCode();
                DataSource dataSource = DataSourceFactory.create(dbName, dsConfig);
                logger.info("DataSource created successfully for: {} with driver: {}", company.getCode(), driver);

                return dataSource;

            } catch (Exception e) {
                logger.error("Failed to create DataSource for {}", company.getCode(), e);
                throw new RuntimeException("DataSource creation failed", e);
            }
        });
    }

    /**
     * Obtiene una empresa por su ID desde la base de datos maestra.
     *
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
     * Cambia el contexto al tenant especificado. Ebean automáticamente usará el
     * DataSource correcto vía TenantDataSourceProvider.
     *
     * @param company La empresa a establecer como tenant activo
     */
    public static void switchToTenant(Company company) {
        TenantContext.setCurrentTenant(company);
        logger.debug("Switched to tenant: {} ({})", company.getCode(), company.getId());
    }

    /**
     * Obtiene la base de datos maestra.
     *
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
     *
     * @return La base de datos multi-tenant
     */
    public static Database getTenantDatabase() {
        if (tenantDatabase == null) {
            initializeMultiTenant();
        }
        return tenantDatabase;
    }

    /**
     * Obtiene el servidor de base de datos por defecto. Usa la base de datos
     * maestra si no hay multi-tenant inicializado.
     *
     * @return La base de datos por defecto
     */
    public static Database getServer() {
        // Always return master database if tenant database is not available
        if (tenantDatabase == null && masterDatabase != null) {
            return masterDatabase;
        }

        // If tenant database is initialized but no tenant context, use master
        if (tenantDatabase != null && !TenantContext.hasTenant()) {
            return masterDatabase != null ? masterDatabase : getTenantDatabase();
        }

        // If there's a tenant context, use tenant database
        if (tenantDatabase != null && TenantContext.hasTenant()) {
            return tenantDatabase;
        }

        // Fallback: initialize master and return it
        if (masterDatabase == null) {
            initializeMaster();
        }
        return masterDatabase;
    }

    /**
     * Cierra el DataSource de un tenant específico.
     *
     * @param companyId ID de la empresa
     */
    public static void closeTenantDataSource(Long companyId) {
        DataSource ds = tenantDataSources.remove(companyId);
        if (ds != null) {
            logger.info("DataSource closed for company ID: {}", companyId);
        }
        // Para testing
        closeTenantDataSourceCalled = true;
        lastClosedTenantId = companyId;
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
