package com.econovafx.modules.reporting.service.consolidation;

import com.econovafx.modules.accounting.service.FinancialStatementService;
import com.econovafx.modules.accounting.service.IntercompanyEliminationService;
import com.econovafx.modules.core.service.CompanyService;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class ConsolidationService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(ConsolidationService.class)) {
      var bean = new ConsolidationService(builder.get(CompanyService.class,"!companyService"), builder.get(FinancialStatementService.class,"!financialStatementService"), builder.get(IntercompanyEliminationService.class,"!intercompanyEliminationService"));
      builder.register(bean);
    }
  }

}
