package com.econovafx.modules.core.ui.controller;

import com.econovafx.modules.core.service.AuditService;
import com.econovafx.modules.core.service.NotificationService;
import com.econovafx.modules.core.service.SystemConfigService;
import com.econovafx.modules.core.service.backup.BackupSchedulerService;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class SystemSettingsController$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(SystemSettingsController.class)) {
      var bean = new SystemSettingsController();
      var $bean = builder.register(bean);
       builder.addInjector(b -> {
         // field and method injection
        $bean.backupSchedulerService = b.get(BackupSchedulerService.class);
        $bean.auditService = b.get(AuditService.class);
        $bean.notificationService = b.get(NotificationService.class);
        $bean.systemConfigService = b.get(SystemConfigService.class);
      });
    }
  }

}
