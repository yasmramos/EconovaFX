package com.econovafx.modules.core.ui.web;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class LocalWebServer$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(LocalWebServer.class)) {
      var bean = new LocalWebServer();
      builder.register(bean);
    }
  }

}
