package com.econovafx.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User entity
 */
class UserTest {

    @Test
    void testCreateUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setPassword("password123");

        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test User", user.getFullName());
        assertEquals("password123", user.getPassword());
    }

    @Test
    void testDefaultRole() {
        User user = new User();
        assertEquals(User.UserRole.USER, user.getRole());
    }

    @Test
    void testSetAndGetRole() {
        User user = new User();
        user.setRole(User.UserRole.ADMIN);
        assertEquals(User.UserRole.ADMIN, user.getRole());
    }

    @Test
    void testAllUserRoles() {
        assertEquals(4, User.UserRole.values().length);
        assertNotNull(User.UserRole.ADMIN);
        assertNotNull(User.UserRole.ACCOUNTANT);
        assertNotNull(User.UserRole.USER);
        assertNotNull(User.UserRole.VIEWER);
    }

    @Test
    void testSetAndGetLastLogin() {
        User user = new User();
        LocalDateTime loginTime = LocalDateTime.now();
        user.setLastLogin(loginTime);

        assertEquals(loginTime, user.getLastLogin());
    }

    @Test
    void testLastLoginIsNullByDefault() {
        User user = new User();
        assertNull(user.getLastLogin());
    }

    @Test
    void testSetAndGetUsername() {
        User user = new User();
        user.setUsername("johndoe");
        assertEquals("johndoe", user.getUsername());
    }

    @Test
    void testSetAndGetEmail() {
        User user = new User();
        user.setEmail("john@example.com");
        assertEquals("john@example.com", user.getEmail());
    }

    @Test
    void testSetAndGetFullName() {
        User user = new User();
        user.setFullName("John Doe");
        assertEquals("John Doe", user.getFullName());
    }

    @Test
    void testSetAndGetPassword() {
        User user = new User();
        user.setPassword("securePassword123");
        assertEquals("securePassword123", user.getPassword());
    }

    @Test
    void testUserWithAdminRole() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setRole(User.UserRole.ADMIN);

        assertEquals("admin", admin.getUsername());
        assertEquals(User.UserRole.ADMIN, admin.getRole());
    }

    @Test
    void testUserWithAccountantRole() {
        User accountant = new User();
        accountant.setUsername("accountant");
        accountant.setRole(User.UserRole.ACCOUNTANT);

        assertEquals(User.UserRole.ACCOUNTANT, accountant.getRole());
    }

    @Test
    void testUserWithViewerRole() {
        User viewer = new User();
        viewer.setUsername("viewer");
        viewer.setRole(User.UserRole.VIEWER);

        assertEquals(User.UserRole.VIEWER, viewer.getRole());
    }

    @Test
    void testUserIsActive() {
        User user = new User();
        // BaseEntity has isActive property, default should be true
        assertTrue(user.getIsActive());
    }

    @Test
    void testUserCanChangeActiveStatus() {
        User user = new User();
        user.setIsActive(false);
        assertFalse(user.getIsActive());
    }
}
