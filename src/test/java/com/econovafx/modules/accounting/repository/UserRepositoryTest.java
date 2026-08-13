package com.econovafx.modules.accounting.repository;

import com.econovafx.modules.core.model.User;
import com.econovafx.modules.core.repository.UserRepository;
import io.ebean.Database;
import io.ebean.ExpressionList;
import io.ebean.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserRepository
 */
@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private Database database;

    @Mock
    private Query<User> query;

    @Mock
    private ExpressionList<User> expressionList;

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository(database);
    }

    @Test
    void testFindById_WhenUserExists_ReturnsOptionalContainingUser() {
        // Arrange
        Long userId = 1L;
        User expectedUser = createUser("testuser", "Test User", "test@example.com", "password123");
        when(database.find(User.class, userId)).thenReturn(expectedUser);

        // Act
        Optional<User> result = userRepository.findById(userId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
        verify(database).find(User.class, userId);
    }

    @Test
    void testFindById_WhenUserDoesNotExist_ReturnsEmptyOptional() {
        // Arrange
        Long userId = 999L;
        when(database.find(User.class, userId)).thenReturn(null);

        // Act
        Optional<User> result = userRepository.findById(userId);

        // Assert
        assertFalse(result.isPresent());
        verify(database).find(User.class, userId);
    }

    @Test
    void testFindByUsername_WhenUserExists_ReturnsOptionalContainingUser() {
        // Arrange
        String username = "testuser";
        User expectedUser = createUser(username, "Test User", "test@example.com", "password123");
        when(database.find(User.class)).thenReturn(query);
        when(query.where()).thenReturn(expressionList);
        when(expressionList.eq("username", username)).thenReturn(expressionList);
        when(expressionList.findOne()).thenReturn(expectedUser);

        // Act
        Optional<User> result = userRepository.findByUsername(username);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
        verify(expressionList).findOne();
    }

    @Test
    void testFindByUsername_WhenUserDoesNotExist_ReturnsEmptyOptional() {
        // Arrange
        String username = "nonexistent";
        when(database.find(User.class)).thenReturn(query);
        when(query.where()).thenReturn(expressionList);
        when(expressionList.eq("username", username)).thenReturn(expressionList);
        when(expressionList.findOne()).thenReturn(null);

        // Act
        Optional<User> result = userRepository.findByUsername(username);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByEmail_WhenUserExists_ReturnsOptionalContainingUser() {
        // Arrange
        String email = "test@example.com";
        User expectedUser = createUser("testuser", "Test User", email, "password123");
        when(database.find(User.class)).thenReturn(query);
        when(query.where()).thenReturn(expressionList);
        when(expressionList.eq("email", email)).thenReturn(expressionList);
        when(expressionList.findOne()).thenReturn(expectedUser);

        // Act
        Optional<User> result = userRepository.findByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
    }

    @Test
    void testFindByEmail_WhenUserDoesNotExist_ReturnsEmptyOptional() {
        // Arrange
        String email = "nonexistent@example.com";
        when(database.find(User.class)).thenReturn(query);
        when(query.where()).thenReturn(expressionList);
        when(expressionList.eq("email", email)).thenReturn(expressionList);
        when(expressionList.findOne()).thenReturn(null);

        // Act
        Optional<User> result = userRepository.findByEmail(email);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testFindAll_ReturnsListOfUsers() {
        // Arrange
        List<User> expectedUsers = List.of(
                createUser("user1", "User One", "user1@example.com", "pass1"),
                createUser("user2", "User Two", "user2@example.com", "pass2")
        );
        when(database.find(User.class)).thenReturn(query);
        when(query.findList()).thenReturn(expectedUsers);

        // Act
        List<User> result = userRepository.findAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals(expectedUsers, result);
    }

    @Test
    void testFindActiveUsers_ReturnsOnlyActiveUsers() {
        // Arrange
        List<User> expectedActiveUsers = List.of(
                createUser("active1", "Active One", "active1@example.com", "pass1")
        );
        when(database.find(User.class)).thenReturn(query);
        when(query.where()).thenReturn(expressionList);
        when(expressionList.eq("isActive", true)).thenReturn(expressionList);
        when(expressionList.findList()).thenReturn(expectedActiveUsers);

        // Act
        List<User> result = userRepository.findActiveUsers();

        // Assert
        assertEquals(1, result.size());
        assertEquals(expectedActiveUsers, result);
    }

    @Test
    void testSave_SavesUserAndReturnsIt() {
        // Arrange
        User user = createUser("newuser", "New User", "new@example.com", "password123");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // Act
        User result = userRepository.save(user);

        // Assert
        verify(database).save(userCaptor.capture());
        assertEquals(user, userCaptor.getValue());
        assertEquals(user, result);
    }

    @Test
    void testUpdate_UpdatesUser() {
        // Arrange
        User user = createUser("existinguser", "Existing User", "existing@example.com", "password123");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // Act
        userRepository.update(user);

        // Assert
        verify(database).update(userCaptor.capture());
        assertEquals(user, userCaptor.getValue());
    }

    @Test
    void testDelete_DeletesUser() {
        // Arrange
        User user = createUser("todelete", "To Delete", "delete@example.com", "password123");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // Act
        userRepository.delete(user);

        // Assert
        verify(database).delete(userCaptor.capture());
        assertEquals(user, userCaptor.getValue());
    }

    @Test
    void testDeleteById_DeletesUserById() {
        // Arrange
        Long userId = 1L;
        ArgumentCaptor<Class<User>> classCaptor = ArgumentCaptor.forClass(Class.class);
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);

        // Act
        userRepository.deleteById(userId);

        // Assert
        verify(database).delete(classCaptor.capture(), idCaptor.capture());
        assertEquals(User.class, classCaptor.getValue());
        assertEquals(userId, idCaptor.getValue());
    }

    @Test
    void testExistsByUsername_WhenUserExists_ReturnsTrue() {
        // Arrange
        String username = "existinguser";
        when(database.find(User.class)).thenReturn(query);
        when(query.where()).thenReturn(expressionList);
        when(expressionList.eq("username", username)).thenReturn(expressionList);
        when(expressionList.exists()).thenReturn(true);

        // Act
        boolean result = userRepository.existsByUsername(username);

        // Assert
        assertTrue(result);
    }

    @Test
    void testExistsByUsername_WhenUserDoesNotExist_ReturnsFalse() {
        // Arrange
        String username = "nonexistent";
        when(database.find(User.class)).thenReturn(query);
        when(query.where()).thenReturn(expressionList);
        when(expressionList.eq("username", username)).thenReturn(expressionList);
        when(expressionList.exists()).thenReturn(false);

        // Act
        boolean result = userRepository.existsByUsername(username);

        // Assert
        assertFalse(result);
    }

    @Test
    void testExistsByEmail_WhenEmailExists_ReturnsTrue() {
        // Arrange
        String email = "existing@example.com";
        when(database.find(User.class)).thenReturn(query);
        when(query.where()).thenReturn(expressionList);
        when(expressionList.eq("email", email)).thenReturn(expressionList);
        when(expressionList.exists()).thenReturn(true);

        // Act
        boolean result = userRepository.existsByEmail(email);

        // Assert
        assertTrue(result);
    }

    @Test
    void testExistsByEmail_WhenEmailDoesNotExist_ReturnsFalse() {
        // Arrange
        String email = "nonexistent@example.com";
        when(database.find(User.class)).thenReturn(query);
        when(query.where()).thenReturn(expressionList);
        when(expressionList.eq("email", email)).thenReturn(expressionList);
        when(expressionList.exists()).thenReturn(false);

        // Act
        boolean result = userRepository.existsByEmail(email);

        // Assert
        assertFalse(result);
    }

    @Test
    void testExistsById_WhenUserExists_ReturnsTrue() {
        // Arrange
        Long userId = 1L;
        User user = createUser("testuser", "Test User", "test@example.com", "password123");
        when(database.find(User.class, userId)).thenReturn(user);

        // Act
        boolean result = userRepository.existsById(userId);

        // Assert
        assertTrue(result);
    }

    @Test
    void testExistsById_WhenUserDoesNotExist_ReturnsFalse() {
        // Arrange
        Long userId = 999L;
        when(database.find(User.class, userId)).thenReturn(null);

        // Act
        boolean result = userRepository.existsById(userId);

        // Assert
        assertFalse(result);
    }

    @Test
    void testCount_ReturnsUserCount() {
        // Arrange
        int expectedCount = 5;
        when(database.find(User.class)).thenReturn(query);
        when(query.findCount()).thenReturn(expectedCount);

        // Act
        long result = userRepository.count();

        // Assert
        assertEquals(expectedCount, result);
    }

    private User createUser(String username, String fullName, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }
}
