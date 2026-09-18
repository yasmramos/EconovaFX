package com.econovafx.modules.core.ui.controller;

import com.econovafx.modules.core.service.CompanyService;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class CompanySelectionController$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(CompanySelectionController.class)) {
      var bean = new CompanySelectionController();
      var $bean = builder.register(bean);
       builder.addInjector(b -> {
         // field and method injection
        $bean.setCompanyService(b.get(CompanyService.class,"!companyService"));
      });
    }
  }

}
