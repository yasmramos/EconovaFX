package com.econovafx.modules.cash.controller;

import com.econovafx.modules.bank.repository.BankAccountRepository;
import com.econovafx.modules.bank.repository.BankReconciliationRepository;
import com.econovafx.modules.bank.service.BankReconciliationService;
import com.econovafx.modules.cash.repository.CashBoxRepository;
import com.econovafx.modules.cash.repository.CashMovementRepository;
import com.econovafx.modules.cash.service.CashMovementService;
import com.econovafx.modules.core.service.ExportService;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class CashModuleController$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(CashModuleController.class)) {
      var bean = new CashModuleController(builder.get(BankAccountRepository.class,"!bankAccountRepository"), builder.get(CashBoxRepository.class,"!cashBoxRepository"), builder.get(CashMovementRepository.class,"!cashMovementRepository"), builder.get(BankReconciliationRepository.class,"!bankReconciliationRepository"), builder.get(CashMovementService.class,"!cashMovementService"), builder.get(BankReconciliationService.class,"!bankReconciliationService"), builder.get(ExportService.class,"!exportService"));
      var $bean = builder.register(bean);
       builder.addInjector(b -> {
         // field and method injection
        $bean.exportService = b.get(ExportService.class);
        $bean.bankReconciliationService = b.get(BankReconciliationService.class);
        $bean.cashMovementService = b.get(CashMovementService.class);
        $bean.bankReconciliationRepository = b.get(BankReconciliationRepository.class);
        $bean.cashMovementRepository = b.get(CashMovementRepository.class);
        $bean.cashBoxRepository = b.get(CashBoxRepository.class);
        $bean.bankAccountRepository = b.get(BankAccountRepository.class);
      });
    }
  }

}
