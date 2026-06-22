package com.econovafx.service;

import com.econovafx.repository.ThirdPartyRepository;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class ThirdPartyService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(ThirdPartyService.class)) {
      var bean = new ThirdPartyService(builder.get(ThirdPartyRepository.class,"!thirdPartyRepository"));
      builder.register(bean);
    }
  }

}
