package com.econovafx.modules.billing.service;

import com.econovafx.modules.billing.repository.BillingSeriesRepository;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class SequentialNumberService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(SequentialNumberService.class)) {
      var bean = new SequentialNumberService(builder.get(BillingSeriesRepository.class,"!billingSeriesRepository"));
      builder.register(bean);
    }
  }

}
