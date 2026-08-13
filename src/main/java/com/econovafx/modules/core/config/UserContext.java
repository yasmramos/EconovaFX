package com.econovafx.modules.core.config;

import com.econovafx.modules.core.model.User;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the current user session and provides access to user-specific information.
 * This class is used to track the currently logged-in user for audit purposes and
 * authorization checks.
 */
@Singleton
public class UserContext {
    
    private static final Logger logger = LoggerFactory.getLogger(UserContext.class);
    
    private User currentUser;
    private Long currentTenantId;
    
    public UserContext() {
        // Default constructor for dependency injection
    }
    
    /**
     * Sets the currently logged-in user.
     * @param user the authenticated user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null && user.getCompany() != null) {
            this.currentTenantId = user.getCompany().getId();
        }
        logger.info("User context set for user: {} (ID: {})", 
            user != null ? user.getUsername() : "null",
            user != null ? user.getId() : "null");
    }
    
    /**
     * Gets the currently logged-in user.
     * @return the current user, or null if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Gets the ID of the currently logged-in user.
     * @return the current user ID, or null if no user is logged in
     */
    public Long getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }
    
    /**
     * Gets the tenant ID associated with the current user's company.
     * @return the tenant ID, or null if no user is logged in
     */
    public Long getCurrentTenantId() {
        return currentTenantId;
    }
    
    /**
     * Checks if a user is currently logged in.
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Clears the current user context (e.g., on logout).
     */
    public void clear() {
        logger.info("Clearing user context for user: {}", 
            currentUser != null ? currentUser.getUsername() : "null");
        this.currentUser = null;
        this.currentTenantId = null;
    }
}
