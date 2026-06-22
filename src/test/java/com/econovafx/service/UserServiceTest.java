package com.econovafx.service;

import com.econovafx.model.AuditLog;
import com.econovafx.model.User;
import com.econovafx.repository.AuditLogRepository;
import com.econovafx.repository.UserRepository;
import com.econovafx.security.PasswordService;
import org.mindrot.jbcrypt.BCrypt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserService with manual stub mocks
 */
class UserServiceTest {

    private StubUserRepository userRepository;
    private StubAuditLogRepository auditLogRepository;
    private AuditService auditService;
    private PasswordService passwordService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = new StubUserRepository();
        auditLogRepository = new StubAuditLogRepository();
        auditService = new AuditService(auditLogRepository);
        passwordService = new PasswordService();
        userService = new UserService(userRepository, auditService, passwordService);
    }

    @Test
    void testCreateUserSuccess() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");

        User result = userService.createUser(user, "password123");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertTrue(BCrypt.checkpw("password123", result.getPassword()));
        assertTrue(userRepository.saveCalled);
    }

    @Test
    void testCreateUserFailsWhenUsernameExists() {
        User existing = new User();
        existing.setUsername("testuser");
        existing.setEmail("existing@example.com");
        existing.setFullName("Existing User");
        userRepository.save(existing);
        
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser(user, "password123")
        );

        assertEquals("Username already exists: testuser", exception.getMessage());
    }

    @Test
    void testCreateUserFailsWhenEmailExists() {
        User existing = new User();
        existing.setUsername("existing");
        existing.setEmail("test@example.com");
        existing.setFullName("Existing User");
        userRepository.save(existing);
        
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser(user, "password123")
        );

        assertEquals("Email already exists: test@example.com", exception.getMessage());
    }

    @Test
    void testCreateUserFailsWithNullUsername() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFullName("Test User");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser(user, "password123")
        );

        assertEquals("Username is required", exception.getMessage());
    }

    @Test
    void testCreateUserFailsWithEmptyUsername() {
        User user = new User();
        user.setUsername("");
        user.setEmail("test@example.com");
        user.setFullName("Test User");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser(user, "password123")
        );

        assertEquals("Username is required", exception.getMessage());
    }

    @Test
    void testCreateUserFailsWithNullEmail() {
        User user = new User();
        user.setUsername("testuser");
        user.setFullName("Test User");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser(user, "password123")
        );

        assertEquals("Email is required", exception.getMessage());
    }

    @Test
    void testCreateUserFailsWithNullFullName() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser(user, "password123")
        );

        assertEquals("Full name is required", exception.getMessage());
    }

    @Test
    void testCreateUserWithAdminRole() {
        User user = new User();
        user.setUsername("admin");
        user.setEmail("admin@example.com");
        user.setFullName("Admin User");
        user.setRole(User.UserRole.ADMIN);

        User result = userService.createUser(user, "adminpass");

        assertNotNull(result);
        assertEquals(User.UserRole.ADMIN, result.getRole());
        assertTrue(BCrypt.checkpw("adminpass", result.getPassword()));
    }

    @Test
    void testUpdateUserSuccess() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        User saved = userService.createUser(user, "password123");
        
        saved.setFullName("Updated Name");
        User result = userService.updateUser(saved);

        assertNotNull(result);
        assertTrue(userRepository.updateCalled);
    }

    @Test
    void testUpdateUserFailsWhenNotFound() {
        User user = new User();
        user.setId(999L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.updateUser(user)
        );

        assertEquals("User not found with ID: 999", exception.getMessage());
    }

    @Test
    void testUpdateUserFailsValidation() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFullName("Test User");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.updateUser(user)
        );

        assertEquals("Username is required", exception.getMessage());
    }

    @Test
    void testDeleteUserSuccess() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        User saved = userService.createUser(user, "password123");
        
        userService.deleteUser(saved.getId());

        assertTrue(userRepository.deleteCalled);
        assertEquals(0, userRepository.count());
    }

    @Test
    void testDeleteUserFailsWhenNotFound() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.deleteUser(999L)
        );

        assertEquals("User not found with ID: 999", exception.getMessage());
    }

    @Test
    void testAuthenticateSuccess() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        userService.createUser(user, "password123");

        boolean result = userService.authenticate("testuser", "password123");

        assertTrue(result);
    }

    @Test
    void testAuthenticateFailsWithWrongPassword() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        userService.createUser(user, "correctpassword");

        boolean result = userService.authenticate("testuser", "wrongpassword");

        assertFalse(result);
    }

    @Test
    void testAuthenticateFailsWhenUserNotFound() {
        boolean result = userService.authenticate("nonexistent", "password123");

        assertFalse(result);
    }

    @Test
    void testAuthenticateFailsWhenUserInactive() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        User saved = userService.createUser(user, "password123");
        saved.setIsActive(false);

        boolean result = userService.authenticate("testuser", "password123");

        assertFalse(result);
    }

    @Test
    void testGetUserById() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        User saved = userService.createUser(user, "password123");

        Optional<User> result = userService.getUserById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void testGetUserByUsername() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        userService.createUser(user, "password123");

        Optional<User> result = userService.getUserByUsername("testuser");

        assertTrue(result.isPresent());
    }

    @Test
    void testGetUserByEmail() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        userService.createUser(user, "password123");

        Optional<User> result = userService.getUserByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void testGetAllUsers() {
        userService.createUser(createUser("user1", "User One"), "pass1");
        userService.createUser(createUser("user2", "User Two"), "pass2");
        userService.createUser(createUser("user3", "User Three"), "pass3");

        List<User> result = userService.getAllUsers();

        assertEquals(3, result.size());
    }

    @Test
    void testGetActiveUsers() {
        User user1 = userService.createUser(createUser("user1", "User One"), "pass1");
        User user2 = userService.createUser(createUser("user2", "User Two"), "pass2");
        user2.setIsActive(false);
        userRepository.update(user2); // Update the user in the repository

        List<User> result = userService.getActiveUsers();

        assertEquals(1, result.size());
    }

    @Test
    void testGetUsersCount() {
        userService.createUser(createUser("user1", "User One"), "pass1");
        userService.createUser(createUser("user2", "User Two"), "pass2");

        long count = userService.getUsersCount();

        assertEquals(2, count);
    }

    @Test
    void testHashPasswordCreatesExpectedFormat() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");

        userService.createUser(user, "mypassword");

        assertTrue(BCrypt.checkpw("mypassword", userRepository.lastSavedUser.getPassword()));
    }

    @Test
    void testVerifyPasswordWorksWithHashedPasswords() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        userService.createUser(user, "secret");

        assertTrue(userService.authenticate("testuser", "secret"));
        assertFalse(userService.authenticate("testuser", "wrong"));
    }

    private User createUser(String username, String fullName) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setFullName(fullName);
        return user;
    }

    // Stub implementation of AuditLogRepository
    private static class StubAuditLogRepository extends AuditLogRepository {
        boolean saveCalled = false;
        AuditLog lastSavedLog;
        private List<AuditLog> logs = new ArrayList<>();

        public StubAuditLogRepository() {
            super(null);
        }

        @Override
        public Optional<AuditLog> findById(Long id) {
            return logs.stream()
                .filter(l -> l.getId() != null && l.getId().equals(id))
                .findFirst();
        }

        @Override
        public List<AuditLog> findAll() {
            return new ArrayList<>(logs);
        }

        @Override
        public List<AuditLog> findByUsername(String username) {
            return logs.stream()
                .filter(l -> username.equals(l.getUsername()))
                .toList();
        }

        @Override
        public List<AuditLog> findByOperationType(AuditLog.OperationType operationType) {
            return logs.stream()
                .filter(l -> operationType.equals(l.getOperationType()))
                .toList();
        }

        @Override
        public List<AuditLog> findByEntityType(String entityType) {
            return logs.stream()
                .filter(l -> entityType.equals(l.getEntityType()))
                .toList();
        }

        @Override
        public List<AuditLog> findByEntityId(Long entityId) {
            return logs.stream()
                .filter(l -> entityId.equals(l.getEntityId()))
                .toList();
        }

        @Override
        public List<AuditLog> findByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
            return logs.stream()
                .filter(l -> !l.getCreatedAt().isBefore(startDate) && !l.getCreatedAt().isAfter(endDate))
                .toList();
        }

        @Override
        public List<AuditLog> findFailedOperations() {
            return logs.stream()
                .filter(l -> Boolean.FALSE.equals(l.getSuccess()))
                .toList();
        }

        @Override
        public AuditLog save(AuditLog log) {
            if (log.getId() == null) {
                log.setId((long)(logs.size() + 1));
            }
            logs.add(log);
            saveCalled = true;
            lastSavedLog = log;
            return log;
        }

        @Override
        public long count() {
            return logs.size();
        }

        @Override
        public long countByUser(String username) {
            return logs.stream()
                .filter(l -> username.equals(l.getUsername()))
                .count();
        }
    }

    // Stub implementation
    private static class StubUserRepository extends UserRepository {
        boolean saveCalled = false;
        boolean updateCalled = false;
        boolean deleteCalled = false;
        User lastSavedUser;
        private List<User> users = new ArrayList<>();
        private Long nextId = 1L;

        public StubUserRepository() {
            super(null);
        }

        @Override
        public Optional<User> findById(Long id) {
            return users.stream()
                .filter(u -> u.getId() != null && u.getId().equals(id))
                .findFirst();
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return users.stream()
                .filter(u -> username.equals(u.getUsername()))
                .findFirst();
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return users.stream()
                .filter(u -> email.equals(u.getEmail()))
                .findFirst();
        }

        @Override
        public List<User> findAll() {
            return new ArrayList<>(users);
        }

        @Override
        public List<User> findActiveUsers() {
            return users.stream()
                .filter(User::getIsActive)
                .toList();
        }

        @Override
        public User save(User user) {
            if (user.getId() == null) {
                user.setId(nextId++);
            }
            users.add(user);
            saveCalled = true;
            lastSavedUser = user;
            return user;
        }

        @Override
        public void update(User user) {
            updateCalled = true;
        }

        @Override
        public void delete(User user) {
            users.removeIf(u -> u.getId().equals(user.getId()));
            deleteCalled = true;
        }

        @Override
        public boolean existsByUsername(String username) {
            return users.stream().anyMatch(u -> username.equals(u.getUsername()));
        }

        @Override
        public boolean existsByEmail(String email) {
            return users.stream().anyMatch(u -> email.equals(u.getEmail()));
        }

        @Override
        public boolean existsById(Long id) {
            return users.stream().anyMatch(u -> u.getId() != null && u.getId().equals(id));
        }

        @Override
        public long count() {
            return users.size();
        }
    }
}
