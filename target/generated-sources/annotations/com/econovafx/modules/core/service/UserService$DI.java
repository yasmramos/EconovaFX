package com.econovafx.modules.core.service;

import com.econovafx.modules.core.repository.UserRepository;
import com.econovafx.modules.core.security.PasswordService;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class UserService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(UserService.class)) {
      var bean = new UserService(builder.get(UserRepository.class,"!userRepository"), builder.get(AuditService.class,"!auditService"), builder.get(PasswordService.class,"!passwordService"));
      builder.register(bean);
    }
  }

}
