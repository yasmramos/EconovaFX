package com.econovafx.modules.core.security;

import com.econovafx.modules.core.config.AppConfig;
import com.econovafx.modules.core.config.TenantContext;
import com.econovafx.modules.core.model.AuditLog;
import com.econovafx.modules.core.model.User;
import com.econovafx.modules.core.service.AuditService;
import io.avaje.inject.Component;
import io.ebean.DB;
import io.ebean.Query;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Authentication and authorization service with login attempt tracking and session management.
 */
@Component
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final PasswordService passwordService;
    private final AuditService auditService;
    
    // In-memory storage for failed login attempts (username -> FailedLoginInfo)
    private final Map<String, FailedLoginInfo> failedLoginAttempts = new ConcurrentHashMap<>();

    @Inject
    public AuthService(PasswordService passwordService, AuditService auditService) {
        this.passwordService = passwordService;
        this.auditService = auditService;
    }

    /**
     * Inner class to track failed login information
     */
    private static class FailedLoginInfo {
        int count;
        Instant lockedUntil;
        
        FailedLoginInfo(int count, Instant lockedUntil) {
            this.count = count;
            this.lockedUntil = lockedUntil;
        }
    }

    /**
     * Authenticates a user by username or email and password.
     * @param login User username or email
     * @param password User password
     * @return The authenticated user or null if authentication fails
     */
    public User authenticate(String login, String password) {
        // Check if user is locked due to too many failed attempts
        FailedLoginInfo lockInfo = failedLoginAttempts.get(login);
        if (lockInfo != null && lockInfo.lockedUntil != null) {
            if (Instant.now().isBefore(lockInfo.lockedUntil)) {
                long remainingSeconds = Duration.between(Instant.now(), lockInfo.lockedUntil).getSeconds();
                logger.warn("Login attempt blocked for user {}: account locked for {} more seconds", 
                           login, remainingSeconds);
                auditService.logFailure(login, 
                    AuditLog.OperationType.LOGIN, 
                    "User", null, 
                    "Login blocked - too many failed attempts", 
                    "Account locked. Try again in " + remainingSeconds + " seconds");
                return null;
            } else {
                // Lock expired, reset
                failedLoginAttempts.remove(login);
                logger.info("Lock expired for user {}", login);
            }
        }

        // Search by username OR email
        Query<User> query = DB.find(User.class)
            .where()
            .or()
            .eq("username", login)
            .eq("email", login)
            .endOr()
            .query();
        Optional<User> userOpt = query.findOneOrEmpty();
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Check if user account is active
            if (!"ACTIVE".equals(user.getStatus())) {
                logger.warn("Login attempt for inactive user: {}", login);
                auditService.logFailure(login, 
                    AuditLog.OperationType.LOGIN, 
                    "User", user.getId(), 
                    "Login failed - user inactive", 
                    "User status: " + user.getStatus());
                return null;
            }
            
            if (passwordService.checkPassword(password, user.getPassword())) {
                // Successful login - reset failed attempts
                failedLoginAttempts.remove(login);
                
                // Set the current tenant (user's company)
                if (user.getCompany() != null) {
                    TenantContext.setCurrentTenant(user.getCompany());
                }
                
                // Update last login timestamp
                user.setLastLogin(java.time.LocalDateTime.now());
                DB.save(user);
                
                // Audit log success
                auditService.logSuccess(login, 
                    AuditLog.OperationType.LOGIN, 
                    "User", user.getId(), 
                    "User logged in successfully");
                
                logger.info("Login successful for user: {}", user.getUsername());
                return user;
            } else {
                // Failed login - increment counter
                recordFailedAttempt(login, user.getId());
                return null;
            }
        } else {
            // User not found - still record as failed attempt to prevent enumeration
            logger.warn("Login attempt for non-existent user: {}", login);
            auditService.logFailure(login, 
                AuditLog.OperationType.LOGIN, 
                "User", null, 
                "Login failed - user not found", 
                "No user found with username or email: " + login);
            return null;
        }
    }

    /**
     * Records a failed login attempt and locks the account if max attempts reached
     */
    private void recordFailedAttempt(String login, Long userId) {
        FailedLoginInfo existing = failedLoginAttempts.get(login);
        int newCount = (existing != null) ? existing.count + 1 : 1;
        
        logger.warn("Failed login attempt {} for user {}", newCount, login);
        
        if (newCount >= AppConfig.MAX_LOGIN_ATTEMPTS) {
            // Lock the account for 15 minutes
            Instant lockedUntil = Instant.now().plus(Duration.ofMinutes(15));
            failedLoginAttempts.put(login, new FailedLoginInfo(newCount, lockedUntil));
            
            logger.warn("Account locked for user {} after {} failed attempts. Locked until {}", 
                       login, newCount, lockedUntil);
            
            auditService.logFailure(login, 
                AuditLog.OperationType.LOGIN, 
                "User", userId, 
                "Account locked - too many failed attempts", 
                "Account locked for 15 minutes after " + newCount + " failed attempts");
        } else {
            failedLoginAttempts.put(login, new FailedLoginInfo(newCount, null));
            
            auditService.logFailure(login, 
                AuditLog.OperationType.LOGIN, 
                "User", userId, 
                "Login failed - invalid credentials", 
                "Failed attempt " + newCount + " of " + AppConfig.MAX_LOGIN_ATTEMPTS);
        }
    }

    /**
     * Verifies if the user has a specific role.
     */
    public boolean hasRole(User user, User.UserRole role) {
        return user != null && user.getRole() == role;
    }

    /**
     * Logs out the current user.
     */
    public void logout() {
        TenantContext.clear();
        SecurityUtil.clearCurrentUser();
        logger.info("User logged out successfully");
    }
}
