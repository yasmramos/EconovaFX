package com.econovafx.service;

import com.econovafx.repository.AuditLogRepository;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class AuditService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(AuditService.class)) {
      var bean = new AuditService(builder.get(AuditLogRepository.class,"!auditLogRepository"));
      builder.register(bean);
    }
  }

}
