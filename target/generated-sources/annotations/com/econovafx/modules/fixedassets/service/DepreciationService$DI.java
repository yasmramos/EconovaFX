package com.econovafx.modules.fixedassets.service;

import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.service.TransactionService;
import com.econovafx.modules.core.service.AuditService;
import com.econovafx.modules.fixedassets.repository.DepreciationRecordRepository;
import com.econovafx.modules.fixedassets.repository.FixedAssetRepository;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class DepreciationService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(DepreciationService.class)) {
      var bean = new DepreciationService(builder.get(FixedAssetRepository.class,"!fixedAssetRepository"), builder.get(DepreciationRecordRepository.class,"!depreciationRecordRepository"), builder.get(TransactionService.class,"!transactionService"), builder.get(AccountRepository.class,"!accountRepository"), builder.get(AuditService.class,"!auditService"));
      builder.register(bean);
    }
  }

}
