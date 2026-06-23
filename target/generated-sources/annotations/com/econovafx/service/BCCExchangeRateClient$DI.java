package com.econovafx.service;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class BCCExchangeRateClient$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(BCCExchangeRateClient.class)) {
      var bean = new BCCExchangeRateClient();
      builder.register(bean);
    }
  }

}
