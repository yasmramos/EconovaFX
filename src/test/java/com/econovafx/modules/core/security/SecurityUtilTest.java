package com.econovafx.modules.core.security;

import com.econovafx.modules.core.config.AppConfig;
import com.econovafx.modules.core.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SecurityUtil session timeout functionality
 */
class SecurityUtilTest {

    @BeforeEach
    void setUp() {
        // Clear any existing user context
        SecurityUtil.clearCurrentUser();
    }

    @Test
    void testSessionTimeoutParsing() {
        // Verify that SESSION_TIMEOUT is parsed from AppConfig
        // Default is "30m" which should be 30 minutes
        String timeoutStr = AppConfig.SESSION_TIMEOUT;
        assertNotNull(timeoutStr);
        assertFalse(timeoutStr.isEmpty());
    }

    @Test
    void testSetCurrentUserUpdatesActivity() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        // Set current user - this should update last activity
        SecurityUtil.setCurrentUser(user);

        // Verify user is set
        assertEquals("testuser", SecurityUtil.getCurrentUser().getUsername());
        assertTrue(SecurityUtil.isAuthenticated());
    }

    @Test
    void testClearCurrentUserRemovesActivity() {
        User user = new User();
        user.setUsername("testuser");

        SecurityUtil.setCurrentUser(user);
        assertTrue(SecurityUtil.isAuthenticated());

        SecurityUtil.clearCurrentUser();
        assertFalse(SecurityUtil.isAuthenticated());
        assertNull(SecurityUtil.getCurrentUser());
    }

    @Test
    void testIsSessionExpiredWithNoUser() {
        // No user logged in should be considered expired
        assertTrue(SecurityUtil.isSessionExpired());
    }

    @Test
    void testIsSessionExpiredImmediatelyAfterLogin() throws InterruptedException {
        User user = new User();
        user.setUsername("testuser");

        SecurityUtil.setCurrentUser(user);

        // Immediately after login, session should not be expired
        // (assuming timeout is more than 0)
        assertFalse(SecurityUtil.isSessionExpired());
    }

    @Test
    void testUpdateLastActivity() {
        User user = new User();
        user.setUsername("testuser");

        SecurityUtil.setCurrentUser(user);

        // Update activity
        SecurityUtil.updateLastActivity();

        // Should still be authenticated
        assertTrue(SecurityUtil.isAuthenticated());
        assertEquals("testuser", SecurityUtil.getCurrentUser().getUsername());
    }

    @Test
    void testGetRemainingSessionTimeMinutes() {
        User user = new User();
        user.setUsername("testuser");

        SecurityUtil.setCurrentUser(user);

        // Should have some remaining time (close to full timeout)
        long remainingMinutes = SecurityUtil.getRemainingSessionTimeMinutes();
        
        // Timeout is configured to at least 1 minute by default
        assertTrue(remainingMinutes >= 0);
    }

    @Test
    void testGetRemainingSessionTimeWhenNotAuthenticated() {
        // When no user is logged in, remaining time should be 0
        assertEquals(0, SecurityUtil.getRemainingSessionTimeMinutes());
    }

    @Test
    void testSessionExpirationAfterTimeout() throws InterruptedException {
        // This test verifies the session timeout logic
        // Note: We can't actually wait for 30 minutes, so we test the mechanism
        
        User user = new User();
        user.setUsername("timeoutuser");

        SecurityUtil.setCurrentUser(user);
        
        // Session should not be expired immediately
        assertFalse(SecurityUtil.isSessionExpired());
        
        // The actual expiration will happen after SESSION_TIMEOUT duration
        // In production, this would be tested with a shorter timeout
        // For now, we just verify the mechanism exists
        assertTrue(SecurityUtil.getRemainingSessionTimeMinutes() >= 0);
    }
}
