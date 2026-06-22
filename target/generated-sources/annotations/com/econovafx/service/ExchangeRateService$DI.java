package com.econovafx.service;

import com.econovafx.repository.CurrencyRepository;
import com.econovafx.repository.ExchangeRateRepository;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class ExchangeRateService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(ExchangeRateService.class)) {
      var bean = new ExchangeRateService(builder.get(ExchangeRateRepository.class,"!exchangeRateRepository"), builder.get(CurrencyRepository.class,"!currencyRepository"), builder.get(BCCExchangeRateFetcher.class,"!bccFetcher"));
      builder.register(bean);
    }
  }

}
