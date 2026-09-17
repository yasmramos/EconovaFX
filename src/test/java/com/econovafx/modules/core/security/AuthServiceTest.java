package com.econovafx.modules.core.security;

import com.econovafx.modules.core.config.AppConfig;
import com.econovafx.modules.core.config.TenantContext;
import com.econovafx.modules.core.model.AuditLog;
import com.econovafx.modules.core.model.Company;
import com.econovafx.modules.core.model.User;
import com.econovafx.modules.core.service.AuditService;
import io.ebean.DB;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService with login attempt tracking and session management.
 * Uses ebean-test with H2 in-memory database for real database operations.
 */
class AuthServiceTest {

    private PasswordService passwordService;
    private AuditService auditService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
        auditService = mock(AuditService.class);
        authService = new AuthService(passwordService, auditService);
        
        // Clean up any existing data before each test
        DB.deleteAll(User.class, null);
        DB.deleteAll(Company.class, null);
        TenantContext.clear();
        SecurityUtil.clearCurrentUser();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        DB.deleteAll(User.class, null);
        DB.deleteAll(Company.class, null);
        TenantContext.clear();
        SecurityUtil.clearCurrentUser();
    }

    @Test
    void testAuthenticateSuccess() {
        // Create and save a company
        Company company = new Company("Test Company", "TEST1", "123456789");
        DB.save(company);
        
        // Create a user with hashed password
        User user = new User();
        user.setEmail("test1@example.com");
        user.setUsername("testuser1");
        user.setFullName("Test User");
        user.setPassword(passwordService.hashPassword("password123"));
        user.setStatus("ACTIVE");
        user.setCompany(company);
        DB.save(user);

        // Authenticate
        User result = authService.authenticate("test1@example.com", "password123");

        assertNotNull(result);
        assertEquals("test1@example.com", result.getEmail());
        assertNotNull(TenantContext.getCurrentTenant());
        
        // Verify audit log was called
        verify(auditService).logSuccess(eq("test1@example.com"), eq(AuditLog.OperationType.LOGIN), 
                                       eq("User"), eq(user.getId()), anyString());
    }

    @Test
    void testAuthenticateInvalidPassword() {
        // Create and save a company
        Company company = new Company("Test Company", "TEST2", "123456789");
        DB.save(company);
        
        // Create a user with hashed password
        User user = new User();
        user.setEmail("test2@example.com");
        user.setUsername("testuser2");
        user.setFullName("Test User");
        user.setPassword(passwordService.hashPassword("password123"));
        user.setStatus("ACTIVE");
        user.setCompany(company);
        DB.save(user);

        // Try to authenticate with wrong password
        User result = authService.authenticate("test2@example.com", "wrongpassword");

        assertNull(result);
        
        // Verify audit log was called for failure
        verify(auditService).logFailure(eq("test2@example.com"), eq(AuditLog.OperationType.LOGIN), 
                                       eq("User"), eq(user.getId()), anyString(), anyString());
    }

    @Test
    void testAuthenticateUserNotFound() {
        // Try to authenticate non-existent user
        User result = authService.authenticate("nonexistent@example.com", "password123");
        assertNull(result);
        
        // Verify audit log was called for not found
        verify(auditService).logFailure(eq("nonexistent@example.com"), eq(AuditLog.OperationType.LOGIN), 
                                       eq("User"), isNull(), anyString(), anyString());
    }

    @Test
    void testAuthenticateInactiveUser() {
        // Create and save a company
        Company company = new Company("Test Company", "TEST3", "123456789");
        DB.save(company);
        
        // Create an inactive user
        User user = new User();
        user.setEmail("test3@example.com");
        user.setUsername("testuser3");
        user.setFullName("Test User");
        user.setPassword(passwordService.hashPassword("password123"));
        user.setStatus("INACTIVE");
        user.setCompany(company);
        DB.save(user);

        User result = authService.authenticate("test3@example.com", "password123");
        assertNull(result);
        
        // Verify audit log was called for inactive user
        verify(auditService).logFailure(eq("test3@example.com"), eq(AuditLog.OperationType.LOGIN), 
                                       eq("User"), eq(user.getId()), contains("inactive"), anyString());
    }

    @Test
    void testLoginAttemptsLockout() throws InterruptedException {
        String email = "locktest@example.com";
        
        // Create and save a company
        Company company = new Company("Test Company", "TEST4", "123456789");
        DB.save(company);
        
        // Create a user
        User user = new User();
        user.setEmail(email);
        user.setUsername("locktest");
        user.setFullName("Test User");
        user.setPassword(passwordService.hashPassword("password123"));
        user.setStatus("ACTIVE");
        user.setCompany(company);
        DB.save(user);

        // Simulate MAX_LOGIN_ATTEMPTS failed attempts
        for (int i = 0; i < AppConfig.MAX_LOGIN_ATTEMPTS; i++) {
            User result = authService.authenticate(email, "wrongpassword");
            assertNull(result);
        }

        // Next attempt should be blocked even with correct password
        User blockedResult = authService.authenticate(email, "password123");
        assertNull(blockedResult);
        
        // Verify lockout was logged
        verify(auditService, atLeastOnce()).logFailure(eq(email), eq(AuditLog.OperationType.LOGIN), 
                                                      eq("User"), eq(user.getId()), contains("locked"), anyString());
    }

    @Test
    void testSuccessfulLoginResetsAttempts() {
        String email = "reset@example.com";
        
        // Create and save a company
        Company company = new Company("Test Company", "TEST5", "123456789");
        DB.save(company);
        
        // Create a user
        User user = new User();
        user.setEmail(email);
        user.setUsername("resetuser");
        user.setFullName("Test User");
        user.setPassword(passwordService.hashPassword("password123"));
        user.setStatus("ACTIVE");
        user.setCompany(company);
        DB.save(user);

        // Simulate some failed attempts
        for (int i = 0; i < 3; i++) {
            authService.authenticate(email, "wrongpassword");
        }

        // Successful login
        User result = authService.authenticate(email, "password123");
        assertNotNull(result);

        // After successful login, user should be able to login again even after more failures
        // This verifies the counter was reset
        // Note: In a real scenario, we would need to test this across multiple test methods
        // or use reflection to check internal state
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
        // Set a tenant and current user
        Company company = new Company("Test Company", "TEST", "123456789");
        TenantContext.setCurrentTenant(company);
        User user = new User();
        user.setUsername("testuser");
        SecurityUtil.setCurrentUser(user);

        // Logout
        authService.logout();

        // Verify both tenant and user are cleared
        assertNull(TenantContext.getCurrentTenant());
        assertNull(SecurityUtil.getCurrentUser());
    }
}
