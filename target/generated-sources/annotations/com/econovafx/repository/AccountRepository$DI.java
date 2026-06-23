package com.econovafx.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class AccountRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(AccountRepository.class)) {
      var bean = new AccountRepository(builder.get(Database.class,"!database"));
      builder.register(bean);
    }
  }

}
