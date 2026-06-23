package com.econovafx.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class SystemConfigRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(SystemConfigRepository.class)) {
      var bean = new SystemConfigRepository(builder.get(Database.class,"!database"));
      builder.register(bean);
    }
  }

}
