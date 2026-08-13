package com.econovafx.modules.billing.service;

import com.econovafx.modules.billing.repository.ThirdPartyRepository;
import com.econovafx.modules.core.config.UserContext;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class ThirdPartyService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(ThirdPartyService.class)) {
      var bean = new ThirdPartyService(builder.get(ThirdPartyRepository.class,"!thirdPartyRepository"), builder.get(UserContext.class,"!userContext"));
      builder.register(bean);
    }
  }

}
