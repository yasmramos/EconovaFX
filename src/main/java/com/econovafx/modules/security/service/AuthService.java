package com.econovafx.modules.security.service;

import com.econovafx.modules.security.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Service class for handling user authentication
 * In production, this should integrate with a database and use password hashing
 */
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    // Demo users - in production, these should come from a database
    // Passwords are stored in plain text for demo purposes only
    // Default credentials: admin/admin123, user/user123
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String USER_USERNAME = "user";
    private static final String USER_PASSWORD = "user123";

    /**
     * Authenticate a user with username and password
     * @param username the username
     * @param password the password
     * @return Optional containing the User if authentication succeeds, empty otherwise
     */
    public Optional<User> authenticate(String username, String password) {
        logger.info("Attempting authentication for user: {}", username);

        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            logger.warn("Authentication failed: empty credentials");
            return Optional.empty();
        }

        User user = null;

        // Check against demo users
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            user = new User(1L, ADMIN_USERNAME, "admin@econovafx.com", "Administrator", "ADMIN");
            logger.info("Authentication successful for admin user");
        } else if (USER_USERNAME.equals(username) && USER_PASSWORD.equals(password)) {
            user = new User(2L, USER_USERNAME, "user@econovafx.com", "Regular User", "USER");
            logger.info("Authentication successful for regular user");
        } else {
            logger.warn("Authentication failed for user: {}", username);
            return Optional.empty();
        }

        return Optional.of(user);
    }

    /**
     * Validate if a user session is still active
     * @param user the user to validate
     * @return true if the user is valid and active, false otherwise
     */
    public boolean validateSession(User user) {
        if (user == null) {
            return false;
        }
        return user.isActive();
    }

    /**
     * Logout the current user
     * @param user the user to logout
     */
    public void logout(User user) {
        if (user != null) {
            logger.info("User {} logged out", user.getUsername());
        }
    }
}
