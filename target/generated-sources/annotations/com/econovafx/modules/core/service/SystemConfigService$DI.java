package com.econovafx.modules.core.service;

import com.econovafx.modules.core.repository.SystemConfigRepository;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class SystemConfigService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(SystemConfigService.class)) {
      var bean = new SystemConfigService(builder.get(SystemConfigRepository.class,"!repository"));
      builder.register(bean);
    }
  }

}
