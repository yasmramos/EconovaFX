package com.econovafx.security;

import java.lang.annotation.*;

/**
 * Annotation que requiere un tenant activo en el contexto para ejecutar el método.
 * 
 * Se usa en servicios y repositorios para garantizar que todas las operaciones
 * se ejecuten dentro de un contexto multi-tenant válido.
 * 
 * Ejemplo:
 * <pre>
 * {@code @RequiresTenant}
 * public Transaction createTransaction(Transaction tx) { ... }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresTenant {
}
