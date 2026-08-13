package com.econovafx.modules.payroll.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class PayrollConceptRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(PayrollConceptRepository.class)) {
      var bean = new PayrollConceptRepository();
      builder.register(bean);
    }
  }

}
