package com.econovafx.service;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class BCCExchangeRateFetcher$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(BCCExchangeRateFetcher.class)) {
      var bean = new BCCExchangeRateFetcher();
      builder.register(bean);
    }
  }

}
