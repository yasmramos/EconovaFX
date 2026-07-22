package com.econovafx.modules.core.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PasswordService
 */
class PasswordServiceTest {

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
    }

    @Test
    void testHashPasswordReturnsNonNull() {
        String hashed = passwordService.hashPassword("password123");
        assertNotNull(hashed);
        assertTrue(hashed.startsWith("$2a$"));
    }

    @Test
    void testHashPasswordDifferentForSameInput() {
        String hash1 = passwordService.hashPassword("password123");
        String hash2 = passwordService.hashPassword("password123");
        // BCrypt uses salt, so hashes should be different
        assertNotEquals(hash1, hash2);
    }

    @Test
    void testCheckPasswordCorrect() {
        String plainPassword = "password123";
        String hashed = passwordService.hashPassword(plainPassword);
        assertTrue(passwordService.checkPassword(plainPassword, hashed));
    }

    @Test
    void testCheckPasswordIncorrect() {
        String plainPassword = "password123";
        String hashed = passwordService.hashPassword(plainPassword);
        assertFalse(passwordService.checkPassword("wrongpassword", hashed));
    }

    @Test
    void testCheckPasswordWithInvalidHash() {
        assertFalse(passwordService.checkPassword("password123", "invalid_hash"));
    }

    @Test
    void testCheckPasswordWithNullHash() {
        assertThrows(IllegalArgumentException.class, () -> {
            passwordService.checkPassword("password123", null);
        });
    }

    @Test
    void testCheckPasswordWithEmptyPassword() {
        String hashed = passwordService.hashPassword("password123");
        assertFalse(passwordService.checkPassword("", hashed));
    }
}
