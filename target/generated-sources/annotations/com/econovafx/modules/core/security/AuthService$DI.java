package com.econovafx.modules.core.security;

import com.econovafx.modules.core.service.AuditService;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class AuthService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(AuthService.class)) {
      var bean = new AuthService(builder.get(PasswordService.class,"!passwordService"), builder.get(AuditService.class,"!auditService"));
      builder.register(bean);
    }
  }

}
