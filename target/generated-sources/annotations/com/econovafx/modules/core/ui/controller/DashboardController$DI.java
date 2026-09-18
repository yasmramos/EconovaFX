package com.econovafx.modules.core.ui.controller;

import com.econovafx.modules.accounting.service.AccountService;
import com.econovafx.modules.accounting.service.TransactionService;
import com.econovafx.modules.core.service.SystemConfigService;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import javafx.fxml.Initializable;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class DashboardController$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(DashboardController.class, Initializable.class)) {
      var bean = new DashboardController(builder.get(AccountService.class,"!accountService"), builder.get(TransactionService.class,"!transactionService"), builder.get(SystemConfigService.class,"!systemConfigService"));
      builder.register(bean);
    }
  }

}
