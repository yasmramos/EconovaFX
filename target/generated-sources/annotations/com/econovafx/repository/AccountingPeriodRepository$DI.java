package com.econovafx.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class AccountingPeriodRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(AccountingPeriodRepository.class)) {
      var bean = new AccountingPeriodRepository();
      var $bean = builder.register(bean);
       builder.addInjector(b -> {
         // field and method injection
        $bean.database = b.get(Database.class);
      });
    }
  }

}
