package com.econovafx.modules.reporting.service;

import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.repository.TransactionRepository;
import com.econovafx.modules.reporting.repository.FinancialReportRepository;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class FinancialReportingService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(FinancialReportingService.class)) {
      var bean = new FinancialReportingService(builder.get(FinancialReportRepository.class,"!reportRepository"), builder.get(AccountRepository.class,"!accountRepository"), builder.get(TransactionRepository.class,"!transactionRepository"));
      builder.register(bean);
    }
  }

}
