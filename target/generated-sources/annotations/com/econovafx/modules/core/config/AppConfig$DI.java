package com.econovafx.modules.core.config;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class AppConfig$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(AppConfig.class)) {
      var bean = new AppConfig();
      builder.register(bean);
    }
  }

}
