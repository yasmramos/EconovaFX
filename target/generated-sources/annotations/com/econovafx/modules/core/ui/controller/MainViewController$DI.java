package com.econovafx.modules.core.ui.controller;

import com.econovafx.modules.accounting.service.AccountService;
import com.econovafx.modules.accounting.service.TransactionService;
import com.econovafx.modules.core.service.UserService;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import javafx.fxml.Initializable;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class MainViewController$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(MainViewController.class, Initializable.class)) {
      var bean = new MainViewController(builder.get(AccountService.class,"!accountService"), builder.get(TransactionService.class,"!transactionService"), builder.get(UserService.class,"!userService"));
      builder.register(bean);
    }
  }

}
