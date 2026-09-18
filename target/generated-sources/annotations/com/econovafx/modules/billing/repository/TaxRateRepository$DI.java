package com.econovafx.modules.billing.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class TaxRateRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(TaxRateRepository.class)) {
      var bean = new TaxRateRepository(builder.get(Database.class,"!database"));
      builder.register(bean);
    }
  }

}
