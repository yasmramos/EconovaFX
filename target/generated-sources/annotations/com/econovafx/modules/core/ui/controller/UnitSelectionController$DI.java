package com.econovafx.modules.core.ui.controller;

import com.econovafx.modules.core.service.BusinessUnitService;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class UnitSelectionController$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(UnitSelectionController.class)) {
      var bean = new UnitSelectionController();
      var $bean = builder.register(bean);
       builder.addInjector(b -> {
         // field and method injection
        $bean.setBusinessUnitService(b.get(BusinessUnitService.class,"!businessUnitService"));
      });
    }
  }

}
