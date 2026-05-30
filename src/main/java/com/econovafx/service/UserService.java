package com.econovafx.service;

import com.econovafx.domain.User;
import com.econovafx.repository.UserRepository;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing users
 */
@Component
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    
    @Inject
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
    
    public User createUser(User user, String plainPassword) {
        validateUser(user);
        
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        
        user.setPassword(hashPassword(plainPassword));
        
        User saved = userRepository.save(user);
        logger.info("User created: {}", saved.getUsername());
        return saved;
    }
    
    public User updateUser(User user) {
        validateUser(user);
        
        if (!userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("User not found with ID: " + user.getId());
        }
        
        userRepository.update(user);
        logger.info("User updated: {}", user.getUsername());
        return user;
    }
    
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        
        userRepository.delete(user);
        logger.info("User deleted: {}", user.getUsername());
    }
    
    public boolean authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        if (!user.getIsActive()) {
            return false;
        }
        
        return verifyPassword(password, user.getPassword());
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
     * Hash password using BCrypt with salt
     * @param password plain text password
     * @return hashed password
     */
    private String hashPassword(String password) {
        // BCrypt.gensalt() generates a salt with default log rounds (10)
        // Higher log rounds = more secure but slower
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    
    /**
     * Verify password against hashed value
     * @param plainPassword plain text password
     * @param hashedPassword stored hashed password
     * @return true if password matches
     */
    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Handle case where hashed password is not in BCrypt format
            logger.warn("Invalid hash format for user authentication");
            return false;
        }
    }
    
    public long getUsersCount() {
        return userRepository.count();
    }
}
