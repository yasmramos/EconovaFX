package com.econovafx.modules.inventory.service;

import com.econovafx.modules.inventory.repository.InventoryItemRepository;
import com.econovafx.modules.inventory.repository.InventoryMovementRepository;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class InventoryReportService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(InventoryReportService.class)) {
      var bean = new InventoryReportService();
      var $bean = builder.register(bean);
       builder.addInjector(b -> {
         // field and method injection
        $bean.movementRepository = b.get(InventoryMovementRepository.class);
        $bean.itemRepository = b.get(InventoryItemRepository.class);
      });
    }
  }

}
