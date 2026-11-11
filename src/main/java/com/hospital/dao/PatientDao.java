package com.hospital.dao;

import com.hospital.database.DatabaseConnection;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Patient;
import java.sql.Connection;
import java.sql.Date;
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
 * Data Access Object for Patient persistence.
 * Implements CrudDao<Patient>. All queries use PreparedStatement and try-with-resources.
 */
public class PatientDao implements CrudDao<Patient> {

    private static final Logger LOGGER = Logger.getLogger(PatientDao.class.getName());

    @Override
    public Patient add(Patient patient) {
        String sql = "INSERT INTO patients (name, age, gender, phone, address, medical_history, registered_on) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, patient.getName());
            ps.setInt(2, patient.getAge());
            ps.setString(3, patient.getGender());
            ps.setString(4, patient.getPhone());
            ps.setString(5, patient.getAddress());
            ps.setString(6, patient.getMedicalHistory());
            ps.setDate(7, Date.valueOf(patient.getRegisteredOn()));

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Creating patient failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    patient.setId(generatedKeys.getInt(1));
                } else {
                    throw new DataAccessException("Creating patient failed, no ID obtained.");
                }
            }
            return patient;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error adding patient: " + patient.getName(), e);
            throw new DataAccessException("Failed to register patient: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Patient patient) {
        String sql = "UPDATE patients SET name = ?, age = ?, gender = ?, phone = ?, "
                   + "address = ?, medical_history = ? WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, patient.getName());
            ps.setInt(2, patient.getAge());
            ps.setString(3, patient.getGender());
            ps.setString(4, patient.getPhone());
            ps.setString(5, patient.getAddress());
            ps.setString(6, patient.getMedicalHistory());
            ps.setInt(7, patient.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error updating patient id: " + patient.getId(), e);
            throw new DataAccessException("Failed to update patient details: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM patients WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error deleting patient id: " + id, e);
            if (e.getErrorCode() == 1451) {
                throw new DataAccessException("Cannot delete patient (ID " + id + ") because existing appointments, medical records, or bills reference this patient.", e);
            }
            throw new DataAccessException("Failed to delete patient: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Patient> getById(int id) {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToPatient(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error fetching patient id: " + id, e);
            throw new DataAccessException("Failed to retrieve patient: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Patient> getAll() {
        String sql = "SELECT * FROM patients ORDER BY patient_id DESC";
        List<Patient> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToPatient(rs));
            }
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error fetching all patients", e);
            throw new DataAccessException("Failed to retrieve patient directory: " + e.getMessage(), e);
        }
    }

    /**
     * Searches patients by partial name match (case-insensitive).
     */
    public List<Patient> searchByName(String nameQuery) {
        String sql = "SELECT * FROM patients WHERE LOWER(name) LIKE ? ORDER BY name ASC";
        List<Patient> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + nameQuery.trim().toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToPatient(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error searching patients by name: " + nameQuery, e);
            throw new DataAccessException("Failed to search patients by name: " + e.getMessage(), e);
        }
    }

    /**
     * Finds a patient by exact phone number.
     */
    public Optional<Patient> findByPhone(String phone) {
        String sql = "SELECT * FROM patients WHERE phone = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, phone.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToPatient(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error finding patient by phone: " + phone, e);
            throw new DataAccessException("Failed to query patient by phone: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if a patient has any appointments, bills, or medical records.
     */
    public boolean hasActiveReferences(int patientId) {
        String sql = "SELECT "
                   + "(SELECT COUNT(*) FROM appointments WHERE patient_id = ?) + "
                   + "(SELECT COUNT(*) FROM bills WHERE patient_id = ?) + "
                   + "(SELECT COUNT(*) FROM medical_records WHERE patient_id = ?) AS total_refs";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ps.setInt(2, patientId);
            ps.setInt(3, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_refs") > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error checking references for patient: " + patientId, e);
            throw new DataAccessException("Failed to verify patient references: " + e.getMessage(), e);
        }
    }

    public int countTotalPatients() {
        String sql = "SELECT COUNT(*) FROM patients";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error counting patients", e);
            return 0;
        }
    }

    private Patient mapRowToPatient(ResultSet rs) throws SQLException {
        Date regDate = rs.getDate("registered_on");
        return new Patient(
                rs.getInt("patient_id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("gender"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getString("medical_history"),
                regDate != null ? regDate.toLocalDate() : null
        );
    }
}
