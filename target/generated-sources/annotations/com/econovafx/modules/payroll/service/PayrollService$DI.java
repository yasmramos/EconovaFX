package com.econovafx.modules.payroll.service;

import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.service.TransactionService;
import com.econovafx.modules.core.service.SystemConfigService;
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
        $bean.accountRepository = b.get(AccountRepository.class);
        $bean.transactionService = b.get(TransactionService.class);
        $bean.systemConfigService = b.get(SystemConfigService.class);
        $bean.conceptRepository = b.get(PayrollConceptRepository.class);
        $bean.employeeRepository = b.get(EmployeeRepository.class);
      });
    }
  }

}
