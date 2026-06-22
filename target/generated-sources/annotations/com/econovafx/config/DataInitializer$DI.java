package com.econovafx.config;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class DataInitializer$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(DataInitializer.class)) {
      var bean = new DataInitializer();
      builder.register(bean);
    }
  }

}
