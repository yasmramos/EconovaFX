package com.econovafx.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to require that the user is authenticated (logged in).
 * Similar to Apache Shiro's @RequiresAuthentication.
 * 
 * Usage:
 * &#64;RequiresAuthentication
 * public void accessProtectedResource() { ... }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresAuthentication {
}
