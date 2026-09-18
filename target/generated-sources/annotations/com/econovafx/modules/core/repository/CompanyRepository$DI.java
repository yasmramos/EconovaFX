package com.econovafx.modules.core.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class CompanyRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(CompanyRepository.class)) {
      var bean = new CompanyRepository(builder.get(Database.class,"!database"));
      builder.register(bean);
    }
  }

}
