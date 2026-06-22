package com.econovafx.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class FinancialStatementRowRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(FinancialStatementRowRepository.class)) {
      var bean = new FinancialStatementRowRepository(builder.get(Database.class,"!database"));
      builder.register(bean);
    }
  }

}
