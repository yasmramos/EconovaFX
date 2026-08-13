package com.econovafx.modules.bank.service;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class BankAccountService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(BankAccountService.class)) {
      var bean = new BankAccountService();
      builder.register(bean);
    }
  }

}
