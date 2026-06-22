package com.econovafx.service;

import com.econovafx.repository.AccountRepository;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class AccountService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(AccountService.class)) {
      var bean = new AccountService(builder.get(AccountRepository.class,"!accountRepository"));
      builder.register(bean);
    }
  }

}
