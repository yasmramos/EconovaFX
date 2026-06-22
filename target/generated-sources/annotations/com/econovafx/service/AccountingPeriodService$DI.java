package com.econovafx.service;

import com.econovafx.repository.AccountingPeriodRepository;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class AccountingPeriodService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(AccountingPeriodService.class)) {
      var bean = new AccountingPeriodService();
      var $bean = builder.register(bean);
       builder.addInjector(b -> {
         // field and method injection
        $bean.inventoryService = b.get(InventoryService.class);
        $bean.cashMovementService = b.get(CashMovementService.class);
        $bean.repository = b.get(AccountingPeriodRepository.class);
      });
    }
  }

}
