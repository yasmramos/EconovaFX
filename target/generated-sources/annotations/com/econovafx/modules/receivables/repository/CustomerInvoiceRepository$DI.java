package com.econovafx.modules.receivables.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class CustomerInvoiceRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(CustomerInvoiceRepository.class)) {
      var bean = new CustomerInvoiceRepository(builder.get(Database.class,"!database"));
      builder.register(bean);
    }
  }

}
