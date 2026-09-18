package com.econovafx.modules.reporting.controller;

import com.econovafx.modules.reporting.service.FinancialReportingService;
import com.econovafx.modules.reporting.service.consolidation.ConsolidationService;
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
        $bean.consolidationService = b.get(ConsolidationService.class);
        $bean.reportingService = b.get(FinancialReportingService.class);
      });
    }
  }

}
