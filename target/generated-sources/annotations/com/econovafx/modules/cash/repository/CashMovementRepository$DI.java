package com.econovafx.modules.cash.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class CashMovementRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(CashMovementRepository.class)) {
      var bean = new CashMovementRepository(builder.get(Database.class,"!database"));
      builder.register(bean);
    }
  }

}
