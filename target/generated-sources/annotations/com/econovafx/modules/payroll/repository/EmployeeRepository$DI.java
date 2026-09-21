package com.econovafx.modules.payroll.repository;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class EmployeeRepository$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(EmployeeRepository.class)) {
      var bean = new EmployeeRepository(builder.get(Database.class,"!database"));
      builder.register(bean);
    }
  }

}
