package com.nadi.security;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PasswordPolicyTest {

    @Test
    void rejectsNullPassword() {
        assertThrows(RuntimeException.class, () -> PasswordPolicy.validateOrThrow(null));
    }

    @Test
    void rejectsBlankPassword() {
        assertThrows(RuntimeException.class, () -> PasswordPolicy.validateOrThrow("   "));
    }

    @Test
    void rejectsTooShortPassword() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> PasswordPolicy.validateOrThrow("abc123"));
        assertTrue(ex.getMessage().contains("8"));
    }

    @Test
    void acceptsExactlyMinLength() {
        assertDoesNotThrow(() -> PasswordPolicy.validateOrThrow("abcdefgh"));
    }

    @Test
    void acceptsLongPassword() {
        assertDoesNotThrow(() -> PasswordPolicy.validateOrThrow("a-very-long-and-strong-password-123!"));
    }

    @Test
    void generatedTemporaryPasswordIsStrongAndUnique() {
        String first = PasswordPolicy.generateTemporaryPassword();
        String second = PasswordPolicy.generateTemporaryPassword();
        assertEquals(12, first.length());
        assertTrue(first.matches("[A-Za-z0-9]+"));
        assertNotEquals(first, second);
        Set<String> batch = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            batch.add(PasswordPolicy.generateTemporaryPassword());
        }
        assertEquals(100, batch.size());
    }
}
