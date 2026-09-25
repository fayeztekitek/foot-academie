package com.nadi.security;

import java.security.SecureRandom;

/**
 * Single source of truth for password rules.
 * Minimum 8 characters everywhere (self-change, admin reset, invitation, onboarding).
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;

    private static final String TEMP_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final int TEMP_LENGTH = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordPolicy() {
    }

    public static void validateOrThrow(String password) {
        if (password == null || password.isBlank()) {
            throw new RuntimeException("Le mot de passe est obligatoire");
        }
        if (password.length() < MIN_LENGTH) {
            throw new RuntimeException(
                    "Le mot de passe doit contenir au moins " + MIN_LENGTH + " caractères");
        }
    }

    /** Unpredictable temporary password (replaces the old "Nadi"+4-hex scheme). */
    public static String generateTemporaryPassword() {
        StringBuilder sb = new StringBuilder(TEMP_LENGTH);
        for (int i = 0; i < TEMP_LENGTH; i++) {
            sb.append(TEMP_ALPHABET.charAt(RANDOM.nextInt(TEMP_ALPHABET.length())));
        }
        return sb.toString();
    }
}
