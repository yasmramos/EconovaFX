package com.econovafx.modules.core.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to restrict method access based on permissions.
 * Similar to Apache Shiro's @RequiresPermission.
 * 
 * Usage:
 * &#64;RequiresPermission("account:create")
 * public void createAccount(Account account) { ... }
 * 
 * &#64;RequiresPermission(value = "account:delete", logical = Logical.OR)
 * public void deleteAccount(Long id) { ... }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    
    /**
     * The permission code(s) required to execute the annotated method.
     * Uses the format "module:action" (e.g., "account:create", "user:delete").
     */
    String[] value();
    
    /**
     * Logical operator to apply when multiple permissions are specified.
     * AND = user must have ALL permissions
     * OR = user must have AT LEAST ONE permission
     */
    Logical logical() default Logical.AND;
    
    enum Logical {
        AND,
        OR
    }
}
