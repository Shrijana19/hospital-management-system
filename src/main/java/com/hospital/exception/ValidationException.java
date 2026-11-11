package com.hospital.exception;

/**
 * Custom exception thrown when user input violates business validation rules.
 * Enables the service layer to reject bad input before database execution and
 * supplies user-friendly messages for the Swing UI dialogs.
 */
public class ValidationException extends RuntimeException {

    private final String fieldName;

    public ValidationException(String message) {
        super(message);
        this.fieldName = null;
    }

    public ValidationException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
