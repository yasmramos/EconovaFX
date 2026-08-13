package com.econovafx.modules.payables.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class SupplierInvoiceRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(SupplierInvoiceRepository.class)) {
      var bean = new SupplierInvoiceRepository(builder.get(Database.class,"!database"));
      builder.register(bean);
    }
  }

}
