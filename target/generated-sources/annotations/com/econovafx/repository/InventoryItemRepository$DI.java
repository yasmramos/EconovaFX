package com.econovafx.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class InventoryItemRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(InventoryItemRepository.class)) {
      var bean = new InventoryItemRepository(builder.get(Database.class,"!database"));
      builder.register(bean);
    }
  }

}
