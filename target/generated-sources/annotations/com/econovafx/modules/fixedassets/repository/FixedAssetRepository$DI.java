package com.econovafx.modules.fixedassets.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class FixedAssetRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(FixedAssetRepository.class)) {
      var bean = new FixedAssetRepository(builder.get(Database.class,"!database"));
      builder.register(bean);
    }
  }

}
