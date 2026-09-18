package com.econovafx.modules.core.config;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class DatabaseSeeder$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(DatabaseSeeder.class)) {
      var bean = new DatabaseSeeder(builder.get(Database.class,"!database"));
      builder.register(bean);
    }
  }

}
