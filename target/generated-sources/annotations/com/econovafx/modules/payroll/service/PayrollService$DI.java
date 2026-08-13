package com.econovafx.modules.payroll.service;

import com.econovafx.modules.payroll.repository.EmployeeRepository;
import com.econovafx.modules.payroll.repository.PayrollConceptRepository;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class PayrollService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(PayrollService.class)) {
      var bean = new PayrollService();
      var $bean = builder.register(bean);
       builder.addInjector(b -> {
         // field and method injection
        $bean.conceptRepository = b.get(PayrollConceptRepository.class);
        $bean.employeeRepository = b.get(EmployeeRepository.class);
      });
    }
  }

}
