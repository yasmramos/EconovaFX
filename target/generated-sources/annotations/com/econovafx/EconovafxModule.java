package com.econovafx;

import io.avaje.inject.BeanScope;
import io.avaje.inject.InjectModule;
import io.avaje.inject.spi.AvajeModule;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.DependencyMeta;
import io.avaje.inject.spi.Generated;
import io.avaje.inject.spi.GenericType;
import java.lang.reflect.Type;
import com.econovafx.config.DatabaseFactory$DI;
import com.econovafx.core.service.AccountingReportService$DI;
import com.econovafx.repository.AccountRepository$DI;
import com.econovafx.repository.AccountingPeriodRepository$DI;
import com.econovafx.repository.AuditLogRepository$DI;
import com.econovafx.repository.CompanyRepository$DI;
import com.econovafx.repository.CurrencyRepository$DI;
import com.econovafx.repository.ExchangeRateRepository$DI;
import com.econovafx.repository.FinancialStatementModelRepository$DI;
import com.econovafx.repository.FinancialStatementRowRepository$DI;
import com.econovafx.repository.InventoryCategoryRepository$DI;
import com.econovafx.repository.InventoryItemRepository$DI;
import com.econovafx.repository.InventoryMovementRepository$DI;
import com.econovafx.repository.ReportDefinitionRepository$DI;
import com.econovafx.repository.SystemConfigRepository$DI;
import com.econovafx.repository.ThirdPartyRepository$DI;
import com.econovafx.repository.TransactionRepository$DI;
import com.econovafx.repository.UserRepository$DI;
import com.econovafx.repository.WarehouseRepository$DI;
import com.econovafx.service.AccountService$DI;
import com.econovafx.service.AccountingPeriodService$DI;
import com.econovafx.service.AuditService$DI;
import com.econovafx.service.BCCExchangeRateClient$DI;
import com.econovafx.service.BCCExchangeRateFetcher$DI;
import com.econovafx.service.CashMovementService$DI;
import com.econovafx.service.CompanyService$DI;
import com.econovafx.service.ExchangeRateScheduler$DI;
import com.econovafx.service.ExchangeRateService$DI;
import com.econovafx.service.ExportService$DI;
import com.econovafx.service.FinancialStatementService$DI;
import com.econovafx.service.InventoryService$DI;
import com.econovafx.service.NotificationService$DI;
import com.econovafx.service.SystemConfigService$DI;
import com.econovafx.service.ThirdPartyService$DI;
import com.econovafx.service.TransactionService$DI;
import com.econovafx.service.UserService$DI;
import com.econovafx.ui.controller.AuditLogsController$DI;

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
    build_config_DatabaseFactory(builder);
    build_repository_AccountingPeriodRepository(builder);
    build_repository_CompanyRepository(builder);
    build_service_AccountingPeriodService(builder);
    build_service_BCCExchangeRateClient(builder);
    build_service_BCCExchangeRateFetcher(builder);
    build_service_CashMovementService(builder);
    build_service_CompanyService(builder);
    build_service_ExportService(builder);
    build_service_InventoryService(builder);
    build_service_NotificationService(builder);
    build_controller_AuditLogsController(builder);
    build_ebean_Database(builder);
    build_service_AccountingReportService(builder);
    build_repository_AccountRepository(builder);
    build_repository_AuditLogRepository(builder);
    build_repository_CurrencyRepository(builder);
    build_repository_ExchangeRateRepository(builder);
    build_repository_FinancialStatementModelRepository(builder);
    build_repository_FinancialStatementRowRepository(builder);
    build_repository_InventoryCategoryRepository(builder);
    build_repository_InventoryItemRepository(builder);
    build_repository_InventoryMovementRepository(builder);
    build_repository_ReportDefinitionRepository(builder);
    build_repository_SystemConfigRepository(builder);
    build_repository_ThirdPartyRepository(builder);
    build_repository_TransactionRepository(builder);
    build_repository_UserRepository(builder);
    build_repository_WarehouseRepository(builder);
    build_service_AccountService(builder);
    build_service_AuditService(builder);
    build_service_ExchangeRateService(builder);
    build_service_FinancialStatementService(builder);
    build_service_SystemConfigService(builder);
    build_service_ThirdPartyService(builder);
    build_service_TransactionService(builder);
    build_service_UserService(builder);
    build_service_ExchangeRateScheduler(builder);
  }

  @Override
  public String[] providesBeans() {
    return new String[] {
      "com.econovafx.core.service.AccountingReportService",
      "com.econovafx.repository.AccountRepository",
      "com.econovafx.repository.AccountingPeriodRepository",
      "com.econovafx.repository.AuditLogRepository",
      "com.econovafx.repository.CompanyRepository",
      "com.econovafx.repository.CurrencyRepository",
      "com.econovafx.repository.ExchangeRateRepository",
      "com.econovafx.repository.FinancialStatementModelRepository",
      "com.econovafx.repository.FinancialStatementRowRepository",
      "com.econovafx.repository.InventoryCategoryRepository",
      "com.econovafx.repository.InventoryItemRepository",
      "com.econovafx.repository.InventoryMovementRepository",
      "com.econovafx.repository.ReportDefinitionRepository",
      "com.econovafx.repository.SystemConfigRepository",
      "com.econovafx.repository.ThirdPartyRepository",
      "com.econovafx.repository.TransactionRepository",
      "com.econovafx.repository.UserRepository",
      "com.econovafx.repository.WarehouseRepository",
      "com.econovafx.service.AccountService",
      "com.econovafx.service.AccountingPeriodService",
      "com.econovafx.service.AuditService",
      "com.econovafx.service.BCCExchangeRateClient",
      "com.econovafx.service.BCCExchangeRateFetcher",
      "com.econovafx.service.CashMovementService",
      "com.econovafx.service.CompanyService",
      "com.econovafx.service.ExchangeRateScheduler",
      "com.econovafx.service.ExchangeRateService",
      "com.econovafx.service.ExportService",
      "com.econovafx.service.FinancialStatementService",
      "com.econovafx.service.InventoryService",
      "com.econovafx.service.NotificationService",
      "com.econovafx.service.SystemConfigService",
      "com.econovafx.service.ThirdPartyService",
      "com.econovafx.service.TransactionService",
      "com.econovafx.service.UserService",
      "com.econovafx.ui.controller.AuditLogsController",
      "io.ebean.Database",
    };
  }

  @Override
  public Class<?>[] classes() {
    return new Class<?>[] {
      com.econovafx.config.DatabaseFactory.class,
      com.econovafx.core.service.AccountingReportService.class,
      com.econovafx.repository.AccountRepository.class,
      com.econovafx.repository.AccountingPeriodRepository.class,
      com.econovafx.repository.AuditLogRepository.class,
      com.econovafx.repository.CompanyRepository.class,
      com.econovafx.repository.CurrencyRepository.class,
      com.econovafx.repository.ExchangeRateRepository.class,
      com.econovafx.repository.FinancialStatementModelRepository.class,
      com.econovafx.repository.FinancialStatementRowRepository.class,
      com.econovafx.repository.InventoryCategoryRepository.class,
      com.econovafx.repository.InventoryItemRepository.class,
      com.econovafx.repository.InventoryMovementRepository.class,
      com.econovafx.repository.ReportDefinitionRepository.class,
      com.econovafx.repository.SystemConfigRepository.class,
      com.econovafx.repository.ThirdPartyRepository.class,
      com.econovafx.repository.TransactionRepository.class,
      com.econovafx.repository.UserRepository.class,
      com.econovafx.repository.WarehouseRepository.class,
      com.econovafx.service.AccountService.class,
      com.econovafx.service.AccountingPeriodService.class,
      com.econovafx.service.AuditService.class,
      com.econovafx.service.BCCExchangeRateClient.class,
      com.econovafx.service.BCCExchangeRateFetcher.class,
      com.econovafx.service.CashMovementService.class,
      com.econovafx.service.CompanyService.class,
      com.econovafx.service.ExchangeRateScheduler.class,
      com.econovafx.service.ExchangeRateService.class,
      com.econovafx.service.ExportService.class,
      com.econovafx.service.FinancialStatementService.class,
      com.econovafx.service.InventoryService.class,
      com.econovafx.service.NotificationService.class,
      com.econovafx.service.SystemConfigService.class,
      com.econovafx.service.ThirdPartyService.class,
      com.econovafx.service.TransactionService.class,
      com.econovafx.service.UserService.class,
      com.econovafx.ui.controller.AuditLogsController.class,
      io.ebean.Database.class,
    };
  }

  @DependencyMeta(type = "com.econovafx.config.DatabaseFactory")
  public static void build_config_DatabaseFactory(Builder builder) {
    DatabaseFactory$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.repository.AccountingPeriodRepository",
      provides = {"com.econovafx.repository.AccountingPeriodRepository"})
  public static void build_repository_AccountingPeriodRepository(Builder builder) {
    AccountingPeriodRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.repository.CompanyRepository",
      provides = {"com.econovafx.repository.CompanyRepository"})
  public static void build_repository_CompanyRepository(Builder builder) {
    CompanyRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.service.AccountingPeriodService",
      provides = {"com.econovafx.service.AccountingPeriodService"})
  public static void build_service_AccountingPeriodService(Builder builder) {
    AccountingPeriodService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.service.BCCExchangeRateClient",
      provides = {"com.econovafx.service.BCCExchangeRateClient"})
  public static void build_service_BCCExchangeRateClient(Builder builder) {
    BCCExchangeRateClient$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.service.BCCExchangeRateFetcher",
      provides = {"com.econovafx.service.BCCExchangeRateFetcher"})
  public static void build_service_BCCExchangeRateFetcher(Builder builder) {
    BCCExchangeRateFetcher$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.service.CashMovementService",
      provides = {"com.econovafx.service.CashMovementService"})
  public static void build_service_CashMovementService(Builder builder) {
    CashMovementService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.service.CompanyService",
      provides = {"com.econovafx.service.CompanyService"})
  public static void build_service_CompanyService(Builder builder) {
    CompanyService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.service.ExportService",
      provides = {"com.econovafx.service.ExportService"})
  public static void build_service_ExportService(Builder builder) {
    ExportService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.service.InventoryService",
      provides = {"com.econovafx.service.InventoryService"})
  public static void build_service_InventoryService(Builder builder) {
    InventoryService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.service.NotificationService",
      provides = {"com.econovafx.service.NotificationService"})
  public static void build_service_NotificationService(Builder builder) {
    NotificationService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.ui.controller.AuditLogsController",
      provides = {"com.econovafx.ui.controller.AuditLogsController"})
  public static void build_controller_AuditLogsController(Builder builder) {
    AuditLogsController$DI.build(builder);
  }

  @DependencyMeta(
      type = "io.ebean.Database",
      method = "com.econovafx.config.DatabaseFactory$DI.build_database",
      provides = {"io.ebean.Database"},
      dependsOn = {"com.econovafx.config.DatabaseFactory"})
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
      type = "com.econovafx.repository.AccountRepository",
      provides = {"com.econovafx.repository.AccountRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_AccountRepository(Builder builder) {
    AccountRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.repository.AuditLogRepository",
      provides = {"com.econovafx.repository.AuditLogRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_AuditLogRepository(Builder builder) {
    AuditLogRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.repository.CurrencyRepository",
      provides = {"com.econovafx.repository.CurrencyRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_CurrencyRepository(Builder builder) {
    CurrencyRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.repository.ExchangeRateRepository",
      provides = {"com.econovafx.repository.ExchangeRateRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_ExchangeRateRepository(Builder builder) {
    ExchangeRateRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.repository.FinancialStatementModelRepository",
      provides = {"com.econovafx.repository.FinancialStatementModelRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_FinancialStatementModelRepository(Builder builder) {
    FinancialStatementModelRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.repository.FinancialStatementRowRepository",
      provides = {"com.econovafx.repository.FinancialStatementRowRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_FinancialStatementRowRepository(Builder builder) {
    FinancialStatementRowRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.repository.InventoryCategoryRepository",
      provides = {"com.econovafx.repository.InventoryCategoryRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_InventoryCategoryRepository(Builder builder) {
    InventoryCategoryRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.repository.InventoryItemRepository",
      provides = {"com.econovafx.repository.InventoryItemRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_InventoryItemRepository(Builder builder) {
    InventoryItemRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.repository.InventoryMovementRepository",
      provides = {"com.econovafx.repository.InventoryMovementRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_InventoryMovementRepository(Builder builder) {
    InventoryMovementRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.repository.ReportDefinitionRepository",
      provides = {"com.econovafx.repository.ReportDefinitionRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_ReportDefinitionRepository(Builder builder) {
    ReportDefinitionRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.repository.SystemConfigRepository",
      provides = {"com.econovafx.repository.SystemConfigRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_SystemConfigRepository(Builder builder) {
    SystemConfigRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.repository.ThirdPartyRepository",
      provides = {"com.econovafx.repository.ThirdPartyRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_ThirdPartyRepository(Builder builder) {
    ThirdPartyRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.repository.TransactionRepository",
      provides = {"com.econovafx.repository.TransactionRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_TransactionRepository(Builder builder) {
    TransactionRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.repository.UserRepository",
      provides = {"com.econovafx.repository.UserRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_UserRepository(Builder builder) {
    UserRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.repository.WarehouseRepository",
      provides = {"com.econovafx.repository.WarehouseRepository"},
      dependsOn = {"io.ebean.Database"})
  public static void build_repository_WarehouseRepository(Builder builder) {
    WarehouseRepository$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.service.AccountService",
      provides = {"com.econovafx.service.AccountService"},
      dependsOn = {"com.econovafx.repository.AccountRepository"})
  public static void build_service_AccountService(Builder builder) {
    AccountService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.service.AuditService",
      provides = {"com.econovafx.service.AuditService"},
      dependsOn = {"com.econovafx.repository.AuditLogRepository"})
  public static void build_service_AuditService(Builder builder) {
    AuditService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.service.ExchangeRateService",
      provides = {"com.econovafx.service.ExchangeRateService"},
      dependsOn = {
        "com.econovafx.repository.ExchangeRateRepository",
        "com.econovafx.repository.CurrencyRepository",
        "com.econovafx.service.BCCExchangeRateFetcher"
      })
  public static void build_service_ExchangeRateService(Builder builder) {
    ExchangeRateService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.service.FinancialStatementService",
      provides = {"com.econovafx.service.FinancialStatementService"},
      dependsOn = {
        "com.econovafx.repository.FinancialStatementModelRepository",
        "com.econovafx.repository.FinancialStatementRowRepository",
        "com.econovafx.repository.AccountRepository"
      })
  public static void build_service_FinancialStatementService(Builder builder) {
    FinancialStatementService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.service.SystemConfigService",
      provides = {"com.econovafx.service.SystemConfigService"},
      dependsOn = {"com.econovafx.repository.SystemConfigRepository"})
  public static void build_service_SystemConfigService(Builder builder) {
    SystemConfigService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.service.ThirdPartyService",
      provides = {"com.econovafx.service.ThirdPartyService"},
      dependsOn = {"com.econovafx.repository.ThirdPartyRepository"})
  public static void build_service_ThirdPartyService(Builder builder) {
    ThirdPartyService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.service.TransactionService",
      provides = {"com.econovafx.service.TransactionService"},
      dependsOn = {
        "com.econovafx.repository.TransactionRepository",
        "com.econovafx.repository.AccountRepository",
        "com.econovafx.service.AuditService"
      })
  public static void build_service_TransactionService(Builder builder) {
    TransactionService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.service.UserService",
      provides = {"com.econovafx.service.UserService"},
      dependsOn = {
        "com.econovafx.repository.UserRepository",
        "com.econovafx.service.AuditService"
      })
  public static void build_service_UserService(Builder builder) {
    UserService$DI.build(builder);
  }

  @DependencyMeta(
      type = "com.econovafx.service.ExchangeRateScheduler",
      provides = {"com.econovafx.service.ExchangeRateScheduler"},
      dependsOn = {
        "com.econovafx.service.ExchangeRateService",
        "com.econovafx.service.BCCExchangeRateClient"
      })
  public static void build_service_ExchangeRateScheduler(Builder builder) {
    ExchangeRateScheduler$DI.build(builder);
  }

}
