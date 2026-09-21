package com.econovafx.modules.reporting.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class FinancialReportRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(FinancialReportRepository.class)) {
      var bean = new FinancialReportRepository(builder.get(Database.class,"!database"));
      builder.register(bean);
    }
  }

}
