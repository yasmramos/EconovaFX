package com.econovafx.modules.cash.controller;

import com.econovafx.modules.bank.repository.BankAccountRepository;
import com.econovafx.modules.bank.repository.BankReconciliationRepository;
import com.econovafx.modules.cash.repository.CashBoxRepository;
import com.econovafx.modules.cash.repository.CashMovementRepository;
import com.econovafx.modules.cash.service.CashMovementService;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class CashBankController$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(CashBankController.class)) {
      var bean = new CashBankController(builder.get(CashMovementService.class,"!cashMovementService"), builder.get(BankAccountRepository.class,"!bankAccountRepository"), builder.get(CashBoxRepository.class,"!cashBoxRepository"), builder.get(CashMovementRepository.class,"!cashMovementRepository"), builder.get(BankReconciliationRepository.class,"!bankReconciliationRepository"));
      var $bean = builder.register(bean);
       builder.addInjector(b -> {
         // field and method injection
        $bean.bankReconciliationRepository = b.get(BankReconciliationRepository.class);
        $bean.cashMovementRepository = b.get(CashMovementRepository.class);
        $bean.cashBoxRepository = b.get(CashBoxRepository.class);
        $bean.bankAccountRepository = b.get(BankAccountRepository.class);
        $bean.cashMovementService = b.get(CashMovementService.class);
      });
    }
  }

}
