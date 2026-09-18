package com.econovafx.modules.security.ui.controller;

import com.econovafx.modules.core.security.AuthService;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class LoginController$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(LoginController.class)) {
      var bean = new LoginController(builder.get(AuthService.class,"!authService"));
      builder.register(bean);
    }
  }

}
