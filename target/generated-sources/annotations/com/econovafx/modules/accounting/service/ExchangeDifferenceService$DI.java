package com.econovafx.modules.accounting.service;

import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.repository.ExchangeDifferenceRepository;
import com.econovafx.modules.core.repository.ExchangeRateRepository;
import com.econovafx.modules.core.service.AuditService;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class ExchangeDifferenceService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(ExchangeDifferenceService.class)) {
      var bean = new ExchangeDifferenceService(builder.get(ExchangeDifferenceRepository.class,"!exchangeDifferenceRepository"), builder.get(ExchangeRateRepository.class,"!exchangeRateRepository"), builder.get(TransactionService.class,"!transactionService"), builder.get(AccountRepository.class,"!accountRepository"), builder.get(AuditService.class,"!auditService"));
      builder.register(bean);
    }
  }

}
