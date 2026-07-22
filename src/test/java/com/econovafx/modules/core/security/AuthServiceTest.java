package com.econovafx.modules.core.security;

import com.econovafx.modules.core.config.TenantContext;
import com.econovafx.modules.core.model.Company;
import com.econovafx.modules.core.model.User;
import io.ebean.DB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 */
class AuthServiceTest {

    private PasswordService passwordService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
        authService = new AuthService();
    }

    @Test
    void testAuthenticateSuccess() {
        // Create a user with hashed password
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword(passwordService.hashPassword("password123"));
        Company company = new Company("Test Company", "TEST", "123456789");
        user.setCompany(company);

        try (MockedStatic<DB> mockedDB = mockStatic(DB.class)) {
            io.ebean.Query<User> mockQuery = mock(io.ebean.Query.class);
            io.ebean.ExpressionList<User> mockExpressionList = mock(io.ebean.ExpressionList.class);

            when(DB.find(User.class)).thenReturn(mockQuery);
            when(mockQuery.where()).thenReturn(mockExpressionList);
            when(mockExpressionList.eq("email", "test@example.com")).thenReturn(mockExpressionList);
            when(mockExpressionList.query()).thenReturn(mockQuery);
            when(mockQuery.findOneOrEmpty()).thenReturn(java.util.Optional.of(user));

            // Authenticate
            User result = authService.authenticate("test@example.com", "password123");

            assertNotNull(result);
            assertEquals("test@example.com", result.getEmail());
            assertNotNull(TenantContext.getCurrentTenant());
        }
    }

    @Test
    void testAuthenticateInvalidPassword() {
        // Create a user with hashed password
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword(passwordService.hashPassword("password123"));

        try (MockedStatic<DB> mockedDB = mockStatic(DB.class)) {
            io.ebean.Query<User> mockQuery = mock(io.ebean.Query.class);
            io.ebean.ExpressionList<User> mockExpressionList = mock(io.ebean.ExpressionList.class);

            when(DB.find(User.class)).thenReturn(mockQuery);
            when(mockQuery.where()).thenReturn(mockExpressionList);
            when(mockExpressionList.eq("email", "test@example.com")).thenReturn(mockExpressionList);
            when(mockExpressionList.query()).thenReturn(mockQuery);
            when(mockQuery.findOneOrEmpty()).thenReturn(java.util.Optional.of(user));

            // Try to authenticate with wrong password
            User result = authService.authenticate("test@example.com", "wrongpassword");

            assertNull(result);
        }
    }

    @Test
    void testAuthenticateUserNotFound() {
        try (MockedStatic<DB> mockedDB = mockStatic(DB.class)) {
            io.ebean.Query<User> mockQuery = mock(io.ebean.Query.class);
            io.ebean.ExpressionList<User> mockExpressionList = mock(io.ebean.ExpressionList.class);

            when(DB.find(User.class)).thenReturn(mockQuery);
            when(mockQuery.where()).thenReturn(mockExpressionList);
            when(mockExpressionList.eq("email", "nonexistent@example.com")).thenReturn(mockExpressionList);
            when(mockExpressionList.query()).thenReturn(mockQuery);
            when(mockQuery.findOneOrEmpty()).thenReturn(java.util.Optional.empty());

            User result = authService.authenticate("nonexistent@example.com", "password123");
            assertNull(result);
        }
    }

    @Test
    void testHasRoleTrue() {
        User user = new User();
        user.setRole(User.UserRole.ADMIN);

        assertTrue(authService.hasRole(user, User.UserRole.ADMIN));
    }

    @Test
    void testHasRoleFalse() {
        User user = new User();
        user.setRole(User.UserRole.USER);

        assertFalse(authService.hasRole(user, User.UserRole.ADMIN));
    }

    @Test
    void testHasRoleNullUser() {
        assertFalse(authService.hasRole(null, User.UserRole.ADMIN));
    }

    @Test
    void testLogout() {
        // Set a tenant
        Company company = new Company("Test Company", "TEST", "123456789");
        TenantContext.setCurrentTenant(company);

        // Logout
        authService.logout();

        // Verify tenant is cleared
        assertNull(TenantContext.getCurrentTenant());
    }
}
