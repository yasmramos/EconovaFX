package com.econovafx.modules.core.config;

import com.econovafx.modules.core.model.*;
import com.econovafx.modules.billing.model.BillingSeries;
import com.econovafx.modules.billing.model.BillingSeries.DocumentType;
import com.econovafx.modules.billing.model.TaxRate;
import com.econovafx.modules.billing.model.ThirdParty;
import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.inventory.model.Warehouse;
import com.econovafx.modules.fixedassets.model.FixedAssetCategory;
import com.econovafx.modules.core.model.ExchangeRate;
import com.econovafx.modules.core.security.PasswordService;
import io.ebean.Database;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * Inicializador de datos de demostración.
 * Crea usuario admin, empresa demo, tasas impositivas y series de facturación.
 */
@Singleton
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    public DataInitializer() {
        initialize();
    }

    private void initialize() {
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

        // Crear tasas impositivas demo
        initializeTaxRates(db);

        // Crear series de facturación demo
        initializeBillingSeries(db);

        // Crear categorías de activos fijos demo
        initializeFixedAssetCategories(db);

        log.info("Inicialización completada.");
    }

    /**
     * Inicializa tasas impositivas de demostración.
     */
    private void initializeTaxRates(Database db) {
        // Verificar si ya existen tasas impositivas
        long taxRateCount = db.find(TaxRate.class).findCount();
        if (taxRateCount > 0) {
            log.info("Tasas impositivas ya existen: {}", taxRateCount);
            return;
        }

        // IVA 18% (Nicaragua)
        TaxRate iva18 = new TaxRate();
        iva18.setCode("IVA18");
        iva18.setName("IVA 18%");
        iva18.setPercentage(new BigDecimal("18.00"));
        iva18.setDescription("Impuesto al Valor Agregado - Tasa General");
        iva18.setAccountCode("203-001"); // IVA por pagar
        iva18.setActive(true);
        db.save(iva18);
        log.info("Tasa impositiva creada: IVA 18%");

        // IVA 0% (Exento)
        TaxRate iva0 = new TaxRate();
        iva0.setCode("IVA0");
        iva0.setName("IVA 0%");
        iva0.setPercentage(BigDecimal.ZERO);
        iva0.setDescription("Operaciones exentas de IVA");
        iva0.setAccountCode(null);
        iva0.setActive(true);
        db.save(iva0);
        log.info("Tasa impositiva creada: IVA 0%");

        // ISC 5% (Impuesto Selectivo al Consumo)
        TaxRate isc5 = new TaxRate();
        isc5.setCode("ISC5");
        isc5.setName("ISC 5%");
        isc5.setPercentage(new BigDecimal("5.00"));
        isc5.setDescription("Impuesto Selectivo al Consumo");
        isc5.setAccountCode("203-002"); // ISC por pagar
        isc5.setActive(true);
        db.save(isc5);
        log.info("Tasa impositiva creada: ISC 5%");
    }

    /**
     * Inicializa series de facturación de demostración.
     */
    private void initializeBillingSeries(Database db) {
        // Verificar si ya existen series
        long seriesCount = db.find(BillingSeries.class).findCount();
        if (seriesCount > 0) {
            log.info("Series de facturación ya existen: {}", seriesCount);
            return;
        }

        // Serie A - Facturas de Crédito Fiscal
        BillingSeries serieA = new BillingSeries();
        serieA.setSeriesCode("A");
        serieA.setDocumentType(DocumentType.INVOICE);
        serieA.setDescription("Facturas de Crédito Fiscal");
        serieA.setStartNumber(1);
        serieA.setCurrentNumber(1);
        serieA.setEndNumber(1000);
        serieA.setActive(true);
        db.save(serieA);
        log.info("Serie de facturación creada: A (Facturas)");

        // Serie B - Facturas de Consumidor Final
        BillingSeries serieB = new BillingSeries();
        serieB.setSeriesCode("B");
        serieB.setDocumentType(DocumentType.INVOICE);
        serieB.setDescription("Facturas de Consumidor Final");
        serieB.setStartNumber(1);
        serieB.setCurrentNumber(1);
        serieB.setEndNumber(2000);
        serieB.setActive(true);
        db.save(serieB);
        log.info("Serie de facturación creada: B (Consumidor Final)");

        // Serie NC - Notas de Crédito
        BillingSeries serieNC = new BillingSeries();
        serieNC.setSeriesCode("NC");
        serieNC.setDocumentType(DocumentType.CREDIT_NOTE);
        serieNC.setDescription("Notas de Crédito");
        serieNC.setStartNumber(1);
        serieNC.setCurrentNumber(1);
        serieNC.setEndNumber(500);
        serieNC.setActive(true);
        db.save(serieNC);
        log.info("Serie de facturación creada: NC (Notas de Crédito)");

        // Serie ND - Notas de Débito
        BillingSeries serieND = new BillingSeries();
        serieND.setSeriesCode("ND");
        serieND.setDocumentType(DocumentType.DEBIT_NOTE);
        serieND.setDescription("Notas de Débito");
        serieND.setStartNumber(1);
        serieND.setCurrentNumber(1);
        serieND.setEndNumber(500);
        serieND.setActive(true);
        db.save(serieND);
        log.info("Serie de facturación creada: ND (Notas de Débito)");

        // Serie R - Recibos
        BillingSeries serieR = new BillingSeries();
        serieR.setSeriesCode("R");
        serieR.setDocumentType(DocumentType.RECEIPT);
        serieR.setDescription("Recibos de Ingreso");
        serieR.setStartNumber(1);
        serieR.setCurrentNumber(1);
        serieR.setEndNumber(3000);
        serieR.setActive(true);
        db.save(serieR);
        log.info("Serie de facturación creada: R (Recibos)");
    }

    /**
     * Inicializa categorías de activos fijos de demostración.
     */
    private void initializeFixedAssetCategories(Database db) {
        // Verificar si ya existen categorías
        long categoryCount = db.find(FixedAssetCategory.class).findCount();
        if (categoryCount > 0) {
            log.info("Categorías de activos fijos ya existen: {}", categoryCount);
            return;
        }

        // Vehículos
        FixedAssetCategory vehiculos = new FixedAssetCategory();
        vehiculos.setCode("VEH");
        vehiculos.setName("Vehículos");
        vehiculos.setDescription("Automóviles, camiones, motocicletas");
        vehiculos.setUsefulLifeYears(5);
        vehiculos.setDepreciationRate(new BigDecimal("20.00"));
        vehiculos.setAssetAccount("105-001");
        vehiculos.setAccumulatedDepreciationAccount("105-099");
        vehiculos.setDepreciationExpenseAccount("502-001");
        vehiculos.setActive(true);
        db.save(vehiculos);
        log.info("Categoría de activo fijo creada: Vehículos");

        // Equipos de Oficina
        FixedAssetCategory equiposOficina = new FixedAssetCategory();
        equiposOficina.setCode("EQO");
        equiposOficina.setName("Equipos de Oficina");
        equiposOficina.setDescription("Computadoras, impresoras, muebles");
        equiposOficina.setUsefulLifeYears(4);
        equiposOficina.setDepreciationRate(new BigDecimal("25.00"));
        equiposOficina.setAssetAccount("106-001");
        equiposOficina.setAccumulatedDepreciationAccount("106-099");
        equiposOficina.setDepreciationExpenseAccount("502-002");
        equiposOficina.setActive(true);
        db.save(equiposOficina);
        log.info("Categoría de activo fijo creada: Equipos de Oficina");

        // Maquinaria
        FixedAssetCategory maquinaria = new FixedAssetCategory();
        maquinaria.setCode("MAQ");
        maquinaria.setName("Maquinaria Industrial");
        maquinaria.setDescription("Máquinas y equipos de producción");
        maquinaria.setUsefulLifeYears(10);
        maquinaria.setDepreciationRate(new BigDecimal("10.00"));
        maquinaria.setAssetAccount("104-001");
        maquinaria.setAccumulatedDepreciationAccount("104-099");
        maquinaria.setDepreciationExpenseAccount("501-001");
        maquinaria.setActive(true);
        db.save(maquinaria);
        log.info("Categoría de activo fijo creada: Maquinaria Industrial");

        // Edificios
        FixedAssetCategory edificios = new FixedAssetCategory();
        edificios.setCode("EDI");
        edificios.setName("Edificios y Construcciones");
        edificios.setDescription("Construcciones, mejoras permanentes");
        edificios.setUsefulLifeYears(20);
        edificios.setDepreciationRate(new BigDecimal("5.00"));
        edificios.setAssetAccount("103-001");
        edificios.setAccumulatedDepreciationAccount("103-099");
        edificios.setDepreciationExpenseAccount("502-003");
        edificios.setActive(true);
        db.save(edificios);
        log.info("Categoría de activo fijo creada: Edificios y Construcciones");
    }
}
