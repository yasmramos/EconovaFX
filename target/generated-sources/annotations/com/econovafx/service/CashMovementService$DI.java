package com.econovafx.service;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class CashMovementService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(CashMovementService.class)) {
      var bean = new CashMovementService();
      builder.register(bean);
    }
  }

}
