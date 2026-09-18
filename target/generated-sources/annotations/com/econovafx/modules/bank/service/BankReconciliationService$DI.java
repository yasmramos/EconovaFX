package com.econovafx.modules.bank.service;

import com.econovafx.modules.bank.repository.BankReconciliationRepository;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class BankReconciliationService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(BankReconciliationService.class)) {
      var bean = new BankReconciliationService(builder.get(BankReconciliationRepository.class,"!repository"));
      builder.register(bean);
    }
  }

}
