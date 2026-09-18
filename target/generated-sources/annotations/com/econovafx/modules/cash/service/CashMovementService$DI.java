package com.econovafx.modules.cash.service;

import com.econovafx.modules.bank.repository.BankAccountRepository;
import com.econovafx.modules.cash.repository.CashBoxRepository;
import com.econovafx.modules.cash.repository.CashMovementRepository;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class CashMovementService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(CashMovementService.class)) {
      var bean = new CashMovementService(builder.get(CashMovementRepository.class,"!movementRepository"), builder.get(BankAccountRepository.class,"!bankAccountRepository"), builder.get(CashBoxRepository.class,"!cashBoxRepository"));
      builder.register(bean);
    }
  }

}
