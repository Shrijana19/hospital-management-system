package com.hospital.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Secure password hashing utility using PBKDF2WithHmacSHA256 from the Java Standard Library.
 * Complies with NIST cryptographic guidelines:
 * - 65,536 iterations
 * - Cryptographically random 16-byte salt
 * - 256-bit derived key length
 * - Constant-time comparison to prevent timing attacks
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
        // Utility class
    }

    /**
     * Generates a random 16-byte salt, returned as a hex string.
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return bytesToHex(salt);
    }

    /**
     * Hashes a plain-text password using the given hex-encoded salt.
     *
     * @param password Plain-text password
     * @param saltHex  Hex string of the salt
     * @return Hex-encoded password hash
     */
    public static String hashPassword(String password, String saltHex) {
        if (password == null || saltHex == null) {
            throw new IllegalArgumentException("Password and salt must not be null");
        }
        try {
            byte[] salt = hexToBytes(saltHex);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Error hashing password with PBKDF2", e);
        }
    }

    /**
     * Verifies whether a candidate plain-text password matches the stored hash and salt.
     * Uses constant-time byte comparison.
     */
    public static boolean verifyPassword(String candidatePassword, String storedHash, String storedSalt) {
        if (candidatePassword == null || storedHash == null || storedSalt == null) {
            return false;
        }
        String calculatedHash = hashPassword(candidatePassword, storedSalt);
        return constantTimeEquals(calculatedHash, storedHash);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Helper main method to generate hashes for seed accounts.
     */
    public static void main(String[] args) {
        String adminSalt = generateSalt();
        String adminHash = hashPassword("admin123", adminSalt);
        System.out.println("Admin Salt: " + adminSalt);
        System.out.println("Admin Hash: " + adminHash);

        String recepSalt = generateSalt();
        String recepHash = hashPassword("recep123", recepSalt);
        System.out.println("Receptionist Salt: " + recepSalt);
        System.out.println("Receptionist Hash: " + recepHash);
    }
}
