package com.econovafx.modules.core.ui.controller;

import com.econovafx.modules.core.service.AuditService;
import com.econovafx.modules.core.service.NotificationService;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class AuditLogsController$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(AuditLogsController.class)) {
      var bean = new AuditLogsController();
      var $bean = builder.register(bean);
       builder.addInjector(b -> {
         // field and method injection
        $bean.notificationService = b.get(NotificationService.class);
        $bean.auditService = b.get(AuditService.class);
      });
    }
  }

}
