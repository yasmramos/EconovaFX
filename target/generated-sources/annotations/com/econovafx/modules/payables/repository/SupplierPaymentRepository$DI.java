package com.econovafx.modules.payables.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class SupplierPaymentRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(SupplierPaymentRepository.class)) {
      var bean = new SupplierPaymentRepository(builder.get(Database.class,"!database"));
      builder.register(bean);
    }
  }

}
