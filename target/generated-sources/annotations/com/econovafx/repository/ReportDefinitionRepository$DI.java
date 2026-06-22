package com.econovafx.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class ReportDefinitionRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(ReportDefinitionRepository.class)) {
      var bean = new ReportDefinitionRepository(builder.get(Database.class,"!database"));
      builder.register(bean);
    }
  }

}
