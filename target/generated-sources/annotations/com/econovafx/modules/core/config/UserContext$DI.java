package com.econovafx.modules.core.config;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class UserContext$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(UserContext.class)) {
      var bean = new UserContext();
      builder.register(bean);
    }
  }

}
