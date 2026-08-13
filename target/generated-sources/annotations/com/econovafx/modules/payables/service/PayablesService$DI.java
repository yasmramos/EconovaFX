package com.econovafx.modules.payables.service;

import com.econovafx.modules.accounting.service.TransactionService;
import com.econovafx.modules.billing.service.ThirdPartyService;
import com.econovafx.modules.core.config.UserContext;
import com.econovafx.modules.payables.repository.SupplierInvoiceRepository;
import com.econovafx.modules.payables.repository.SupplierPaymentRepository;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class PayablesService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(PayablesService.class)) {
      var bean = new PayablesService(builder.get(SupplierInvoiceRepository.class,"!invoiceRepository"), builder.get(SupplierPaymentRepository.class,"!paymentRepository"), builder.get(ThirdPartyService.class,"!thirdPartyService"), builder.get(TransactionService.class,"!transactionService"), builder.get(UserContext.class,"!userContext"));
      builder.register(bean);
    }
  }

}
