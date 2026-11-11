package com.hospital;

import com.hospital.exception.ValidationException;
import com.hospital.service.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for business input validation rules.
 */
public class ValidatorTest {

    @Test
    @DisplayName("Should accept valid patient names and reject invalid or empty names")
    public void testNameValidation() {
        // Valid
        Assertions.assertEquals("John Doe", Validator.validateName("John Doe", "Name"));
        Assertions.assertEquals("Dr. Mary-Jane Watson", Validator.validateName("Dr. Mary-Jane Watson", "Name"));

        // Invalid
        Assertions.assertThrows(ValidationException.class, () -> Validator.validateName("", "Name"));
        Assertions.assertThrows(ValidationException.class, () -> Validator.validateName("   ", "Name"));
        Assertions.assertThrows(ValidationException.class, () -> Validator.validateName("A", "Name")); // too short
        Assertions.assertThrows(ValidationException.class, () -> Validator.validateName("John@123", "Name")); // invalid chars
    }

    @Test
    @DisplayName("Should validate patient age within range [0, 125]")
    public void testPatientAgeValidation() {
        Assertions.assertEquals(0, Validator.validatePatientAge(0));
        Assertions.assertEquals(45, Validator.validatePatientAge(45));
        Assertions.assertEquals(125, Validator.validatePatientAge(125));

        Assertions.assertThrows(ValidationException.class, () -> Validator.validatePatientAge(-1));
        Assertions.assertThrows(ValidationException.class, () -> Validator.validatePatientAge(126));
    }

    @Test
    @DisplayName("Should validate doctor age within professional range [21, 100]")
    public void testDoctorAgeValidation() {
        Assertions.assertEquals(21, Validator.validateDoctorAge(21));
        Assertions.assertEquals(50, Validator.validateDoctorAge(50));
        Assertions.assertEquals(100, Validator.validateDoctorAge(100));

        Assertions.assertThrows(ValidationException.class, () -> Validator.validateDoctorAge(20));
        Assertions.assertThrows(ValidationException.class, () -> Validator.validateDoctorAge(105));
    }

    @Test
    @DisplayName("Should accept exactly 10 digits for phone and reject other lengths")
    public void testPhoneValidation() {
        Assertions.assertEquals("9876543210", Validator.validatePhone("9876543210", "Phone"));
        Assertions.assertEquals("9876543210", Validator.validatePhone("987-654-3210", "Phone")); // stripped

        Assertions.assertThrows(ValidationException.class, () -> Validator.validatePhone("12345", "Phone")); // too short
        Assertions.assertThrows(ValidationException.class, () -> Validator.validatePhone("98765432100", "Phone")); // 11 digits
        Assertions.assertThrows(ValidationException.class, () -> Validator.validatePhone("abcdefghij", "Phone"));
    }

    @Test
    @DisplayName("Should validate monetary amounts and reject negative values or >2 decimal places")
    public void testMoneyValidation() {
        Assertions.assertEquals(new BigDecimal("100.50"),
                Validator.validateMoney(new BigDecimal("100.50"), "Fee", true));
        Assertions.assertEquals(BigDecimal.ZERO,
                Validator.validateMoney(BigDecimal.ZERO, "Fee", true));

        // Negative rejection
        Assertions.assertThrows(ValidationException.class, () ->
                Validator.validateMoney(new BigDecimal("-10.00"), "Fee", true));

        // Too many decimal places rejection (e.g. 10.999)
        Assertions.assertThrows(ValidationException.class, () ->
                Validator.validateMoney(new BigDecimal("10.999"), "Fee", true));
    }

    @Test
    @DisplayName("Should reject appointment dates scheduled in the past")
    public void testAppointmentDateValidation() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Assertions.assertEquals(tomorrow, Validator.validateAppointmentDate(tomorrow));

        LocalDate yesterday = LocalDate.now().minusDays(1);
        Assertions.assertThrows(ValidationException.class, () -> Validator.validateAppointmentDate(yesterday));
    }
}
