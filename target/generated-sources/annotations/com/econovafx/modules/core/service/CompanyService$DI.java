package com.econovafx.modules.core.service;

import com.econovafx.modules.core.repository.CompanyRepository;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class CompanyService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(CompanyService.class)) {
      var bean = new CompanyService();
      var $bean = builder.register(bean);
       builder.addInjector(b -> {
         // field and method injection
        $bean.companyRepository = b.get(CompanyRepository.class);
      });
    }
  }

}
