package com.econovafx.modules.reporting.controller;

import com.econovafx.modules.reporting.service.FinancialReportingService;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class FinancialReportingController$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(FinancialReportingController.class)) {
      var bean = new FinancialReportingController();
      var $bean = builder.register(bean);
       builder.addInjector(b -> {
         // field and method injection
        $bean.reportingService = b.get(FinancialReportingService.class);
      });
    }
  }

}
