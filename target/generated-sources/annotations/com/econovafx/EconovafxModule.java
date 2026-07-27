package com.econovafx;

import io.avaje.inject.BeanScope;
import io.avaje.inject.InjectModule;
import io.avaje.inject.spi.AvajeModule;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.DependencyMeta;
import io.avaje.inject.spi.Generated;
import io.avaje.inject.spi.GenericType;
import java.lang.reflect.Type;
import com.econovafx.core.service.AccountingReportService$DI;
import com.econovafx.modules.accounting.repository.AccountRepository$DI;
import com.econovafx.modules.accounting.repository.AccountingPeriodRepository$DI;
import com.econovafx.modules.accounting.repository.ExchangeDifferenceRepository$DI;
import com.econovafx.modules.accounting.repository.FinancialStatementModelRepository$DI;
import com.econovafx.modules.accounting.repository.FinancialStatementRowRepository$DI;
import com.econovafx.modules.accounting.repository.TransactionRepository$DI;
import com.econovafx.modules.accounting.service.AccountService$DI;
import com.econovafx.modules.accounting.service.AccountingPeriodService$DI;
import com.econovafx.modules.accounting.service.ExchangeDifferenceService$DI;
import com.econovafx.modules.accounting.service.FinancialStatementService$DI;
import com.econovafx.modules.accounting.service.TransactionService$DI;
import com.econovafx.modules.bank.service.BankAccountService$DI;
import com.econovafx.modules.bank.service.BankReconciliationService$DI;
import com.econovafx.modules.billing.repository.BillingSeriesRepository$DI;
import com.econovafx.modules.billing.repository.SalesInvoiceRepository$DI;
import com.econovafx.modules.billing.repository.TaxRateRepository$DI;
import com.econovafx.modules.billing.repository.ThirdPartyRepository$DI;
import com.econovafx.modules.billing.service.BillingService$DI;
import com.econovafx.modules.billing.service.SequentialNumberService$DI;
import com.econovafx.modules.billing.service.ThirdPartyService$DI;
import com.econovafx.modules.cash.service.CashMovementService$DI;
import com.econovafx.modules.core.config.DataInitializer$DI;
import com.econovafx.modules.core.config.DatabaseFactory$DI;
import com.econovafx.modules.core.config.SecurityModule$DI;
import com.econovafx.modules.core.config.UserContext$DI;
import com.econovafx.modules.core.repository.AuditLogRepository$DI;
import com.econovafx.modules.core.repository.CompanyRepository$DI;
import com.econovafx.modules.core.repository.CurrencyRepository$DI;
import com.econovafx.modules.core.repository.ExchangeRateRepository$DI;
import com.econovafx.modules.core.repository.ReportDefinitionRepository$DI;
import com.econovafx.modules.core.repository.SystemConfigRepository$DI;
import com.econovafx.modules.core.repository.UserRepository$DI;
import com.econovafx.modules.core.security.AuthService$DI;
import com.econovafx.modules.core.security.PasswordService$DI;
import com.econovafx.modules.core.service.AuditService$DI;
import com.econovafx.modules.core.service.BCCExchangeRateClient$DI;
import com.econovafx.modules.core.service.BCCExchangeRateFetcher$DI;
import com.econovafx.modules.core.service.CompanyService$DI;
import com.econovafx.modules.core.service.ExchangeRateScheduler$DI;
import com.econovafx.modules.core.service.ExchangeRateService$DI;
import com.econovafx.modules.core.service.ExportService$DI;
import com.econovafx.modules.core.service.SystemConfigService$DI;
import com.econovafx.modules.core.service.UserService$DI;
import com.econovafx.modules.core.service.backup.TenantBackupService$DI;
import com.econovafx.modules.core.ui.controller.AuditLogsController$DI;
import com.econovafx.modules.fixedassets.repository.DepreciationRecordRepository$DI;
import com.econovafx.modules.fixedassets.repository.FixedAssetRepository$DI;
import com.econovafx.modules.fixedassets.service.DepreciationService$DI;
import com.econovafx.modules.inventory.repository.InventoryCategoryRepository$DI;
import com.econovafx.modules.inventory.repository.InventoryItemRepository$DI;
import com.econovafx.modules.inventory.repository.InventoryMovementRepository$DI;
import com.econovafx.modules.inventory.repository.WarehouseRepository$DI;
import com.econovafx.modules.inventory.service.InventoryService$DI;

/**
 * Avaje Inject module for Econovafx.
 * 
 * When using the Java module system, this generated class should be explicitly
 * registered in module-info via a <code>provides</code> clause like:
 * 
 * <pre>{@code
 * 
 *   module example {
 *     requires io.avaje.inject;
 *     
 *     provides io.avaje.inject.spi.InjectExtension with com.econovafx.EconovafxModule;
 *     
 *   }
 * 
 * }</pre>
 */
@Generated("io.avaje.inject.generator")
@InjectModule()
@SuppressWarnings("all")
public final class EconovafxModule implements AvajeModule {

  /**
   * Creates all the beans in order based on constructor dependencies.
   * The beans are registered into the builder along with callbacks for
   * field/method injection, and lifecycle support.
   */
  @Override
  public void build(Builder builder) {
    // create beans in order based on constructor dependencies
    // i.e. "provides" followed by "dependsOn"
    build_repository_AccountingPeriodRepository(builder);
    build_service_AccountingPeriodService(builder);
    build_service_BankAccountService(builder);
    build_service_BankReconciliationService(builder);
    build_service_CashMovementService(builder);
    build_config_DataInitializer(builder);
    build_config_DatabaseFactory(builder);
    build_config_SecurityModule(builder);
    build_config_UserContext(builder);
    build_repository_CompanyRepository(builder);
    build_security_AuthService(builder);
    build_security_PasswordService(builder);
    build_service_BCCExchangeRateClient(builder);
    build_service_BCCExchangeRateFetcher(builder);
    build_service_CompanyService(builder);
    build_service_ExportService(builder);
    build_backup_TenantBackupService(builder);
    build_controller_AuditLogsController(builder);
    build_service_InventoryService(builder);
    build_aop_MethodInterceptor_authentication(builder);
    build_aop_MethodInterceptor_permission(builder);
    build_aop_MethodInterceptor_role(builder);
    build_aop_MethodInterceptor_tenantValidation(builder);
    build_ebean_Database(builder);
    build_service_AccountingReportService(builder);
    build_repository_AccountRepository(builder);
    build_repository_ExchangeDifferenceRepository(builder);
    build_repository_FinancialStatementModelRepository(builder);
    build_repository_FinancialStatementRowRepository(builder);
    build_repository_TransactionRepository(builder);
    build_service_AccountService(builder);
    build_service_FinancialStatementService(builder);
    build_repository_BillingSeriesRepository(builder);
    build_repository_SalesInvoiceRepository(builder);
    build_repository_TaxRateRepository(builder);
    build_repository_ThirdPartyRepository(builder);
    build_service_SequentialNumberService(builder);
    build_service_ThirdPartyService(builder);
    build_repository_AuditLogRepository(builder);
    build_repository_CurrencyRepository(builder);
    build_repository_ExchangeRateRepository(builder);
    build_repository_ReportDefinitionRepository(builder);
    build_repository_SystemConfigRepository(builder);
    build_repository_UserRepository(builder);
    build_service_AuditService(builder);
    build_service_ExchangeRateService(builder);
    build_service_SystemConfigService(builder);
    build_service_UserService(builder);
    build_repository_DepreciationRecordRepository(builder);
    build_repository_FixedAssetRepository(builder);
    build_repository_InventoryCategoryRepository(builder);
    build_repository_InventoryItemRepository(builder);
    build_repository_InventoryMovementRepository(builder);
    build_repository_WarehouseRepository(builder);
    build_service_TransactionService(builder);
    build_service_BillingService(builder);
    build_service_ExchangeRateScheduler(builder);
    build_service_DepreciationService(builder);
    build_service_ExchangeDifferenceService(builder);
  }

  @Override
  public String[] providesBeans() {
    return new String[] {
      "com.econovafx.core.service.AccountingReportService",
      "com.econovafx.modules.accounting.repository.AccountRepository",
      "com.econovafx.modules.accounting.repository.AccountingPeriodRepository",
      "com.econovafx.modules.accounting.repository.ExchangeDifferenceRepository",
      "com.econovafx.modules.accounting.repository.FinancialStatementModelRepository",
      "com.econovafx.modules.accounting.repository.FinancialStatementRowRepository",
      "com.econovafx.modules.accounting.repository.TransactionRepository",
      "com.econovafx.modules.accounting.service.AccountService",
      "com.econovafx.modules.accounting.service.AccountingPeriodService",
      "com.econovafx.modules.accounting.service.ExchangeDifferenceService",
      "com.econovafx.modules.accounting.service.FinancialStatementService",
      "com.econovafx.modules.accounting.service.TransactionService",
      "com.econovafx.modules.bank.service.BankAccountService",
      "com.econovafx.modules.bank.service.BankReconciliationService",
      "com.econovafx.modules.billing.repository.BillingSeriesRepository",
      "com.econovafx.modules.billing.repository.SalesInvoiceRepository",
      "com.econovafx.modules.billing.repository.TaxRateRepository",
      "com.econovafx.modules.billing.repository.ThirdPartyRepository",
      "com.econovafx.modules.billing.service.BillingService",
      "com.econovafx.modules.billing.service.SequentialNumberService",
      "com.econovafx.modules.billing.service.ThirdPartyService",
      "com.econovafx.modules.cash.service.CashMovementService",
      "com.econovafx.modules.core.config.DataInitializer",
      "com.econovafx.modules.core.config.UserContext",
      "com.econovafx.modules.core.repository.AuditLogRepository",
      "com.econovafx.modules.core.repository.CompanyRepository",
      "com.econovafx.modules.core.repository.CurrencyRepository",
      "com.econovafx.modules.core.repository.ExchangeRateRepository",
      "com.econovafx.modules.core.repository.ReportDefinitionRepository",
      "com.econovafx.modules.core.repository.SystemConfigRepository",
      "com.econovafx.modules.core.repository.UserRepository",
      "com.econovafx.modules.core.security.AuthService",
      "com.econovafx.modules.core.security.PasswordService",
      "com.econovafx.modules.core.service.AuditService",
      "com.econovafx.modules.core.service.BCCExchangeRateClient",
      "com.econovafx.modules.core.service.BCCExchangeRateFetcher",
      "com.econovafx.modules.core.service.CompanyService",
      "com.econovafx.modules.core.service.ExchangeRateScheduler",
      "com.econovafx.modules.core.service.ExchangeRateService",
      "com.econovafx.modules.core.service.ExportService",
      "com.econovafx.modules.core.service.SystemConfigService",
      "com.econovafx.modules.core.service.UserService",
      "com.econovafx.modules.core.service.backup.TenantBackupService",
      "com.econovafx.modules.core.ui.controller.AuditLogsController",
      "com.econovafx.modules.fixedassets.repository.DepreciationRecordRepository",
      "com.econovafx.modules.fixedassets.repository.FixedAssetRepository",
      "com.econovafx.modules.fixedassets.service.DepreciationService",
      "com.econovafx.modules.inventory.repository.InventoryCategoryRepository",
      "com.econovafx.modules.inventory.repository.InventoryItemRepository",
      "com.econovafx.modules.inventory.repository.InventoryMovementRepository",
      "com.econovafx.modules.inventory.repository.WarehouseRepository",
      "com.econovafx.modules.inventory.service.InventoryService",
      "io.avaje.inject.aop.MethodInterceptor",
      "io.ebean.Database",
    };
  }

  @Override
  public Class<?>[] classes() {
    return new Class<?>[] {
      com.econovafx.core.service.AccountingReportService.class,
      com.econovafx.modules.accounting.repository.AccountRepository.class,
      com.econovafx.modules.accounting.repository.AccountingPeriodRepository.class,
      com.econovafx.modules.accounting.repository.ExchangeDifferenceRepository.class,
      com.econovafx.modules.accounting.repository.FinancialStatementModelRepository.class,
      com.econovafx.modules.accounting.repository.FinancialStatementRowRepository.class,
      com.econovafx.modules.accounting.repository.TransactionRepository.class,
      com.econovafx.modules.accounting.service.AccountService.class,
      com.econovafx.modules.accounting.service.AccountingPeriodService.class,
      com.econovafx.modules.accounting.service.ExchangeDifferenceService.class,
      com.econovafx.modules.accounting.service.FinancialStatementService.class,
      com.econovafx.modules.accounting.service.TransactionService.class,
      com.econovafx.modules.bank.service.BankAccountService.class,
      com.econovafx.modules.bank.service.BankReconciliationService.class,
      com.econovafx.modules.billing.repository.BillingSeriesRepository.class,
      com.econovafx.modules.billing.repository.SalesInvoiceRepository.class,
      com.econovafx.modules.billing.repository.TaxRateRepository.class,
      com.econovafx.modules.billing.repository.ThirdPartyRepository.class,
      com.econovafx.modules.billing.service.BillingService.class,
      com.econovafx.modules.billing.service.SequentialNumberService.class,
      com.econovafx.modules.billing.service.ThirdPartyService.class,
      com.econovafx.modules.cash.service.CashMovementService.class,
      com.econovafx.modules.core.config.DataInitializer.class,
      com.econovafx.modules.core.config.DatabaseFactory.class,
      com.econovafx.modules.core.config.SecurityModule.class,
      com.econovafx.modules.core.config.UserContext.class,
      com.econovafx.modules.core.repository.AuditLogRepository.class,
      com.econovafx.modules.core.repository.CompanyRepository.class,
      com.econovafx.modules.core.repository.CurrencyRepository.class,
      com.econovafx.modules.core.repository.ExchangeRateRepository.class,
      com.econovafx.modules.core.repository.ReportDefinitionRepository.class,
      com.econovafx.modules.core.repository.SystemConfigRepository.class,
      com.econovafx.modules.core.repository.UserRepository.class,
      com.econovafx.modules.core.security.AuthService.class,
      com.econovafx.modules.core.security.PasswordService.class,
      com.econovafx.modules.core.service.AuditService.class,
      com.econovafx.modules.core.service.BCCExchangeRateClient.class,
      com.econovafx.modules.core.service.BCCExchangeRateFetcher.class,
      com.econovafx.modules.core.service.CompanyService.class,
      com.econovafx.modules.core.service.ExchangeRateScheduler.class,
      com.econovafx.modules.core.service.ExchangeRateService.class,
      com.econovafx.modules.core.service.ExportService.class,
      com.econovafx.modules.core.service.SystemConfigService.class,
      com.econovafx.modules.core.service.UserService.class,
      com.econovafx.modules.core.service.backup.TenantBackupService.class,
      com.econovafx.modules.core.ui.controller.AuditLogsController.class,
      com.econovafx.modules.fixedassets.repository.DepreciationRecordRepository.class,
      com.econovafx.modules.fixedassets.repository.FixedAssetRepository.class,
      com.econovafx.modules.fixedassets.service.DepreciationService.class,
      com.econovafx.modules.inventory.repository.InventoryCategoryRepository.class,
      com.econovafx.modules.inventory.repository.InventoryItemRepository.class,
      com.econovafx.modules.inventory.repository.InventoryMovementRepository.class,
      com.econovafx.modules.inventory.repository.WarehouseRepository.class,
      com.econovafx.modules.inventory.service.InventoryService.class,
      io.avaje.inject.aop.MethodInterceptor.class,
      io.ebean.Database.class,
    };
  }

  @DependencyMeta(
      type = "com.econovafx.modules.accounting.repository.AccountingPeriodRepository",
      provides = {"com.econovafx.modules.accounting.repository.AccountingPeriodRepository"})
  public static void build_repository_AccountingPeriodRepository(Builder builder) {
    AccountingPeriodRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.accounting.service.AccountingPeriodService",
      provides = {"com.econovafx.modules.accounting.service.AccountingPeriodService"})
  public static void build_service_AccountingPeriodService(Builder builder) {
    AccountingPeriodService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.bank.service.BankAccountService",
      provides = {"com.econovafx.modules.bank.service.BankAccountService"})
  public static void build_service_BankAccountService(Builder builder) {
    BankAccountService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.bank.service.BankReconciliationService",
      provides = {"com.econovafx.modules.bank.service.BankReconciliationService"})
  public static void build_service_BankReconciliationService(Builder builder) {
    BankReconciliationService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.cash.service.CashMovementService",
      provides = {"com.econovafx.modules.cash.service.CashMovementService"})
  public static void build_service_CashMovementService(Builder builder) {
    CashMovementService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.core.config.DataInitializer",
      provides = {"com.econovafx.modules.core.config.DataInitializer"})
  public static void build_config_DataInitializer(Builder builder) {
    DataInitializer$DI.build(builder);
  }

  @DependencyMeta(type = "com.econovafx.modules.core.config.DatabaseFactory")
  public static void build_config_DatabaseFactory(Builder builder) {
    DatabaseFactory$DI.build(builder);
  }

  @DependencyMeta(type = "com.econovafx.modules.core.config.SecurityModule")
  public static void build_config_SecurityModule(Builder builder) {
    SecurityModule$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.core.config.UserContext",
      provides = {"com.econovafx.modules.core.config.UserContext"})
  public static void build_config_UserContext(Builder builder) {
    UserContext$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.core.repository.CompanyRepository",
      provides = {"com.econovafx.modules.core.repository.CompanyRepository"})
  public static void build_repository_CompanyRepository(Builder builder) {
    CompanyRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.core.security.AuthService",
      provides = {"com.econovafx.modules.core.security.AuthService"})
  public static void build_security_AuthService(Builder builder) {
    AuthService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.core.security.PasswordService",
      provides = {"com.econovafx.modules.core.security.PasswordService"})
  public static void build_security_PasswordService(Builder builder) {
    PasswordService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.core.service.BCCExchangeRateClient",
      provides = {"com.econovafx.modules.core.service.BCCExchangeRateClient"})
  public static void build_service_BCCExchangeRateClient(Builder builder) {
    BCCExchangeRateClient$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.core.service.BCCExchangeRateFetcher",
      provides = {"com.econovafx.modules.core.service.BCCExchangeRateFetcher"})
  public static void build_service_BCCExchangeRateFetcher(Builder builder) {
    BCCExchangeRateFetcher$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.core.service.CompanyService",
      provides = {"com.econovafx.modules.core.service.CompanyService"})
  public static void build_service_CompanyService(Builder builder) {
    CompanyService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.core.service.ExportService",
      provides = {"com.econovafx.modules.core.service.ExportService"})
  public static void build_service_ExportService(Builder builder) {
    ExportService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.core.service.backup.TenantBackupService",
      provides = {"com.econovafx.modules.core.service.backup.TenantBackupService"})
  public static void build_backup_TenantBackupService(Builder builder) {
    TenantBackupService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.core.ui.controller.AuditLogsController",
      provides = {"com.econovafx.modules.core.ui.controller.AuditLogsController"})
  public static void build_controller_AuditLogsController(Builder builder) {
    AuditLogsController$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.inventory.service.InventoryService",
      provides = {"com.econovafx.modules.inventory.service.InventoryService"})
  public static void build_service_InventoryService(Builder builder) {
    InventoryService$DI.build(builder);
  }

  @DependencyMeta(
      type = "io.avaje.inject.aop.MethodInterceptor",
      name = "authentication",
      method = "com.econovafx.modules.core.config.SecurityModule$DI.build_authenticationInterceptor",
      provides = {"io.avaje.inject.aop.MethodInterceptor"},
      dependsOn = {"com.econovafx.modules.core.config.SecurityModule"})
  public static void build_aop_MethodInterceptor_authentication(Builder builder) {
    SecurityModule$DI.build_authenticationInterceptor(builder);
  }

  @DependencyMeta(
      type = "io.avaje.inject.aop.MethodInterceptor",
      name = "permission",
      method = "com.econovafx.modules.core.config.SecurityModule$DI.build_permissionInterceptor",
      provides = {"io.avaje.inject.aop.MethodInterceptor"},
      dependsOn = {"com.econovafx.modules.core.config.SecurityModule"})
  public static void build_aop_MethodInterceptor_permission(Builder builder) {
    SecurityModule$DI.build_permissionInterceptor(builder);
  }

  @DependencyMeta(
      type = "io.avaje.inject.aop.MethodInterceptor",
      name = "role",
      method = "com.econovafx.modules.core.config.SecurityModule$DI.build_roleInterceptor",
      provides = {"io.avaje.inject.aop.MethodInterceptor"},
      dependsOn = {"com.econovafx.modules.core.config.SecurityModule"})
  public static void build_aop_MethodInterceptor_role(Builder builder) {
    SecurityModule$DI.build_roleInterceptor(builder);
  }

  @DependencyMeta(
      type = "io.avaje.inject.aop.MethodInterceptor",
      name = "tenantValidation",
      method = "com.econovafx.modules.core.config.SecurityModule$DI.build_tenantValidationInterceptor",
      provides = {"io.avaje.inject.aop.MethodInterceptor"},
      dependsOn = {"com.econovafx.modules.core.config.SecurityModule"})
  public static void build_aop_MethodInterceptor_tenantValidation(Builder builder) {
    SecurityModule$DI.build_tenantValidationInterceptor(builder);
  }

  @DependencyMeta(
      type = "io.ebean.Database",
      method = "com.econovafx.modules.core.config.DatabaseFactory$DI.build_database",
      provides = {"io.ebean.Database"},
      dependsOn = {"com.econovafx.modules.core.config.DatabaseFactory"})
  public static void build_ebean_Database(Builder builder) {
    DatabaseFactory$DI.build_database(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.core.service.AccountingReportService",
      provides = {"com.econovafx.core.service.AccountingReportService"},
      dependsOn = {"io.ebean.Database"})
  public static void build_service_AccountingReportService(Builder builder) {
    AccountingReportService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.accounting.repository.AccountRepository",
      provides = {"com.econovafx.modules.accounting.repository.AccountRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_AccountRepository(Builder builder) {
    AccountRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.accounting.repository.ExchangeDifferenceRepository",
      provides = {"com.econovafx.modules.accounting.repository.ExchangeDifferenceRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_ExchangeDifferenceRepository(Builder builder) {
    ExchangeDifferenceRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.accounting.repository.FinancialStatementModelRepository",
      provides = {"com.econovafx.modules.accounting.repository.FinancialStatementModelRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_FinancialStatementModelRepository(Builder builder) {
    FinancialStatementModelRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.accounting.repository.FinancialStatementRowRepository",
      provides = {"com.econovafx.modules.accounting.repository.FinancialStatementRowRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_FinancialStatementRowRepository(Builder builder) {
    FinancialStatementRowRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.accounting.repository.TransactionRepository",
      provides = {"com.econovafx.modules.accounting.repository.TransactionRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_TransactionRepository(Builder builder) {
    TransactionRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.accounting.service.AccountService",
      provides = {"com.econovafx.modules.accounting.service.AccountService"},
      dependsOn = {"com.econovafx.modules.accounting.repository.AccountRepository"})
  public static void build_service_AccountService(Builder builder) {
    AccountService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.accounting.service.FinancialStatementService",
      provides = {"com.econovafx.modules.accounting.service.FinancialStatementService"},
      dependsOn = {
        "com.econovafx.modules.accounting.repository.FinancialStatementModelRepository",
        "com.econovafx.modules.accounting.repository.FinancialStatementRowRepository",
        "com.econovafx.modules.accounting.repository.AccountRepository"
      })
  public static void build_service_FinancialStatementService(Builder builder) {
    FinancialStatementService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.billing.repository.BillingSeriesRepository",
      provides = {"com.econovafx.modules.billing.repository.BillingSeriesRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_BillingSeriesRepository(Builder builder) {
    BillingSeriesRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.billing.repository.SalesInvoiceRepository",
      provides = {"com.econovafx.modules.billing.repository.SalesInvoiceRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_SalesInvoiceRepository(Builder builder) {
    SalesInvoiceRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.billing.repository.TaxRateRepository",
      provides = {"com.econovafx.modules.billing.repository.TaxRateRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_TaxRateRepository(Builder builder) {
    TaxRateRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.billing.repository.ThirdPartyRepository",
      provides = {"com.econovafx.modules.billing.repository.ThirdPartyRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_ThirdPartyRepository(Builder builder) {
    ThirdPartyRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.billing.service.SequentialNumberService",
      provides = {"com.econovafx.modules.billing.service.SequentialNumberService"},
      dependsOn = {"com.econovafx.modules.billing.repository.BillingSeriesRepository"})
  public static void build_service_SequentialNumberService(Builder builder) {
    SequentialNumberService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.billing.service.ThirdPartyService",
      provides = {"com.econovafx.modules.billing.service.ThirdPartyService"},
      dependsOn = {
        "com.econovafx.modules.billing.repository.ThirdPartyRepository",
        "com.econovafx.modules.core.config.UserContext"
      })
  public static void build_service_ThirdPartyService(Builder builder) {
    ThirdPartyService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.core.repository.AuditLogRepository",
      provides = {"com.econovafx.modules.core.repository.AuditLogRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_AuditLogRepository(Builder builder) {
    AuditLogRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.core.repository.CurrencyRepository",
      provides = {"com.econovafx.modules.core.repository.CurrencyRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_CurrencyRepository(Builder builder) {
    CurrencyRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.core.repository.ExchangeRateRepository",
      provides = {"com.econovafx.modules.core.repository.ExchangeRateRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_ExchangeRateRepository(Builder builder) {
    ExchangeRateRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.core.repository.ReportDefinitionRepository",
      provides = {"com.econovafx.modules.core.repository.ReportDefinitionRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_ReportDefinitionRepository(Builder builder) {
    ReportDefinitionRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.core.repository.SystemConfigRepository",
      provides = {"com.econovafx.modules.core.repository.SystemConfigRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_SystemConfigRepository(Builder builder) {
    SystemConfigRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.core.repository.UserRepository",
      provides = {"com.econovafx.modules.core.repository.UserRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_UserRepository(Builder builder) {
    UserRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.core.service.AuditService",
      provides = {"com.econovafx.modules.core.service.AuditService"},
      dependsOn = {"com.econovafx.modules.core.repository.AuditLogRepository"})
  public static void build_service_AuditService(Builder builder) {
    AuditService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.core.service.ExchangeRateService",
      provides = {"com.econovafx.modules.core.service.ExchangeRateService"},
      dependsOn = {
        "com.econovafx.modules.core.repository.ExchangeRateRepository",
        "com.econovafx.modules.core.repository.CurrencyRepository",
        "com.econovafx.modules.core.service.BCCExchangeRateFetcher"
      })
  public static void build_service_ExchangeRateService(Builder builder) {
    ExchangeRateService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.core.service.SystemConfigService",
      provides = {"com.econovafx.modules.core.service.SystemConfigService"},
      dependsOn = {"com.econovafx.modules.core.repository.SystemConfigRepository"})
  public static void build_service_SystemConfigService(Builder builder) {
    SystemConfigService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.core.service.UserService",
      provides = {"com.econovafx.modules.core.service.UserService"},
      dependsOn = {
        "com.econovafx.modules.core.repository.UserRepository",
        "com.econovafx.modules.core.service.AuditService",
        "com.econovafx.modules.core.security.PasswordService"
      })
  public static void build_service_UserService(Builder builder) {
    UserService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.fixedassets.repository.DepreciationRecordRepository",
      provides = {"com.econovafx.modules.fixedassets.repository.DepreciationRecordRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_DepreciationRecordRepository(Builder builder) {
    DepreciationRecordRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.fixedassets.repository.FixedAssetRepository",
      provides = {"com.econovafx.modules.fixedassets.repository.FixedAssetRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_FixedAssetRepository(Builder builder) {
    FixedAssetRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.inventory.repository.InventoryCategoryRepository",
      provides = {"com.econovafx.modules.inventory.repository.InventoryCategoryRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_InventoryCategoryRepository(Builder builder) {
    InventoryCategoryRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.inventory.repository.InventoryItemRepository",
      provides = {"com.econovafx.modules.inventory.repository.InventoryItemRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_InventoryItemRepository(Builder builder) {
    InventoryItemRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.inventory.repository.InventoryMovementRepository",
      provides = {"com.econovafx.modules.inventory.repository.InventoryMovementRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_InventoryMovementRepository(Builder builder) {
    InventoryMovementRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.inventory.repository.WarehouseRepository",
      provides = {"com.econovafx.modules.inventory.repository.WarehouseRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_WarehouseRepository(Builder builder) {
    WarehouseRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.accounting.service.TransactionService",
      provides = {"com.econovafx.modules.accounting.service.TransactionService"},
      dependsOn = {
        "com.econovafx.modules.accounting.repository.TransactionRepository",
        "com.econovafx.modules.accounting.repository.AccountRepository",
        "com.econovafx.modules.core.service.AuditService",
        "com.econovafx.modules.accounting.service.AccountingPeriodService"
      })
  public static void build_service_TransactionService(Builder builder) {
    TransactionService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.billing.service.BillingService",
      provides = {"com.econovafx.modules.billing.service.BillingService"},
      dependsOn = {
        "com.econovafx.modules.billing.repository.SalesInvoiceRepository",
        "com.econovafx.modules.billing.repository.BillingSeriesRepository",
        "com.econovafx.modules.billing.service.SequentialNumberService",
        "com.econovafx.modules.accounting.service.TransactionService",
        "com.econovafx.modules.inventory.service.InventoryService",
        "com.econovafx.modules.accounting.repository.AccountRepository",
        "com.econovafx.modules.billing.repository.TaxRateRepository",
        "com.econovafx.modules.core.service.AuditService"
      })
  public static void build_service_BillingService(Builder builder) {
    BillingService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.core.service.ExchangeRateScheduler",
      provides = {"com.econovafx.modules.core.service.ExchangeRateScheduler"},
      dependsOn = {
        "com.econovafx.modules.core.service.ExchangeRateService",
        "com.econovafx.modules.core.service.BCCExchangeRateClient"
      })
  public static void build_service_ExchangeRateScheduler(Builder builder) {
    ExchangeRateScheduler$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.fixedassets.service.DepreciationService",
      provides = {"com.econovafx.modules.fixedassets.service.DepreciationService"},
      dependsOn = {
        "com.econovafx.modules.fixedassets.repository.FixedAssetRepository",
        "com.econovafx.modules.fixedassets.repository.DepreciationRecordRepository",
        "com.econovafx.modules.accounting.service.TransactionService",
        "com.econovafx.modules.accounting.repository.AccountRepository",
        "com.econovafx.modules.core.service.AuditService"
      })
  public static void build_service_DepreciationService(Builder builder) {
    DepreciationService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.modules.accounting.service.ExchangeDifferenceService",
      provides = {"com.econovafx.modules.accounting.service.ExchangeDifferenceService"},
      dependsOn = {
        "com.econovafx.modules.accounting.repository.ExchangeDifferenceRepository",
        "com.econovafx.modules.core.repository.ExchangeRateRepository",
        "com.econovafx.modules.accounting.service.TransactionService",
        "com.econovafx.modules.accounting.repository.AccountRepository",
        "com.econovafx.modules.core.service.AuditService"
      })
  public static void build_service_ExchangeDifferenceService(Builder builder) {
    ExchangeDifferenceService$DI.build(builder);
  }

}
