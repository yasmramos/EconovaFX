package com.econovafx.modules.receivables.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class CustomerPaymentRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(CustomerPaymentRepository.class)) {
      var bean = new CustomerPaymentRepository(builder.get(Database.class,"!database"));
      builder.register(bean);
    }
  }

}
