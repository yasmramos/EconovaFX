package com.econovafx.modules.core.ui.controller;

import com.econovafx.modules.core.service.ExchangeRateService;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class ExchangeRatesController$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(ExchangeRatesController.class)) {
      var bean = new ExchangeRatesController();
      var $bean = builder.register(bean);
       builder.addInjector(b -> {
         // field and method injection
        $bean.exchangeRateService = b.get(ExchangeRateService.class);
      });
    }
  }

}
