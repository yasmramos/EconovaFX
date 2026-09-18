package com.econovafx.modules.core.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class BusinessUnitRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(BusinessUnitRepository.class)) {
      var bean = new BusinessUnitRepository(builder.get(Database.class,"!database"));
      builder.register(bean);
    }
  }

}
