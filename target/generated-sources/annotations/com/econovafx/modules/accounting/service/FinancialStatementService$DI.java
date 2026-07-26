package com.econovafx.modules.accounting.service;

import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.repository.FinancialStatementModelRepository;
import com.econovafx.modules.accounting.repository.FinancialStatementRowRepository;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class FinancialStatementService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(FinancialStatementService.class)) {
      var bean = new FinancialStatementService(builder.get(FinancialStatementModelRepository.class,"!modelRepository"), builder.get(FinancialStatementRowRepository.class,"!rowRepository"), builder.get(AccountRepository.class,"!accountRepository"));
      builder.register(bean);
    }
  }

}
