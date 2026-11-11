package com.hospital.dao;

import com.hospital.database.DatabaseConnection;
import com.hospital.exception.DataAccessException;
import com.hospital.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for User authentication and credential persistence.
 * Implements CrudDao<User>.
 */
public class UserDao implements CrudDao<User> {

    private static final Logger LOGGER = Logger.getLogger(UserDao.class.getName());

    @Override
    public User add(User user) {
        String sql = "INSERT INTO users (username, password_hash, salt, role, full_name) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getSalt());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getFullName());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                } else {
                    throw new DataAccessException("Creating user failed, no ID obtained.");
                }
            }
            return user;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error adding user: " + user.getUsername(), e);
            if (e.getErrorCode() == 1062) {
                throw new DataAccessException("Username '" + user.getUsername() + "' is already taken.", e);
            }
            throw new DataAccessException("Failed to create user account: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET username = ?, password_hash = ?, salt = ?, role = ?, full_name = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getSalt());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getFullName());
            ps.setInt(6, user.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error updating user id: " + user.getId(), e);
            throw new DataAccessException("Failed to update user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error deleting user id: " + id, e);
            throw new DataAccessException("Failed to delete user: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> getById(int id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error fetching user id: " + id, e);
            throw new DataAccessException("Failed to retrieve user: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> getAll() {
        String sql = "SELECT * FROM users ORDER BY user_id ASC";
        List<User> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToUser(rs));
            }
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error fetching all users", e);
            throw new DataAccessException("Failed to retrieve user accounts: " + e.getMessage(), e);
        }
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error finding user by username: " + username, e);
            throw new DataAccessException("Failed to lookup user: " + e.getMessage(), e);
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("salt"),
                rs.getString("role"),
                rs.getString("full_name")
        );
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            user.setCreatedAt(ts.toLocalDateTime());
        }
        return user;
    }
}
