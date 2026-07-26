package com.econovafx.modules.fixedassets.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class DepreciationRecordRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(DepreciationRecordRepository.class)) {
      var bean = new DepreciationRecordRepository(builder.get(Database.class,"!database"));
      builder.register(bean);
    }
  }

}
