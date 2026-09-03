package com.econovafx.modules.core.config;

import com.econovafx.modules.accounting.model.FinancialStatementModel;
import com.econovafx.modules.accounting.model.FinancialStatementRow;
import com.econovafx.modules.accounting.repository.FinancialStatementModelRepository;
import com.econovafx.modules.accounting.repository.FinancialStatementRowRepository;
import com.econovafx.modules.core.model.Company;
import com.econovafx.modules.core.model.Currency;
import com.econovafx.modules.core.model.User;
import com.econovafx.modules.core.repository.CompanyRepository;
import com.econovafx.modules.core.repository.CurrencyRepository;
import com.econovafx.modules.core.repository.UserRepository;
import com.econovafx.modules.core.security.PasswordService;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database seeder for initializing default data on first application startup.
 * Creates default company, currencies, admin user, and financial statement rows if they don't exist.
 */
@Singleton
public class DatabaseSeeder {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CurrencyRepository currencyRepository;
    private final PasswordService passwordService;
    private final FinancialStatementModelRepository financialStatementModelRepository;
    private final FinancialStatementRowRepository financialStatementRowRepository;

    public DatabaseSeeder() {
        this(io.ebean.DB.getDefault());
    }
    
    @jakarta.inject.Inject
    public DatabaseSeeder(io.ebean.Database database) {
        this.userRepository = new UserRepository(database);
        this.companyRepository = new CompanyRepository(database);
        this.currencyRepository = new CurrencyRepository(database);
        this.passwordService = new PasswordService();
        this.financialStatementModelRepository = new FinancialStatementModelRepository(database);
        this.financialStatementRowRepository = new FinancialStatementRowRepository(database);
    }

    /**
     * Seeds the database with initial data if needed.
     */
    public void seed() {
        logger.info("Checking if database seeding is required...");
        
        // First, seed default company in master database
        seedDefaultCompany();
        
        // Switch to tenant context for the DEMO company before seeding tenant-specific data
        Company demoCompany = companyRepository.findByCode("DEMO").orElse(null);
        if (demoCompany != null) {
            DatabaseConfig.switchToTenant(demoCompany);
            logger.info("Switched to tenant: {} for seeding tenant-specific data", demoCompany.getCode());
            
            // Now seed currencies, admin user, and financial statement rows in the tenant database
            seedCurrencies();
            seedAdminUser();
            seedFinancialStatementRows();
        } else {
            logger.warn("Demo company not found, skipping tenant-specific seeding");
        }
        
        logger.info("Database seeding completed successfully");
    }

    /**
     * Seeds base currencies if they don't exist.
     */
    private void seedCurrencies() {
        if (currencyRepository.findAll().isEmpty()) {
            logger.info("Seeding base currencies...");
            
            Currency usd = new Currency("USD", "US Dollar", "$");
            Currency eur = new Currency("EUR", "Euro", "€");
            Currency cup = new Currency("CUP", "Cuban Peso", "$");
            Currency cuc = new Currency("CUC", "Cuban Convertible Peso", "$");
            
            currencyRepository.save(usd);
            currencyRepository.save(eur);
            currencyRepository.save(cup);
            currencyRepository.save(cuc);
            
            logger.info("Created {} base currencies", 4);
        } else {
            logger.debug("Currencies already exist, skipping seeding");
        }
    }

    /**
     * Seeds default company if it doesn't exist.
     */
    private void seedDefaultCompany() {
        if (companyRepository.findAll().isEmpty()) {
            logger.info("Seeding default company...");
            
            Company defaultCompany = new Company(
                "Demo Company",
                "DEMO",
                "000000000"
            );
            defaultCompany.setAddress("123 Demo Street, Demo City");
            defaultCompany.setPhone("+1 555-123-4567");
            defaultCompany.setEmail("demo@econovafx.com");
            defaultCompany.setDatabaseUrl("jdbc:h2:./db/tenants/econova_demo");
            defaultCompany.setStatus("ACTIVE");
            
            companyRepository.save(defaultCompany);
            logger.info("Created default company: DEMO");
        } else {
            logger.debug("Companies already exist, skipping seeding");
        }
    }

    /**
     * Seeds admin user if no users exist.
     */
    private void seedAdminUser() {
        if (userRepository.findAll().isEmpty()) {
            logger.info("Seeding admin user...");
            
            User adminUser = new User("admin", "admin@econovafx.com", "Administrator");
            adminUser.setPassword(passwordService.hashPassword("admin123"));
            adminUser.setRole(User.UserRole.ADMIN);
            adminUser.setStatus("ACTIVE");
            adminUser.setIsActive(true);
            
            // Associate with default company if it exists
            companyRepository.findByCode("DEMO").ifPresent(adminUser::setCompany);
            
            userRepository.save(adminUser);
            logger.info("Created default admin user: admin (email: admin@econovafx.com). "
                + "Please change the default password immediately after first login.");
        } else {
            logger.debug("Users already exist, skipping seeding");
        }
    }

    /**
     * Seeds financial statement rows for Cuban financial statement models if they don't exist.
     * This ensures that the financial statement models (BS-001, IS-001, CF-001) have their
     * row structures defined even if migrations were run separately or in different environments.
     * Idempotent: will not duplicate rows if they already exist.
     */
    private void seedFinancialStatementRows() {
        logger.info("Checking if financial statement rows need seeding...");
        
        // Seed Balance Sheet (BS-001) rows
        seedBalanceSheetRows();
        
        // Seed Income Statement (IS-001) rows
        seedIncomeStatementRows();
        
        // Seed Cash Flow Statement (CF-001) rows
        seedCashFlowStatementRows();
        
        logger.info("Financial statement rows seeding completed");
    }

    /**
     * Seeds rows for Balance General (BS-001) model.
     */
    private void seedBalanceSheetRows() {
        FinancialStatementModel model = financialStatementModelRepository.findByCode("BS-001").orElse(null);
        if (model == null) {
            logger.warn("Balance Sheet model BS-001 not found, skipping row seeding");
            return;
        }
        
        long existingCount = financialStatementRowRepository.countByModelId(model.getId());
        if (existingCount > 0) {
            logger.debug("Balance Sheet rows already exist ({} rows), skipping seeding", existingCount);
            return;
        }
        
        logger.info("Seeding Balance Sheet rows for model BS-001...");
        
        int rowNum = 0;
        // Activo Circulante
        financialStatementRowRepository.save(createRow(model, ++rowNum, "ACTIVO CIRCULANTE", null, 
            FinancialStatementRow.RowType.HEADER, null, 1, true, false, 0));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Efectivo y Equivalentes", null,
            FinancialStatementRow.RowType.DATA, "1.01.*,1.02.*", 1, false, false, 1));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Cuentas por Cobrar", null,
            FinancialStatementRow.RowType.DATA, "1.03.*,1.04.*", 1, false, false, 1));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Inventarios", null,
            FinancialStatementRow.RowType.DATA, "1.05.*", 1, false, false, 1));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Total Activo Circulante", null,
            FinancialStatementRow.RowType.SUBTOTAL, null, 1, true, false, 0));
        
        // Activo No Circulante
        financialStatementRowRepository.save(createRow(model, ++rowNum, "ACTIVO NO CIRCULANTE", null,
            FinancialStatementRow.RowType.HEADER, null, 1, true, false, 0));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Propiedades, Planta y Equipo", null,
            FinancialStatementRow.RowType.DATA, "1.06.*", 1, false, false, 1));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Activos Intangibles", null,
            FinancialStatementRow.RowType.DATA, "1.07.*", 1, false, false, 1));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Total Activo No Circulante", null,
            FinancialStatementRow.RowType.SUBTOTAL, null, 1, true, false, 0));
        
        // Total Activo
        financialStatementRowRepository.save(createRow(model, ++rowNum, "TOTAL ACTIVO", null,
            FinancialStatementRow.RowType.TOTAL, null, 1, true, true, 0));
        
        // Pasivo Circulante
        financialStatementRowRepository.save(createRow(model, ++rowNum, "PASIVO CIRCULANTE", null,
            FinancialStatementRow.RowType.HEADER, null, 1, true, false, 0));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Cuentas por Pagar", null,
            FinancialStatementRow.RowType.DATA, "2.01.*", -1, false, false, 1));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Obligaciones a Corto Plazo", null,
            FinancialStatementRow.RowType.DATA, "2.02.*", -1, false, false, 1));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Total Pasivo Circulante", null,
            FinancialStatementRow.RowType.SUBTOTAL, null, -1, true, false, 0));
        
        // Pasivo No Circulante
        financialStatementRowRepository.save(createRow(model, ++rowNum, "PASIVO NO CIRCULANTE", null,
            FinancialStatementRow.RowType.HEADER, null, 1, true, false, 0));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Obligaciones a Largo Plazo", null,
            FinancialStatementRow.RowType.DATA, "2.03.*", -1, false, false, 1));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Total Pasivo No Circulante", null,
            FinancialStatementRow.RowType.SUBTOTAL, null, -1, true, false, 0));
        
        // Total Pasivo
        financialStatementRowRepository.save(createRow(model, ++rowNum, "TOTAL PASIVO", null,
            FinancialStatementRow.RowType.TOTAL, null, -1, true, true, 0));
        
        // Patrimonio
        financialStatementRowRepository.save(createRow(model, ++rowNum, "PATRIMONIO", null,
            FinancialStatementRow.RowType.HEADER, null, 1, true, false, 0));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Capital Social", null,
            FinancialStatementRow.RowType.DATA, "3.01.*", -1, false, false, 1));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Resultados Acumulados", null,
            FinancialStatementRow.RowType.DATA, "3.02.*", -1, false, false, 1));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Total Patrimonio", null,
            FinancialStatementRow.RowType.SUBTOTAL, null, -1, true, false, 0));
        
        // Total Pasivo + Patrimonio
        financialStatementRowRepository.save(createRow(model, ++rowNum, "TOTAL PASIVO Y PATRIMONIO", null,
            FinancialStatementRow.RowType.TOTAL, null, -1, true, true, 0));
        
        logger.info("Created {} Balance Sheet rows", rowNum);
    }

    /**
     * Seeds rows for Estado de Resultados (IS-001) model.
     */
    private void seedIncomeStatementRows() {
        FinancialStatementModel model = financialStatementModelRepository.findByCode("IS-001").orElse(null);
        if (model == null) {
            logger.warn("Income Statement model IS-001 not found, skipping row seeding");
            return;
        }
        
        long existingCount = financialStatementRowRepository.countByModelId(model.getId());
        if (existingCount > 0) {
            logger.debug("Income Statement rows already exist ({} rows), skipping seeding", existingCount);
            return;
        }
        
        logger.info("Seeding Income Statement rows for model IS-001...");
        
        int rowNum = 0;
        // Ingresos
        financialStatementRowRepository.save(createRow(model, ++rowNum, "INGRESOS DE ACTIVIDADES ORDINARIAS", null,
            FinancialStatementRow.RowType.HEADER, null, 1, true, false, 0));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Ventas de Bienes", null,
            FinancialStatementRow.RowType.DATA, "4.01.*", -1, false, false, 1));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Prestación de Servicios", null,
            FinancialStatementRow.RowType.DATA, "4.02.*", -1, false, false, 1));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Total Ingresos", null,
            FinancialStatementRow.RowType.SUBTOTAL, null, -1, true, false, 0));
        
        // Costo de Ventas
        financialStatementRowRepository.save(createRow(model, ++rowNum, "COSTO DE VENTAS", null,
            FinancialStatementRow.RowType.HEADER, null, 1, true, false, 0));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Costo de Mercancías Vendidas", null,
            FinancialStatementRow.RowType.DATA, "5.01.*", 1, false, false, 1));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Utilidad Bruta", null,
            FinancialStatementRow.RowType.SUBTOTAL, null, 1, true, false, 0));
        
        // Gastos de Operación
        financialStatementRowRepository.save(createRow(model, ++rowNum, "GASTOS DE OPERACIÓN", null,
            FinancialStatementRow.RowType.HEADER, null, 1, true, false, 0));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Gastos de Administración", null,
            FinancialStatementRow.RowType.DATA, "5.02.*", 1, false, false, 1));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Gastos de Ventas", null,
            FinancialStatementRow.RowType.DATA, "5.03.*", 1, false, false, 1));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Utilidad en Operaciones", null,
            FinancialStatementRow.RowType.SUBTOTAL, null, 1, true, false, 0));
        
        // Resultado Neto
        financialStatementRowRepository.save(createRow(model, ++rowNum, "RESULTADO NETO DEL EJERCICIO", null,
            FinancialStatementRow.RowType.TOTAL, null, 1, true, true, 0));
        
        logger.info("Created {} Income Statement rows", rowNum);
    }

    /**
     * Seeds rows for Estado de Flujos de Efectivo (CF-001) model.
     * Note: Full cash flow classification requires transaction-level tagging.
     * This provides the basic structure; actual implementation may need enhancement.
     */
    private void seedCashFlowStatementRows() {
        FinancialStatementModel model = financialStatementModelRepository.findByCode("CF-001").orElse(null);
        if (model == null) {
            logger.warn("Cash Flow Statement model CF-001 not found, skipping row seeding");
            return;
        }
        
        long existingCount = financialStatementRowRepository.countByModelId(model.getId());
        if (existingCount > 0) {
            logger.debug("Cash Flow Statement rows already exist ({} rows), skipping seeding", existingCount);
            return;
        }
        
        logger.info("Seeding Cash Flow Statement rows for model CF-001...");
        
        int rowNum = 0;
        // Actividades de Operación
        financialStatementRowRepository.save(createRow(model, ++rowNum, "FLUJOS DE EFECTIVO DE ACTIVIDADES DE OPERACIÓN", null,
            FinancialStatementRow.RowType.HEADER, null, 1, true, false, 0));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Cobros de Clientes", null,
            FinancialStatementRow.RowType.DATA, "1.01.*,4.01.*", 1, false, false, 1));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Pagos a Proveedores", null,
            FinancialStatementRow.RowType.DATA, "2.01.*,5.01.*", -1, false, false, 1));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Pago de Gastos de Operación", null,
            FinancialStatementRow.RowType.DATA, "5.02.*,5.03.*", -1, false, false, 1));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Flujo Neto de Actividades de Operación", null,
            FinancialStatementRow.RowType.SUBTOTAL, null, 1, true, false, 0));
        
        // Actividades de Inversión
        financialStatementRowRepository.save(createRow(model, ++rowNum, "FLUJOS DE EFECTIVO DE ACTIVIDADES DE INVERSIÓN", null,
            FinancialStatementRow.RowType.HEADER, null, 1, true, false, 0));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Compra de Propiedades, Planta y Equipo", null,
            FinancialStatementRow.RowType.DATA, "1.06.*", -1, false, false, 1));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Venta de Activos Fijos", null,
            FinancialStatementRow.RowType.DATA, "1.06.*,4.03.*", 1, false, false, 1));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Flujo Neto de Actividades de Inversión", null,
            FinancialStatementRow.RowType.SUBTOTAL, null, 1, true, false, 0));
        
        // Actividades de Financiamiento
        financialStatementRowRepository.save(createRow(model, ++rowNum, "FLUJOS DE EFECTIVO DE ACTIVIDADES DE FINANCIAMIENTO", null,
            FinancialStatementRow.RowType.HEADER, null, 1, true, false, 0));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Préstamos Recibidos", null,
            FinancialStatementRow.RowType.DATA, "2.03.*", 1, false, false, 1));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Pago de Préstamos", null,
            FinancialStatementRow.RowType.DATA, "2.03.*", -1, false, false, 1));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Aportes de Capital", null,
            FinancialStatementRow.RowType.DATA, "3.01.*", 1, false, false, 1));
        financialStatementRowRepository.save(createRow(model, ++rowNum, "Flujo Neto de Actividades de Financiamiento", null,
            FinancialStatementRow.RowType.SUBTOTAL, null, 1, true, false, 0));
        
        // Incremento Neto
        financialStatementRowRepository.save(createRow(model, ++rowNum, "INCREMENTO NETO EN EFECTIVO Y EQUIVALENTES", null,
            FinancialStatementRow.RowType.TOTAL, null, 1, true, true, 0));
        
        logger.info("Created {} Cash Flow Statement rows", rowNum);
    }

    /**
     * Helper method to create a FinancialStatementRow.
     */
    private FinancialStatementRow createRow(FinancialStatementModel model, int rowNumber, String label,
                                            Long parentRowId, FinancialStatementRow.RowType rowType,
                                            String accountCodesPattern, int signMultiplier,
                                            boolean isBold, boolean isItalic, int indentLevel) {
        FinancialStatementRow row = new FinancialStatementRow();
        row.setModel(model);
        row.setRowNumber(rowNumber);
        row.setLabel(label);
        row.setRowType(rowType);
        row.setAccountCodesPattern(accountCodesPattern);
        row.setSignMultiplier(signMultiplier);
        row.setIsBold(isBold);
        row.setIsItalic(isItalic);
        row.setIndentLevel(indentLevel);
        return row;
    }
}
