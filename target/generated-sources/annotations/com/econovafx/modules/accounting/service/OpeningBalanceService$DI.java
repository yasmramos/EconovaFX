package com.econovafx.modules.accounting.service;

import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.repository.ClosingEntryRepository;
import com.econovafx.modules.accounting.repository.TransactionRepository;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class OpeningBalanceService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(OpeningBalanceService.class)) {
      var bean = new OpeningBalanceService(builder.get(TransactionRepository.class,"!transactionRepository"), builder.get(AccountRepository.class,"!accountRepository"), builder.get(TransactionService.class,"!transactionService"), builder.get(ClosingEntryRepository.class,"!closingEntryRepository"));
      builder.register(bean);
    }
  }

}
