package com.econovafx.modules.inventory.service;

import com.econovafx.modules.accounting.service.TransactionService;
import com.econovafx.modules.core.service.AuditService;
import com.econovafx.modules.inventory.repository.InventoryCategoryRepository;
import com.econovafx.modules.inventory.repository.InventoryItemRepository;
import com.econovafx.modules.inventory.repository.InventoryMovementRepository;
import com.econovafx.modules.inventory.repository.WarehouseRepository;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class InventoryService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(InventoryService.class)) {
      var bean = new InventoryService();
      var $bean = builder.register(bean);
       builder.addInjector(b -> {
         // field and method injection
        $bean.auditService = b.get(AuditService.class);
        $bean.transactionService = b.get(TransactionService.class);
        $bean.warehouseRepository = b.get(WarehouseRepository.class);
        $bean.movementRepository = b.get(InventoryMovementRepository.class);
        $bean.categoryRepository = b.get(InventoryCategoryRepository.class);
        $bean.itemRepository = b.get(InventoryItemRepository.class);
      });
    }
  }

}
