package com.econovafx.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class InventoryCategoryRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(InventoryCategoryRepository.class)) {
      var bean = new InventoryCategoryRepository(builder.get(Database.class,"!database"));
      builder.register(bean);
    }
  }

}
