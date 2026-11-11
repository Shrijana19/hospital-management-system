package com.hospital.dao;

import com.hospital.database.DatabaseConnection;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Doctor;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Doctor persistence.
 * Implements CrudDao<Doctor>.
 */
public class DoctorDao implements CrudDao<Doctor> {

    private static final Logger LOGGER = Logger.getLogger(DoctorDao.class.getName());

    @Override
    public Doctor add(Doctor doctor) {
        String sql = "INSERT INTO doctors (name, age, gender, phone, address, specialization, consultation_fee, available) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, doctor.getName());
            ps.setInt(2, doctor.getAge());
            ps.setString(3, doctor.getGender());
            ps.setString(4, doctor.getPhone());
            ps.setString(5, doctor.getAddress());
            ps.setString(6, doctor.getSpecialization());
            ps.setBigDecimal(7, doctor.getConsultationFee());
            ps.setBoolean(8, doctor.isAvailable());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Creating doctor failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    doctor.setId(generatedKeys.getInt(1));
                } else {
                    throw new DataAccessException("Creating doctor failed, no ID obtained.");
                }
            }
            return doctor;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error adding doctor: " + doctor.getName(), e);
            throw new DataAccessException("Failed to register doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Doctor doctor) {
        String sql = "UPDATE doctors SET name = ?, age = ?, gender = ?, phone = ?, address = ?, "
                   + "specialization = ?, consultation_fee = ?, available = ? WHERE doctor_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, doctor.getName());
            ps.setInt(2, doctor.getAge());
            ps.setString(3, doctor.getGender());
            ps.setString(4, doctor.getPhone());
            ps.setString(5, doctor.getAddress());
            ps.setString(6, doctor.getSpecialization());
            ps.setBigDecimal(7, doctor.getConsultationFee());
            ps.setBoolean(8, doctor.isAvailable());
            ps.setInt(9, doctor.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error updating doctor id: " + doctor.getId(), e);
            throw new DataAccessException("Failed to update doctor details: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM doctors WHERE doctor_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error deleting doctor id: " + id, e);
            if (e.getErrorCode() == 1451) {
                throw new DataAccessException("Cannot delete doctor (ID " + id + ") because active appointments or medical records exist.", e);
            }
            throw new DataAccessException("Failed to delete doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Doctor> getById(int id) {
        String sql = "SELECT * FROM doctors WHERE doctor_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToDoctor(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error fetching doctor id: " + id, e);
            throw new DataAccessException("Failed to retrieve doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Doctor> getAll() {
        String sql = "SELECT * FROM doctors ORDER BY doctor_id ASC";
        List<Doctor> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToDoctor(rs));
            }
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error fetching all doctors", e);
            throw new DataAccessException("Failed to retrieve doctors list: " + e.getMessage(), e);
        }
    }

    public List<Doctor> getAvailableDoctors() {
        String sql = "SELECT * FROM doctors WHERE available = 1 ORDER BY name ASC";
        List<Doctor> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToDoctor(rs));
            }
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error fetching available doctors", e);
            throw new DataAccessException("Failed to retrieve available doctors: " + e.getMessage(), e);
        }
    }

    public List<Doctor> searchByName(String nameQuery) {
        String sql = "SELECT * FROM doctors WHERE LOWER(name) LIKE ? ORDER BY name ASC";
        List<Doctor> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + nameQuery.trim().toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToDoctor(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error searching doctors by name: " + nameQuery, e);
            throw new DataAccessException("Failed to search doctors by name: " + e.getMessage(), e);
        }
    }

    public List<Doctor> searchBySpecialization(String specQuery) {
        String sql = "SELECT * FROM doctors WHERE LOWER(specialization) LIKE ? ORDER BY name ASC";
        List<Doctor> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + specQuery.trim().toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToDoctor(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error searching doctors by specialization: " + specQuery, e);
            throw new DataAccessException("Failed to search doctors by specialization: " + e.getMessage(), e);
        }
    }

    public List<String> getAllSpecializations() {
        String sql = "SELECT DISTINCT specialization FROM doctors ORDER BY specialization ASC";
        List<String> specs = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                specs.add(rs.getString("specialization"));
            }
            return specs;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error fetching specializations", e);
            return specs;
        }
    }

    public boolean hasActiveReferences(int doctorId) {
        String sql = "SELECT "
                   + "(SELECT COUNT(*) FROM appointments WHERE doctor_id = ?) + "
                   + "(SELECT COUNT(*) FROM medical_records WHERE doctor_id = ?) AS total_refs";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            ps.setInt(2, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_refs") > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error checking references for doctor: " + doctorId, e);
            throw new DataAccessException("Failed to verify doctor references: " + e.getMessage(), e);
        }
    }

    public int countTotalDoctors() {
        String sql = "SELECT COUNT(*) FROM doctors";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error counting doctors", e);
            return 0;
        }
    }

    private Doctor mapRowToDoctor(ResultSet rs) throws SQLException {
        BigDecimal fee = rs.getBigDecimal("consultation_fee");
        return new Doctor(
                rs.getInt("doctor_id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("gender"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getString("specialization"),
                fee != null ? fee : BigDecimal.ZERO,
                rs.getBoolean("available")
        );
    }
}
