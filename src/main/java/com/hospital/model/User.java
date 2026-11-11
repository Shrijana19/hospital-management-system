package com.hospital.model;

import java.time.LocalDateTime;

/**
 * Model representing a system user authorized to access the application.
 * Supports role-based access control (RBAC): ADMIN and RECEPTIONIST.
 */
public class User {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_RECEPTIONIST = "RECEPTIONIST";

    private int id;
    private String username;
    private String passwordHash;
    private String salt;
    private String role;
    private String fullName;
    private LocalDateTime createdAt;

    public User() {
        this.role = ROLE_RECEPTIONIST;
        this.createdAt = LocalDateTime.now();
    }

    public User(int id, String username, String passwordHash, String salt, String role, String fullName) {
        setId(id);
        setUsername(username);
        setPasswordHash(passwordHash);
        setSalt(salt);
        setRole(role);
        setFullName(fullName);
        this.createdAt = LocalDateTime.now();
    }

    public User(String username, String passwordHash, String salt, String role, String fullName) {
        this(0, username, passwordHash, salt, role, fullName);
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equalsIgnoreCase(role);
    }

    public boolean isReceptionist() {
        return ROLE_RECEPTIONIST.equalsIgnoreCase(role);
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("User ID cannot be negative");
        }
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        this.username = username.trim();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("Password hash cannot be null or empty");
        }
        this.passwordHash = passwordHash.trim();
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        if (salt == null || salt.trim().isEmpty()) {
            throw new IllegalArgumentException("Salt cannot be null or empty");
        }
        this.salt = salt.trim();
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        if (role == null || (!role.equalsIgnoreCase(ROLE_ADMIN) && !role.equalsIgnoreCase(ROLE_RECEPTIONIST))) {
            throw new IllegalArgumentException("Role must be 'ADMIN' or 'RECEPTIONIST'");
        }
        this.role = role.toUpperCase();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be null or empty");
        }
        this.fullName = fullName.trim();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("User #%d [%s] (%s) - %s", id, username, role, fullName);
    }
}
