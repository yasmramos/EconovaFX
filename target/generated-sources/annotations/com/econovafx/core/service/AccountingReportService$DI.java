package com.econovafx.core.service;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class AccountingReportService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(AccountingReportService.class)) {
      var bean = new AccountingReportService(builder.get(Database.class,"!database"));
      builder.register(bean);
    }
  }

}
