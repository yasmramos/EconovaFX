package com.econovafx.security;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class AuthService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(AuthService.class)) {
      var bean = new AuthService();
      builder.register(bean);
    }
  }

}
