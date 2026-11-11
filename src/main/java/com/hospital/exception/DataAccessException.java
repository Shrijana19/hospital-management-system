package com.hospital.exception;

/**
 * Custom unchecked exception to wrap low-level SQLExceptions.
 * Prevents database and vendor-specific implementation details from leaking
 * into the service and user interface layers (Abstraction).
 */
public class DataAccessException extends RuntimeException {

    private final String sqlState;
    private final int errorCode;

    public DataAccessException(String message) {
        super(message);
        this.sqlState = null;
        this.errorCode = 0;
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
        if (cause instanceof java.sql.SQLException) {
            java.sql.SQLException sqlEx = (java.sql.SQLException) cause;
            this.sqlState = sqlEx.getSQLState();
            this.errorCode = sqlEx.getErrorCode();
        } else {
            this.sqlState = null;
            this.errorCode = 0;
        }
    }

    public String getSqlState() {
        return sqlState;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
