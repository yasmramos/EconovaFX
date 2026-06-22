package com.econovafx.security;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class PasswordService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(PasswordService.class)) {
      var bean = new PasswordService();
      builder.register(bean);
    }
  }

}
