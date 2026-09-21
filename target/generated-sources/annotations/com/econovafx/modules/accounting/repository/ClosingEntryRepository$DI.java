package com.econovafx.modules.accounting.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class ClosingEntryRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(ClosingEntryRepository.class)) {
      var bean = new ClosingEntryRepository();
      var $bean = builder.register(bean);
       builder.addInjector(b -> {
         // field and method injection
        $bean.database = b.get(Database.class);
      });
    }
  }

}
