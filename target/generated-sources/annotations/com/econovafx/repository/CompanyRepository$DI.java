package com.econovafx.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class CompanyRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(CompanyRepository.class)) {
      var bean = new CompanyRepository();
      builder.register(bean);
    }
  }

}
