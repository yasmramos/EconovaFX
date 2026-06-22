package com.econovafx.service;

import com.econovafx.repository.AccountRepository;
import com.econovafx.repository.TransactionRepository;
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
