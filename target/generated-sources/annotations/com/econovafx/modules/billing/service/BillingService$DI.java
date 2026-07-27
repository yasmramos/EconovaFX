package com.econovafx.modules.billing.service;

import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.service.TransactionService;
import com.econovafx.modules.billing.repository.BillingSeriesRepository;
import com.econovafx.modules.billing.repository.SalesInvoiceRepository;
import com.econovafx.modules.billing.repository.TaxRateRepository;
import com.econovafx.modules.core.service.AuditService;
import com.econovafx.modules.inventory.service.InventoryService;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class BillingService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(BillingService.class)) {
      var bean = new BillingService(builder.get(SalesInvoiceRepository.class,"!salesInvoiceRepository"), builder.get(BillingSeriesRepository.class,"!billingSeriesRepository"), builder.get(SequentialNumberService.class,"!sequentialNumberService"), builder.get(TransactionService.class,"!transactionService"), builder.get(InventoryService.class,"!inventoryService"), builder.get(AccountRepository.class,"!accountRepository"), builder.get(TaxRateRepository.class,"!taxRateRepository"), builder.get(AuditService.class,"!auditService"));
      builder.register(bean);
    }
  }

}
