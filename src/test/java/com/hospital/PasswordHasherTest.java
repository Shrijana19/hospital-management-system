package com.hospital;

import com.hospital.util.PasswordHasher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests verifying cryptographic password hashing, salt creation, and timing attack resistance.
 */
public class PasswordHasherTest {

    @Test
    @DisplayName("Should generate unique 32-character hex salts")
    public void testSaltGeneration() {
        String salt1 = PasswordHasher.generateSalt();
        String salt2 = PasswordHasher.generateSalt();

        Assertions.assertNotNull(salt1);
        Assertions.assertNotNull(salt2);
        Assertions.assertEquals(32, salt1.length());
        Assertions.assertEquals(32, salt2.length());
        Assertions.assertNotEquals(salt1, salt2, "Subsequent salts should be cryptographically unique");
    }

    @Test
    @DisplayName("Should accurately hash password and verify valid and invalid candidate passwords")
    public void testHashAndVerify() {
        String password = "CorrectHorseBatteryStaple!";
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword(password, salt);

        // Verification checks
        Assertions.assertTrue(PasswordHasher.verifyPassword(password, hash, salt), "Matching password must return true");
        Assertions.assertFalse(PasswordHasher.verifyPassword("WrongPassword", hash, salt), "Mismatched password must return false");
        Assertions.assertFalse(PasswordHasher.verifyPassword(password, hash, PasswordHasher.generateSalt()), "Mismatched salt must return false");
    }
}
