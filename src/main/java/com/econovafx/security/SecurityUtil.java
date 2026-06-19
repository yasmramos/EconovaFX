package com.econovafx.security;

import com.econovafx.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Utilidad para verificar permisos de usuario en el sistema.
 * Proporciona métodos para validar acceso a operaciones específicas.
 */
public class SecurityUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);
    
    /**
     * Verifica si un usuario tiene un permiso específico
     * @param user Usuario a verificar
     * @param permissionCode Código del permiso
     * @return true si el usuario tiene el permiso, false en caso contrario
     */
    public static boolean hasPermission(User user, String permissionCode) {
        if (user == null) {
            logger.warn("Intento de verificar permiso con usuario nulo: {}", permissionCode);
            return false;
        }
        
        if (user.getRole() == null) {
            logger.warn("Usuario sin rol definido: {}", user.getUsername());
            return false;
        }
        
        boolean hasPermission = Permissions.hasPermission(user.getRole().name(), permissionCode);
        
        if (!hasPermission) {
            logger.warn("Usuario {} no tiene permiso {}: rol={}", 
                       user.getUsername(), permissionCode, user.getRole());
        }
        
        return hasPermission;
    }
    
    /**
     * Verifica si un usuario tiene todos los permisos de una lista
     * @param user Usuario a verificar
     * @param permissionCodes Códigos de los permisos requeridos
     * @return true si el usuario tiene todos los permisos, false en caso contrario
     */
    public static boolean hasAllPermissions(User user, String... permissionCodes) {
        if (user == null || permissionCodes == null) {
            return false;
        }
        
        for (String permissionCode : permissionCodes) {
            if (!hasPermission(user, permissionCode)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Verifica si un usuario tiene al menos uno de los permisos de una lista
     * @param user Usuario a verificar
     * @param permissionCodes Códigos de los permisos
     * @return true si el usuario tiene al menos un permiso, false en caso contrario
     */
    public static boolean hasAnyPermission(User user, String... permissionCodes) {
        if (user == null || permissionCodes == null) {
            return false;
        }
        
        for (String permissionCode : permissionCodes) {
            if (hasPermission(user, permissionCode)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Obtiene todos los permisos disponibles para un usuario según su rol
     * @param user Usuario
     * @return Conjunto de permisos disponibles
     */
    public static Set<Permission> getUserPermissions(User user) {
        if (user == null || user.getRole() == null) {
            return Set.of();
        }
        
        return Permissions.getPermissionsForRole(user.getRole().name());
    }
    
    /**
     * Valida que un usuario tenga un permiso, lanzando excepción si no lo tiene
     * @param user Usuario a verificar
     * @param permissionCode Código del permiso requerido
     * @throws SecurityException si el usuario no tiene el permiso
     */
    public static void requirePermission(User user, String permissionCode) {
        if (!hasPermission(user, permissionCode)) {
            String message = String.format(
                "Acceso denegado: Usuario %s (rol=%s) no tiene permiso %s",
                user != null ? user.getUsername() : "ANONYMOUS",
                user != null ? user.getRole() : "NONE",
                permissionCode
            );
            logger.error(message);
            throw new SecurityException(message);
        }
    }
    
    /**
     * Valida que un usuario tenga todos los permisos especificados
     * @param user Usuario a verificar
     * @param permissionCodes Códigos de los permisos requeridos
     * @throws SecurityException si el usuario no tiene alguno de los permisos
     */
    public static void requireAllPermissions(User user, String... permissionCodes) {
        for (String permissionCode : permissionCodes) {
            requirePermission(user, permissionCode);
        }
    }
    
    /**
     * Verifica si un usuario es administrador
     * @param user Usuario a verificar
     * @return true si el usuario es ADMIN, false en caso contrario
     */
    public static boolean isAdmin(User user) {
        return user != null && User.UserRole.ADMIN.equals(user.getRole());
    }
    
    /**
     * Verifica si un usuario es contador
     * @param user Usuario a verificar
     * @return true si el usuario es ACCOUNTANT, false en caso contrario
     */
    public static boolean isAccountant(User user) {
        return user != null && User.UserRole.ACCOUNTANT.equals(user.getRole());
    }
}
