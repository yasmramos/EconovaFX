package com.econovafx.modules.accounting.service;

import com.econovafx.modules.accounting.repository.AccountingPeriodRepository;
import com.econovafx.modules.accounting.repository.ClosingEntryRepository;
import com.econovafx.modules.accounting.repository.TransactionRepository;
import com.econovafx.modules.cash.service.CashMovementService;
import com.econovafx.modules.inventory.service.InventoryService;
import com.econovafx.modules.reporting.repository.FinancialReportRepository;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class AccountingPeriodService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(AccountingPeriodService.class)) {
      var bean = new AccountingPeriodService();
      var $bean = builder.register(bean);
       builder.addInjector(b -> {
         // field and method injection
        $bean.financialReportRepository = b.get(FinancialReportRepository.class);
        $bean.transactionService = b.get(TransactionService.class);
        $bean.closingEntryRepository = b.get(ClosingEntryRepository.class);
        $bean.financialStatementService = b.get(FinancialStatementService.class);
        $bean.transactionRepository = b.get(TransactionRepository.class);
        $bean.inventoryService = b.get(InventoryService.class);
        $bean.cashMovementService = b.get(CashMovementService.class);
        $bean.repository = b.get(AccountingPeriodRepository.class);
      });
    }
  }

}
