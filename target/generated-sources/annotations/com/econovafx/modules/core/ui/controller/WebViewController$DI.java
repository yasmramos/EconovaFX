package com.econovafx.modules.core.ui.controller;

import com.econovafx.modules.core.ui.web.LocalWebServer;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import javafx.fxml.Initializable;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class WebViewController$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(WebViewController.class, Initializable.class)) {
      var bean = new WebViewController(builder.get(LocalWebServer.class,"!localWebServer"));
      builder.register(bean);
    }
  }

}
