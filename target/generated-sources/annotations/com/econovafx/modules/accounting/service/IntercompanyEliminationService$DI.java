package com.econovafx.modules.accounting.service;

import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.repository.TransactionRepository;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class IntercompanyEliminationService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(IntercompanyEliminationService.class)) {
      var bean = new IntercompanyEliminationService(builder.get(TransactionRepository.class,"!transactionRepository"), builder.get(TransactionService.class,"!transactionService"), builder.get(AccountRepository.class,"!accountRepository"));
      builder.register(bean);
    }
  }

}
