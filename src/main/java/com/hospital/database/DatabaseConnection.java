package com.hospital.database;

import com.hospital.exception.DataAccessException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages database connectivity via JDBC and DriverManager.
 * Configuration is dynamically loaded from db.properties file.
 * Thread-safe and designed for try-with-resources usage.
 */
public final class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/hospital_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "root";
    private static final int DEFAULT_TIMEOUT_SECONDS = 5;

    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;
    private static int loginTimeout;

    static {
        loadConfiguration();
    }

    private DatabaseConnection() {
        // Prevent direct instantiation
    }

    /**
     * Loads database credentials from db.properties if available,
     * otherwise falls back to standard developer defaults.
     */
    public static synchronized void loadConfiguration() {
        Properties props = new Properties();
        boolean loaded = false;

        // 1. Try loading from classpath
        try (InputStream is = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                props.load(is);
                loaded = true;
                LOGGER.info("Loaded database configuration from classpath db.properties");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not load db.properties from classpath", e);
        }

        // 2. Try loading from working directory if not found on classpath
        if (!loaded) {
            File localFile = new File("src/main/resources/db.properties");
            if (!localFile.exists()) {
                localFile = new File("resources/db.properties");
            }
            if (!localFile.exists()) {
                localFile = new File("db.properties");
            }

            if (localFile.exists()) {
                try (InputStream fis = new FileInputStream(localFile)) {
                    props.load(fis);
                    loaded = true;
                    LOGGER.info("Loaded database configuration from " + localFile.getAbsolutePath());
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Could not load db.properties from file system", e);
                }
            }
        }

        dbUrl = props.getProperty("db.url", DEFAULT_URL).trim();
        dbUser = props.getProperty("db.username", DEFAULT_USER).trim();
        dbPassword = props.getProperty("db.password", DEFAULT_PASS);
        try {
            loginTimeout = Integer.parseInt(props.getProperty("db.loginTimeoutSeconds", String.valueOf(DEFAULT_TIMEOUT_SECONDS)));
        } catch (NumberFormatException e) {
            loginTimeout = DEFAULT_TIMEOUT_SECONDS;
        }

        try {
            DriverManager.setLoginTimeout(loginTimeout);
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL Connector/J driver (com.mysql.cj.jdbc.Driver) not found on classpath!", e);
        }
    }

    /**
     * Obtains a new JDBC Connection.
     * Callers MUST manage this connection using try-with-resources.
     *
     * @return Connection instance
     * @throws DataAccessException if connection fails
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (SQLException e) {
            String errorMsg = buildFriendlyErrorMessage(e);
            LOGGER.log(Level.SEVERE, "Failed to connect to MySQL database: " + e.getMessage(), e);
            throw new DataAccessException(errorMsg, e);
        }
    }

    /**
     * Checks if the database is reachable and returns a diagnostic message.
     * Used at application startup.
     */
    public static ConnectionStatus testConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            return new ConnectionStatus(false,
                    "MySQL JDBC Driver (com.mysql.cj.jdbc.Driver) not found.\n"
                    + "Please ensure mysql-connector-j.jar is included on the classpath.");
        }

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            boolean valid = conn.isValid(loginTimeout);
            if (valid) {
                return new ConnectionStatus(true, "Successfully connected to MySQL database: " + dbUrl);
            } else {
                return new ConnectionStatus(false, "Connection check returned false. Database may be unresponsive.");
            }
        } catch (SQLException e) {
            return new ConnectionStatus(false, buildFriendlyErrorMessage(e));
        }
    }

    private static String buildFriendlyErrorMessage(SQLException e) {
        String msg = e.getMessage().toLowerCase();
        if (msg.contains("communications link failure") || msg.contains("connection refused")) {
            return "Cannot reach MySQL server at 127.0.0.1:3306.\n"
                    + "Please ensure MySQL Server is installed, running as a service, and listening on port 3306.\n"
                    + "Technical detail: " + e.getMessage();
        } else if (msg.contains("access denied for user")) {
            return "Access denied for MySQL user '" + dbUser + "'.\n"
                    + "Please verify your username and password in db.properties.\n"
                    + "Technical detail: " + e.getMessage();
        } else if (msg.contains("unknown database")) {
            return "Database 'hospital_management' not found.\n"
                    + "Please execute the 'sql/schema.sql' script in MySQL Workbench to create the database schema.\n"
                    + "Technical detail: " + e.getMessage();
        }
        return "Database error (" + e.getErrorCode() + "): " + e.getMessage();
    }

    public static String getDbUrl() {
        return dbUrl;
    }

    public static String getDbUser() {
        return dbUser;
    }

    public static void main(String[] args) {
        ConnectionStatus status = testConnection();
        System.out.println("Connection status: " + status.isConnected());
        System.out.println("Message: " + status.getMessage());
    }

    /**
     * Status payload for connection verification.
     */
    public static class ConnectionStatus {
        private final boolean connected;
        private final String message;

        public ConnectionStatus(boolean connected, String message) {
            this.connected = connected;
            this.message = message;
        }

        public boolean isConnected() {
            return connected;
        }

        public String getMessage() {
            return message;
        }
    }
}
