package com.hospital.service;

import com.hospital.exception.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Pattern;

/**
 * Centralized business input validation service.
 * Validates entity fields before persistence and throws descriptive ValidationException.
 */
public final class Validator {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s.'\\-]{2,100}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");

    private Validator() {
        // Utility class
    }

    public static String validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName, fieldName + " is required and cannot be empty.");
        }
        return value.trim();
    }

    public static String validateName(String name, String fieldName) {
        String trimmed = validateRequired(name, fieldName);
        if (!NAME_PATTERN.matcher(trimmed).matches()) {
            throw new ValidationException(fieldName,
                    fieldName + " must be 2-100 characters and contain only letters, spaces, hyphens, or periods.");
        }
        return trimmed;
    }

    public static int validateAge(int age, String fieldName, int min, int max) {
        if (age < min || age > max) {
            throw new ValidationException(fieldName,
                    String.format("%s must be between %d and %d years (entered: %d).", fieldName, min, max, age));
        }
        return age;
    }

    public static int validatePatientAge(int age) {
        return validateAge(age, "Patient Age", 0, 125);
    }

    public static int validateDoctorAge(int age) {
        return validateAge(age, "Doctor Age", 21, 100);
    }

    public static String validateGender(String gender) {
        String trimmed = validateRequired(gender, "Gender");
        if (!trimmed.equalsIgnoreCase("Male") && !trimmed.equalsIgnoreCase("Female") && !trimmed.equalsIgnoreCase("Other")) {
            throw new ValidationException("Gender", "Gender must be 'Male', 'Female', or 'Other'.");
        }
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }

    public static String validatePhone(String phone, String fieldName) {
        String trimmed = validateRequired(phone, fieldName);
        String digitsOnly = trimmed.replaceAll("[^0-9]", "");
        if (!PHONE_PATTERN.matcher(digitsOnly).matches()) {
            throw new ValidationException(fieldName,
                    fieldName + " must contain exactly 10 numeric digits (e.g. 9876543210).");
        }
        return digitsOnly;
    }

    public static BigDecimal validateMoney(BigDecimal amount, String fieldName, boolean allowZero) {
        if (amount == null) {
            throw new ValidationException(fieldName, fieldName + " cannot be null.");
        }
        if (amount.scale() > 2) {
            throw new ValidationException(fieldName, fieldName + " cannot have more than 2 decimal places.");
        }
        if (!allowZero && amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(fieldName, fieldName + " must be greater than zero.");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(fieldName, fieldName + " cannot be negative.");
        }
        return amount;
    }

    public static LocalDate validateAppointmentDate(LocalDate date) {
        if (date == null) {
            throw new ValidationException("Appointment Date", "Appointment date is required.");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new ValidationException("Appointment Date", "Cannot schedule an appointment in the past (" + date + ").");
        }
        return date;
    }

    public static LocalTime validateAppointmentTime(LocalTime time) {
        if (time == null) {
            throw new ValidationException("Appointment Time", "Appointment time slot is required.");
        }
        return time;
    }

    public static String validateUsername(String username) {
        String trimmed = validateRequired(username, "Username");
        if (!USERNAME_PATTERN.matcher(trimmed).matches()) {
            throw new ValidationException("Username",
                    "Username must be 3-30 alphanumeric characters (letters, numbers, underscore).");
        }
        return trimmed;
    }

    public static String validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new ValidationException("Password", "Password must be at least 6 characters long.");
        }
        return password;
    }
}
