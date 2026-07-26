package com.econovafx.modules.accounting.service;

import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.repository.TransactionRepository;
import com.econovafx.modules.core.service.AuditService;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class TransactionService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(TransactionService.class)) {
      var bean = new TransactionService(builder.get(TransactionRepository.class,"!transactionRepository"), builder.get(AccountRepository.class,"!accountRepository"), builder.get(AuditService.class,"!auditService"));
      builder.register(bean);
    }
  }

}
