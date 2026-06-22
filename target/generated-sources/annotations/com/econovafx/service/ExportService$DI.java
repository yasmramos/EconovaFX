package com.econovafx.service;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class ExportService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(ExportService.class)) {
      var bean = new ExportService();
      builder.register(bean);
    }
  }

}
