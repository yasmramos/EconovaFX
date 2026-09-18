package com.econovafx.modules.core.config;

import io.avaje.inject.aop.MethodInterceptor;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;

@SuppressWarnings("all")
@Generated("io.avaje.inject.generator")
public final class SecurityModule$DI  {

  public static void build(Builder builder) {
    if (builder.isBeanAbsent(SecurityModule.class)) {
      var bean = new SecurityModule();
      builder.register(bean);
    }
  }

  /**
   * Create and register MethodInterceptor via factory bean method SecurityModule#tenantValidationInterceptor().
   */
  public static void build_tenantValidationInterceptor(Builder builder) {
    if (builder.isBeanAbsent("tenantValidation", MethodInterceptor.class)) {
      var factory = builder.get(SecurityModule.class);
      var bean = factory.tenantValidationInterceptor();
      builder.register(bean);
    }
  }

  /**
   * Create and register MethodInterceptor via factory bean method SecurityModule#authenticationInterceptor().
   */
  public static void build_authenticationInterceptor(Builder builder) {
    if (builder.isBeanAbsent("authentication", MethodInterceptor.class)) {
      var factory = builder.get(SecurityModule.class);
      var bean = factory.authenticationInterceptor();
      builder.register(bean);
    }
  }

  /**
   * Create and register MethodInterceptor via factory bean method SecurityModule#roleInterceptor().
   */
  public static void build_roleInterceptor(Builder builder) {
    if (builder.isBeanAbsent("role", MethodInterceptor.class)) {
      var factory = builder.get(SecurityModule.class);
      var bean = factory.roleInterceptor();
      builder.register(bean);
    }
  }

  /**
   * Create and register MethodInterceptor via factory bean method SecurityModule#permissionInterceptor().
   */
  public static void build_permissionInterceptor(Builder builder) {
    if (builder.isBeanAbsent("permission", MethodInterceptor.class)) {
      var factory = builder.get(SecurityModule.class);
      var bean = factory.permissionInterceptor();
      builder.register(bean);
    }
  }

}
