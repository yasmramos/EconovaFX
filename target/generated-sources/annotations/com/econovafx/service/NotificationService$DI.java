package com.econovafx.service;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class NotificationService$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(NotificationService.class)) {
      var bean = new NotificationService();
      builder.register(bean);
    }
  }

}
