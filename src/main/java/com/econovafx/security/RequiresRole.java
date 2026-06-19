package com.econovafx.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to restrict method access based on user roles.
 * Similar to Apache Shiro's @RequiresRoles.
 * 
 * Usage:
 * &#64;RequiresRole("ADMIN")
 * public void deleteCompany(Long id) { ... }
 * 
 * &#64;RequiresRole(value = {"ADMIN", "MANAGER"}, logical = Logical.OR)
 * public void approveTransaction(Transaction t) { ... }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
    
    /**
     * The role(s) required to execute the annotated method.
     */
    String[] value();
    
    /**
     * Logical operator to apply when multiple roles are specified.
     * AND = user must have ALL roles
     * OR = user must have AT LEAST ONE role
     */
    Logical logical() default Logical.AND;
    
    enum Logical {
        AND,
        OR
    }
}
