package com.econovafx.modules.core.service.backup;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class TenantBackupService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(TenantBackupService.class)) {
      var bean = new TenantBackupService();
      builder.register(bean);
    }
  }

}
