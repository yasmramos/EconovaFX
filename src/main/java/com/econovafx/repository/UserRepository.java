package com.econovafx.repository;

import com.econovafx.domain.User;
import io.avaje.inject.Component;
import io.ebean.Database;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entities
 */
@Component
public class UserRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);
    
    private final Database database;
    
    @Inject
    public UserRepository(Database database) {
        this.database = database;
    }
    
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(database.find(User.class, id));
    }
    
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(database.find(User.class)
                .where().eq("username", username).findOne());
    }
    
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(database.find(User.class)
                .where().eq("email", email).findOne());
    }
    
    public List<User> findAll() {
        return database.find(User.class).findList();
    }
    
    public List<User> findActiveUsers() {
        return database.find(User.class)
                .where().eq("isActive", true).findList();
    }

    /**
     * Alias para findAllActive usado en WarehouseConfigController
     */
    public List<User> findAllActive() {
        return findActiveUsers();
    }
    
    public User save(User user) {
        database.save(user);
        logger.debug("User saved: {}", user.getUsername());
        return user;
    }
    
    public void update(User user) {
        database.update(user);
        logger.debug("User updated: {}", user.getUsername());
    }
    
    public void delete(User user) {
        database.delete(user);
        logger.debug("User deleted: {}", user.getUsername());
    }
    
    public void deleteById(Long id) {
        database.delete(User.class, id);
        logger.debug("User deleted by ID: {}", id);
    }
    
    public boolean existsByUsername(String username) {
        return database.find(User.class)
                .where().eq("username", username).exists();
    }
    
    public boolean existsByEmail(String email) {
        return database.find(User.class)
                .where().eq("email", email).exists();
    }
    
    public boolean existsById(Long id) {
        return database.find(User.class, id) != null;
    }
    
    public long count() {
        return database.find(User.class).findCount();
    }
}
