package com.econovafx.modules.cash.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class CashBoxRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(CashBoxRepository.class)) {
      var bean = new CashBoxRepository(builder.get(Database.class,"!database"));
      builder.register(bean);
    }
  }

}
