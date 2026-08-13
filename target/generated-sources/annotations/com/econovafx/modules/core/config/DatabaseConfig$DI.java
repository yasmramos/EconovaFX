package com.econovafx.modules.core.config;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class DatabaseConfig$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(DatabaseConfig.class)) {
      var bean = new DatabaseConfig();
      builder.register(bean);
    }
  }

}
