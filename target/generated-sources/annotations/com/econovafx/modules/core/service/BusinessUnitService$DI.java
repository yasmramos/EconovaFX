package com.econovafx.modules.core.service;

import com.econovafx.modules.core.repository.BusinessUnitRepository;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class BusinessUnitService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(BusinessUnitService.class)) {
      var bean = new BusinessUnitService();
      var $bean = builder.register(bean);
       builder.addInjector(b -> {
         // field and method injection
        $bean.businessUnitRepository = b.get(BusinessUnitRepository.class);
      });
    }
  }

}
