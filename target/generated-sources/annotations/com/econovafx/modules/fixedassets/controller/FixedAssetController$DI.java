package com.econovafx.modules.fixedassets.controller;

import com.econovafx.modules.fixedassets.repository.DepreciationRecordRepository;
import com.econovafx.modules.fixedassets.repository.FixedAssetRepository;
import com.econovafx.modules.fixedassets.service.DepreciationService;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class FixedAssetController$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(FixedAssetController.class)) {
      var bean = new FixedAssetController();
      var $bean = builder.register(bean);
       builder.addInjector(b -> {
         // field and method injection
        $bean.depreciationRecordRepository = b.get(DepreciationRecordRepository.class);
        $bean.fixedAssetRepository = b.get(FixedAssetRepository.class);
        $bean.depreciationService = b.get(DepreciationService.class);
      });
    }
  }

}
