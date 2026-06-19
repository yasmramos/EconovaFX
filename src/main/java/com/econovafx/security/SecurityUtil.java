package com.econovafx.security;

import com.econovafx.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility class for verifying user permissions in the system.
 * Provides methods to validate access to specific operations.
 * Supports static context for use in interceptors (similar to Apache Shiro).
 */
public class SecurityUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);
    
    /**
     * ThreadLocal holder for the current authenticated user.
     * This allows static access to the current user in interceptors.
     */
    private static final ThreadLocal<User> currentUserHolder = new ThreadLocal<>();
    
    /**
     * Sets the current authenticated user in the thread context.
     * Call this after successful login.
     * @param user The authenticated user
     */
    public static void setCurrentUser(User user) {
        currentUserHolder.set(user);
        logger.debug("Current user set: {}", user != null ? user.getUsername() : "null");
    }
    
    /**
     * Gets the current authenticated user from the thread context.
     * @return The current user, or null if not authenticated
     */
    public static User getCurrentUser() {
        return currentUserHolder.get();
    }
    
    /**
     * Clears the current user from the thread context.
     * Call this on logout or at the end of a request.
     */
    public static void clearCurrentUser() {
        currentUserHolder.remove();
        logger.debug("Current user cleared");
    }
    
    /**
     * Checks if a user is currently authenticated.
     * @return true if a user is set in the current thread context
     */
    public static boolean isAuthenticated() {
        return currentUserHolder.get() != null;
    }
    
    /**
     * Verifies if a user has a specific permission
     * @param user User to verify
     * @param permissionCode Permission code
     * @return true if the user has the permission, false otherwise
     */
    public static boolean hasPermission(User user, String permissionCode) {
        if (user == null) {
            logger.warn("Attempt to verify permission with null user: {}", permissionCode);
            return false;
        }
        
        if (user.getRole() == null) {
            logger.warn("User without defined role: {}", user.getUsername());
            return false;
        }
        
        boolean hasPermission = Permissions.hasPermission(user.getRole().name(), permissionCode);
        
        if (!hasPermission) {
            logger.warn("User {} does not have permission {}: role={}", 
                       user.getUsername(), permissionCode, user.getRole());
        }
        
        return hasPermission;
    }
    
    /**
     * Verifies if the current authenticated user has a specific permission.
     * Uses the ThreadLocal user context.
     * @param permissionCode Permission code
     * @return true if the current user has the permission, false otherwise
     */
    public static boolean hasPermission(String permissionCode) {
        User user = getCurrentUser();
        return hasPermission(user, permissionCode);
    }
    
    /**
     * Verifies if a user has all permissions from a list
     * @param user User to verify
     * @param permissionCodes Codes of required permissions
     * @return true if the user has all permissions, false otherwise
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
     * Verifies if the current authenticated user has all permissions from a list.
     * @param permissionCodes Codes of required permissions
     * @return true if the current user has all permissions, false otherwise
     */
    public static boolean hasAllPermissions(String... permissionCodes) {
        User user = getCurrentUser();
        return hasAllPermissions(user, permissionCodes);
    }
    
    /**
     * Verifies if a user has at least one of the permissions from a list
     * @param user User to verify
     * @param permissionCodes Codes of permissions
     * @return true if the user has at least one permission, false otherwise
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
     * Verifies if the current authenticated user has at least one of the permissions from a list.
     * @param permissionCodes Codes of permissions
     * @return true if the current user has at least one permission, false otherwise
     */
    public static boolean hasAnyPermission(String... permissionCodes) {
        User user = getCurrentUser();
        return hasAnyPermission(user, permissionCodes);
    }
    
    /**
     * Gets all available permissions for a user based on their role
     * @param user User
     * @return Set of available permissions
     */
    public static Set<Permission> getUserPermissions(User user) {
        if (user == null || user.getRole() == null) {
            return Set.of();
        }
        
        return Permissions.getPermissionsForRole(user.getRole().name());
    }
    
    /**
     * Gets all available permissions for the current authenticated user.
     * @return Set of available permissions
     */
    public static Set<Permission> getCurrentUserPermissions() {
        User user = getCurrentUser();
        return getUserPermissions(user);
    }
    
    /**
     * Validates that a user has a permission, throwing an exception if not
     * @param user User to verify
     * @param permissionCode Required permission code
     * @throws AuthorizationException if the user does not have the permission
     */
    public static void requirePermission(User user, String permissionCode) {
        if (!hasPermission(user, permissionCode)) {
            String message = String.format(
                "Access denied: User %s (role=%s) does not have permission %s",
                user != null ? user.getUsername() : "ANONYMOUS",
                user != null ? user.getRole() : "NONE",
                permissionCode
            );
            logger.error(message);
            throw new AuthorizationException(message);
        }
    }
    
    /**
     * Validates that the current authenticated user has a permission, throwing an exception if not.
     * @param permissionCode Required permission code
     * @throws AuthorizationException if the current user does not have the permission
     */
    public static void requirePermission(String permissionCode) {
        User user = getCurrentUser();
        if (user == null) {
            throw new AuthorizationException("Access denied: No authenticated user");
        }
        requirePermission(user, permissionCode);
    }
    
    /**
     * Validates that a user has all specified permissions
     * @param user User to verify
     * @param permissionCodes Codes of required permissions
     * @throws AuthorizationException if the user does not have any of the permissions
     */
    public static void requireAllPermissions(User user, String... permissionCodes) {
        for (String permissionCode : permissionCodes) {
            requirePermission(user, permissionCode);
        }
    }
    
    /**
     * Validates that the current authenticated user has all specified permissions.
     * @param permissionCodes Codes of required permissions
     * @throws AuthorizationException if the current user does not have any of the permissions
     */
    public static void requireAllPermissions(String... permissionCodes) {
        User user = getCurrentUser();
        if (user == null) {
            throw new AuthorizationException("Access denied: No authenticated user");
        }
        requireAllPermissions(user, permissionCodes);
    }
    
    /**
     * Validates that a user has at least one of the specified permissions
     * @param user User to verify
     * @param permissionCodes Codes of permissions
     * @throws AuthorizationException if the user does not have any of the permissions
     */
    public static void requireAnyPermission(User user, String... permissionCodes) {
        if (!hasAnyPermission(user, permissionCodes)) {
            String message = String.format(
                "Access denied: User %s (role=%s) does not have any of permissions %s",
                user != null ? user.getUsername() : "ANONYMOUS",
                user != null ? user.getRole() : "NONE",
                String.join(", ", permissionCodes)
            );
            logger.error(message);
            throw new AuthorizationException(message);
        }
    }
    
    /**
     * Validates that the current authenticated user has at least one of the specified permissions.
     * @param permissionCodes Codes of permissions
     * @throws AuthorizationException if the current user does not have any of the permissions
     */
    public static void requireAnyPermission(String... permissionCodes) {
        User user = getCurrentUser();
        if (user == null) {
            throw new AuthorizationException("Access denied: No authenticated user");
        }
        requireAnyPermission(user, permissionCodes);
    }
    
    /**
     * Verifies if a user is an administrator
     * @param user User to verify
     * @return true if the user is ADMIN, false otherwise
     */
    public static boolean isAdmin(User user) {
        return user != null && User.UserRole.ADMIN.equals(user.getRole());
    }
    
    /**
     * Verifies if the current authenticated user is an administrator.
     * @return true if the current user is ADMIN, false otherwise
     */
    public static boolean isAdmin() {
        User user = getCurrentUser();
        return isAdmin(user);
    }
    
    /**
     * Verifies if a user is an accountant
     * @param user User to verify
     * @return true if the user is ACCOUNTANT, false otherwise
     */
    public static boolean isAccountant(User user) {
        return user != null && User.UserRole.ACCOUNTANT.equals(user.getRole());
    }
    
    /**
     * Verifies if the current authenticated user is an accountant.
     * @return true if the current user is ACCOUNTANT, false otherwise
     */
    public static boolean isAccountant() {
        User user = getCurrentUser();
        return isAccountant(user);
    }
    
    /**
     * Verifies if a user has a specific role
     * @param user User to verify
     * @param roleName Role name
     * @return true if the user has the role, false otherwise
     */
    public static boolean hasRole(User user, String roleName) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        return user.getRole().name().equals(roleName);
    }
    
    /**
     * Verifies if the current authenticated user has a specific role.
     * @param roleName Role name
     * @return true if the current user has the role, false otherwise
     */
    public static boolean hasRole(String roleName) {
        User user = getCurrentUser();
        return hasRole(user, roleName);
    }
    
    /**
     * Verifies if a user has all specified roles
     * @param user User to verify
     * @param roleNames Role names
     * @return true if the user has all roles, false otherwise
     */
    public static boolean hasAllRoles(User user, String... roleNames) {
        if (user == null || roleNames == null) {
            return false;
        }
        
        for (String roleName : roleNames) {
            if (!hasRole(user, roleName)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Verifies if the current authenticated user has all specified roles.
     * @param roleNames Role names
     * @return true if the current user has all roles, false otherwise
     */
    public static boolean hasAllRoles(String... roleNames) {
        User user = getCurrentUser();
        return hasAllRoles(user, roleNames);
    }
    
    /**
     * Verifies if a user has at least one of the specified roles
     * @param user User to verify
     * @param roleNames Role names
     * @return true if the user has at least one role, false otherwise
     */
    public static boolean hasAnyRole(User user, String... roleNames) {
        if (user == null || roleNames == null) {
            return false;
        }
        
        for (String roleName : roleNames) {
            if (hasRole(user, roleName)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Verifies if the current authenticated user has at least one of the specified roles.
     * @param roleNames Role names
     * @return true if the current user has at least one role, false otherwise
     */
    public static boolean hasAnyRole(String... roleNames) {
        User user = getCurrentUser();
        return hasAnyRole(user, roleNames);
    }
    
    /**
     * Validates that a user has a specific role, throwing an exception if not
     * @param user User to verify
     * @param roleName Required role name
     * @throws AuthorizationException if the user does not have the role
     */
    public static void requireRole(User user, String roleName) {
        if (!hasRole(user, roleName)) {
            String message = String.format(
                "Access denied: User %s (role=%s) does not have required role %s",
                user != null ? user.getUsername() : "ANONYMOUS",
                user != null ? user.getRole() : "NONE",
                roleName
            );
            logger.error(message);
            throw new AuthorizationException(message);
        }
    }
    
    /**
     * Validates that the current authenticated user has a specific role, throwing an exception if not.
     * @param roleName Required role name
     * @throws AuthorizationException if the current user does not have the role
     */
    public static void requireRole(String roleName) {
        User user = getCurrentUser();
        if (user == null) {
            throw new AuthorizationException("Access denied: No authenticated user");
        }
        requireRole(user, roleName);
    }
}
