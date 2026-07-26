package com.econovafx.modules.accounting.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class ExchangeDifferenceRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(ExchangeDifferenceRepository.class)) {
      var bean = new ExchangeDifferenceRepository(builder.get(Database.class,"!database"));
      builder.register(bean);
    }
  }

}
