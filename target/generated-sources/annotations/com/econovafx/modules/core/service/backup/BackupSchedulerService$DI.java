package com.econovafx.modules.core.service.backup;

import com.econovafx.modules.core.service.CompanyService;
import com.econovafx.modules.core.service.SystemConfigService;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class BackupSchedulerService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(BackupSchedulerService.class)) {
      var bean = new BackupSchedulerService(builder.get(SystemConfigService.class,"!systemConfigService"), builder.get(TenantBackupService.class,"!tenantBackupService"), builder.get(CompanyService.class,"!companyService"));
      builder.register(bean);
    }
  }

}
