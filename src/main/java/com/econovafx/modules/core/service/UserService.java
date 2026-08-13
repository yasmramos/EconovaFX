package com.econovafx.modules.core.service;

import com.econovafx.modules.core.model.AuditLog;
import com.econovafx.modules.core.model.User;
import com.econovafx.modules.core.repository.UserRepository;
import com.econovafx.modules.core.security.PasswordService;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing users with audit logging
 */
@Component
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final PasswordService passwordService;
    
    @Inject
    public UserService(UserRepository userRepository, AuditService auditService, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.passwordService = passwordService;
    }
    
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public List<User> getActiveUsers() {
        return userRepository.findActiveUsers();
    }
    
    public User createUser(User user, String plainPassword, String currentUser) {
        validateUser(user);
        
        if (userRepository.existsByUsername(user.getUsername())) {
            auditService.logFailure(currentUser, AuditLog.OperationType.CREATE, "User", null,
                                   "Create user - username exists", "Username already exists: " + user.getUsername());
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            auditService.logFailure(currentUser, AuditLog.OperationType.CREATE, "User", null,
                                   "Create user - email exists", "Email already exists: " + user.getEmail());
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        
        user.setPassword(passwordService.hashPassword(plainPassword));
        
        User saved = userRepository.save(user);
        logger.info("User created: {}", saved.getUsername());
        
        // Audit log
        auditService.logSuccess(currentUser, AuditLog.OperationType.CREATE, "User",
                               saved.getId(), "Created user: " + saved.getUsername());
        
        return saved;
    }
    
    /**
     * Create user without audit (for backward compatibility)
     */
    public User createUser(User user, String plainPassword) {
        return createUser(user, plainPassword, "system");
    }
    
    public User updateUser(User user, String currentUser) {
        validateUser(user);
        
        if (!userRepository.existsById(user.getId())) {
            auditService.logFailure(currentUser, AuditLog.OperationType.UPDATE, "User", user.getId(),
                                   "Update user - not found", "User not found with ID: " + user.getId());
            throw new IllegalArgumentException("User not found with ID: " + user.getId());
        }
        
        // Get old values for audit
        User oldUser = userRepository.findById(user.getId()).get();
        String oldValues = buildUserJson(oldUser);
        
        userRepository.update(user);
        logger.info("User updated: {}", user.getUsername());
        
        // Audit log with values
        auditService.logWithValues(currentUser, AuditLog.OperationType.UPDATE, "User",
                                  user.getId(), "Updated user: " + user.getUsername(),
                                  oldValues, buildUserJson(user));
        
        return user;
    }
    
    /**
     * Update user without audit (for backward compatibility)
     */
    public User updateUser(User user) {
        return updateUser(user, "system");
    }
    
    public void deleteUser(Long id, String currentUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        
        Long entityId = user.getId();
        String username = user.getUsername();
        
        userRepository.delete(user);
        logger.info("User deleted: {}", username);
        
        // Audit log
        auditService.logSuccess(currentUser, AuditLog.OperationType.DELETE, "User",
                               entityId, "Deleted user: " + username);
    }
    
    /**
     * Delete user without audit (for backward compatibility)
     */
    public void deleteUser(Long id) {
        deleteUser(id, "system");
    }
    
    public boolean authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            auditService.logFailure(username, AuditLog.OperationType.LOGIN, "User", null,
                                   "Login failed - user not found", "User not found: " + username);
            return false;
        }
        
        User user = userOpt.get();
        if (!user.getIsActive()) {
            auditService.logFailure(username, AuditLog.OperationType.LOGIN, "User", user.getId(),
                                   "Login failed - user inactive", "User is inactive: " + username);
            return false;
        }
        
        boolean success = passwordService.checkPassword(password, user.getPassword());
        
        if (success) {
            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.update(user);
            
            // Audit log
            auditService.logSuccess(username, AuditLog.OperationType.LOGIN, "User",
                                   user.getId(), "User logged in successfully");
        } else {
            auditService.logFailure(username, AuditLog.OperationType.LOGIN, "User", user.getId(),
                                   "Login failed - invalid password", "Invalid password for user: " + username);
        }
        
        return success;
    }
    
    private void validateUser(User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
    }
    
    /**
     * Change user password with audit logging
     */
    public void changePassword(Long userId, String oldPassword, String newPassword, String currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        // Verify old password
        if (!passwordService.checkPassword(oldPassword, user.getPassword())) {
            auditService.logFailure(currentUser, AuditLog.OperationType.PASSWORD_CHANGE, "User",
                                   userId, "Change password - invalid old password", "Old password incorrect");
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // Validate new password
        if (newPassword == null || newPassword.length() < 6) {
            auditService.logFailure(currentUser, AuditLog.OperationType.PASSWORD_CHANGE, "User",
                                   userId, "Change password - weak password", "New password too short");
            throw new IllegalArgumentException("New password must be at least 6 characters");
        }
        
        String oldValues = buildUserJson(user);
        
        // Update password
        user.setPassword(passwordService.hashPassword(newPassword));
        userRepository.update(user);
        
        // Audit log
        auditService.logWithValues(currentUser, AuditLog.OperationType.PASSWORD_CHANGE, "User",
                                  userId, "Password changed for user: " + user.getUsername(),
                                  oldValues, buildUserJson(user));
        
        logger.info("Password changed for user: {}", user.getUsername());
    }
    
    /**
     * Build JSON representation of user for audit logging
     */
    private String buildUserJson(User user) {
        if (user == null) {
            return null;
        }
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":").append(user.getId()).append(",");
        json.append("\"username\":\"").append(user.getUsername()).append("\",");
        json.append("\"email\":\"").append(user.getEmail()).append("\",");
        json.append("\"fullName\":\"").append(user.getFullName()).append("\",");
        json.append("\"role\":\"").append(user.getRole()).append("\",");
        json.append("\"isActive\":").append(user.getIsActive());
        json.append("}");
        return json.toString();
    }
    
    public long getUsersCount() {
        return userRepository.count();
    }
}
