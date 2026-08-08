package com.econovafx.modules.core.config;

import com.econovafx.modules.core.model.Company;
import io.ebean.Database;
import io.ebean.DatabaseBuilder;
import io.ebean.config.ClassLoadConfig;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.config.TenantDataSourceProvider;
import io.ebean.config.TenantMode;
import io.ebean.datasource.DataSourceConfig;
import io.ebean.datasource.DataSourceFactory;
import io.ebean.datasource.DataSourcePool;
import io.ebean.platform.h2.H2Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuración de base de datos con soporte multi-tenant nativo de Ebean. Usa
 * TenantMode.DB con CurrentTenantProvider y TenantDataSourceProvider para
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

    // Variables para testing
    public static boolean closeTenantDataSourceCalled = false;
    public static Long lastClosedTenantId = null;

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

    /**
     * Inicializa la configuración para tests aislados. Usa una base de datos en
     * memoria separada para evitar conflictos. Deshabilita DDL automático para
     * evitar errores de sintaxis en H2. Registra los paquetes de entidades
     * correctamente.
     */
    public static void initializeForTest() {
        // Solo inicializar master si no está ya inicializado
        if (masterDatabase == null) {
            DataSourcePool dataSource = DataSourcePool.builder()
                    .name("econova-test-master")
                    .url("jdbc:h2:mem:econova-test-master;DB_CLOSE_DELAY=-1;MODE=PostgreSQL")
                    .username("sa")
                    .password("")
                    .build();

            DatabaseBuilder builder = Database.builder();
            builder.name("econova-test-master")
                    .dataSource(dataSource)
                    .defaultDatabase(true)
                    .classLoadConfig(new ClassLoadConfig(Thread.currentThread().getContextClassLoader()))
                    .ddlGenerate(true) // Habilitar generación DDL para tests
                    .ddlRun(true) // Habilitar ejecución DDL para tests
                    .databasePlatform(new H2Platform());

            masterDatabase = builder.build();
            logger.info("Master database initialized for test (with entity packages)");
        }
    }

    public static void initializeMaster() {
        DataSourcePool pool = DataSourcePool.builder()
                .name("econova-master")
                .driver(AppConfig.MASTER_DB_DRIVER)
                .url(AppConfig.MASTER_DB_URL)
                .username(AppConfig.MASTER_DB_USERNAME)
                .password(AppConfig.MASTER_DB_PASSWORD)
                .minConnections(1)
                .maxConnections(10)
                .build();

        DatabaseBuilder builder = Database.builder();
        builder.name("econova-master")
                .dataSource(pool)
                .classLoadConfig(new ClassLoadConfig(Thread.currentThread().getContextClassLoader()))
                .ddlGenerate(true)
                .ddlRun(true)
                .databasePlatform(new H2Platform());
        
        Database masterDb = builder.build();
        masterDatabase = masterDb;
        logger.info("Master database initialized successfully");
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
                    .register(true)
                    .defaultDatabase(true)
                    .tenantMode(TenantMode.DB)
                    .currentTenantProvider(tenantProvider)
                    .tenantDataSourceProvider(dataSourceProvider)
                    .databasePlatform(new H2Platform())
                    .classLoadConfig(new ClassLoadConfig(Thread.currentThread().getContextClassLoader()))
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
     * Obtiene o crea un DataSource para un tenant específico. Ejecuta el DDL
     * automáticamente la primera vez que se crea el DataSource.
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

                String dbName = "econova-tenant-" + company.getCode();
                DataSource dataSource = DataSourceFactory.create(dbName, dsConfig);
                logger.info("DataSource created successfully for: {}", company.getCode());

                // Ejecutar DDL para este tenant la primera vez que se crea el DataSource
                executeDDLForTenant(dataSource, dbName);

                return dataSource;

            } catch (Exception e) {
                logger.error("Failed to create DataSource for {}", company.getCode(), e);
                throw new RuntimeException("DataSource creation failed", e);
            }
        });
    }

    /**
     * Ejecuta el DDL para un tenant específico usando su DataSource. Crea un
     * servidor Ebean temporal, ejecuta el DDL y lo cierra.
     *
     * @param dataSource El DataSource del tenant
     * @param dbName Nombre de la base de datos
     */
    private static void executeDDLForTenant(DataSource dataSource, String dbName) {
        logger.info("Executing DDL for tenant database: {}", dbName);

        try {
            // Crear un servidor Ebean temporal solo para ejecutar el DDL
            DatabaseBuilder builder = Database.builder();
            builder.name(dbName + "-ddl")
                    .dataSource(dataSource)
                    .classLoadConfig(new ClassLoadConfig(Thread.currentThread().getContextClassLoader()))
                    .databasePlatform(new H2Platform())
                    .ddlGenerate(true)
                    .ddlRun(true)
                    .setRegister(false)  // No registrar como servidor global
                    // Registrar todos los paquetes de entidades para que Ebean sepa qué tablas crear
                    .addPackage("com.econovafx.modules.core.model")
                    .addPackage("com.econovafx.modules.accounting.model")
                    .addPackage("com.econovafx.modules.cashbank.model")
                    .addPackage("com.econovafx.modules.security.model");

            Database tempDb = builder.build();
            logger.info("DDL executed successfully for tenant: {}", dbName);

            // Cerrar el servidor temporal inmediatamente después de ejecutar el DDL
            tempDb.shutdown();
            logger.info("Temporary DDL server shutdown for: {}", dbName);

        } catch (Exception e) {
            logger.error("Failed to execute DDL for tenant: {}", dbName, e);
            throw new RuntimeException("DDL execution failed for tenant: " + dbName, e);
        }
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
