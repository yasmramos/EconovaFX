package com.econovafx.service;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class ExchangeRateScheduler$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(ExchangeRateScheduler.class)) {
      var bean = new ExchangeRateScheduler(builder.get(ExchangeRateService.class,"!exchangeRateService"), builder.get(BCCExchangeRateClient.class,"!bccClient"));
      builder.register(bean);
    }
  }

}
