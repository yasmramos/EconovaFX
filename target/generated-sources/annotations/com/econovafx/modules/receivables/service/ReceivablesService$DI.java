package com.econovafx.modules.receivables.service;

import com.econovafx.modules.accounting.service.TransactionService;
import com.econovafx.modules.billing.service.ThirdPartyService;
import com.econovafx.modules.core.config.UserContext;
import com.econovafx.modules.receivables.repository.CustomerInvoiceRepository;
import com.econovafx.modules.receivables.repository.CustomerPaymentRepository;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class ReceivablesService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(ReceivablesService.class)) {
      var bean = new ReceivablesService(builder.get(CustomerInvoiceRepository.class,"!invoiceRepository"), builder.get(CustomerPaymentRepository.class,"!paymentRepository"), builder.get(ThirdPartyService.class,"!thirdPartyService"), builder.get(TransactionService.class,"!transactionService"), builder.get(UserContext.class,"!userContext"));
      builder.register(bean);
    }
  }

}
