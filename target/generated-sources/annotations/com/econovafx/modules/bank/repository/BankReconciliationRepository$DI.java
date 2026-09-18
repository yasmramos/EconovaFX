package com.econovafx.modules.bank.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class BankReconciliationRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(BankReconciliationRepository.class)) {
      var bean = new BankReconciliationRepository(builder.get(Database.class,"!database"));
      builder.register(bean);
    }
  }

}
