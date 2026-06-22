package com.econovafx.config;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.ebean.Database;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class DatabaseFactory$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(DatabaseFactory.class)) {
      var bean = new DatabaseFactory();
      builder.register(bean);
    }
  }

  /**
   * Create and register Database via factory bean method DatabaseFactory#database().
   */
  public static void build_database(Builder builder) {
    if (builder.isBeanAbsent(Database.class)) {
      var factory = builder.get(DatabaseFactory.class);
      var bean = factory.database();
      var $bean = builder.register(bean);
      builder.addPreDestroy($bean::shutdown, 2147483647);
    }
  }

}
