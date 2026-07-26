package com.econovafx.modules.bank.service;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class BankReconciliationService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(BankReconciliationService.class)) {
      var bean = new BankReconciliationService();
      builder.register(bean);
    }
  }

}
