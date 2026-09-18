package com.econovafx.modules.accounting.service;

import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.repository.ExchangeDifferenceRepository;
import com.econovafx.modules.core.repository.ExchangeRateRepository;
import com.econovafx.modules.core.service.AuditService;
import com.econovafx.modules.core.service.SystemConfigService;
import com.econovafx.modules.payables.repository.SupplierInvoiceRepository;
import com.econovafx.modules.receivables.repository.CustomerInvoiceRepository;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class ExchangeRevaluationService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(ExchangeRevaluationService.class)) {
      var bean = new ExchangeRevaluationService(builder.get(ExchangeDifferenceRepository.class,"!exchangeDifferenceRepository"), builder.get(ExchangeRateRepository.class,"!exchangeRateRepository"), builder.get(TransactionService.class,"!transactionService"), builder.get(AccountRepository.class,"!accountRepository"), builder.get(AuditService.class,"!auditService"), builder.get(SystemConfigService.class,"!systemConfigService"), builder.get(SupplierInvoiceRepository.class,"!supplierInvoiceRepository"), builder.get(CustomerInvoiceRepository.class,"!customerInvoiceRepository"));
      builder.register(bean);
    }
  }

}
