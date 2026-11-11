package com.hospital.dao;

import com.hospital.database.DatabaseConnection;
import com.hospital.exception.DataAccessException;
import com.hospital.model.MedicalRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Medical Record persistence.
 * Implements CrudDao<MedicalRecord>.
 */
public class MedicalRecordDao implements CrudDao<MedicalRecord> {

    private static final Logger LOGGER = Logger.getLogger(MedicalRecordDao.class.getName());

    private static final String BASE_SELECT =
            "SELECT m.record_id, m.patient_id, m.doctor_id, m.appointment_id, "
            + "m.diagnosis, m.prescribed_medicines, m.treatment_details, m.doctor_notes, m.record_date, "
            + "p.name AS patient_name, d.name AS doctor_name "
            + "FROM medical_records m "
            + "INNER JOIN patients p ON m.patient_id = p.patient_id "
            + "INNER JOIN doctors d ON m.doctor_id = d.doctor_id ";

    @Override
    public MedicalRecord add(MedicalRecord record) {
        String sql = "INSERT INTO medical_records (patient_id, doctor_id, appointment_id, diagnosis, "
                   + "prescribed_medicines, treatment_details, doctor_notes, record_date) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, record.getPatientId());
            ps.setInt(2, record.getDoctorId());
            if (record.getAppointmentId() != null) {
                ps.setInt(3, record.getAppointmentId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, record.getDiagnosis());
            ps.setString(5, record.getPrescribedMedicines());
            ps.setString(6, record.getTreatmentDetails());
            ps.setString(7, record.getDoctorNotes());
            ps.setTimestamp(8, Timestamp.valueOf(record.getRecordDate()));

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Saving medical record failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    record.setId(generatedKeys.getInt(1));
                } else {
                    throw new DataAccessException("Saving medical record failed, no ID obtained.");
                }
            }
            return record;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error adding medical record", e);
            throw new DataAccessException("Failed to record clinical entry: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(MedicalRecord record) {
        String sql = "UPDATE medical_records SET patient_id = ?, doctor_id = ?, appointment_id = ?, "
                   + "diagnosis = ?, prescribed_medicines = ?, treatment_details = ?, doctor_notes = ? "
                   + "WHERE record_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, record.getPatientId());
            ps.setInt(2, record.getDoctorId());
            if (record.getAppointmentId() != null) {
                ps.setInt(3, record.getAppointmentId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, record.getDiagnosis());
            ps.setString(5, record.getPrescribedMedicines());
            ps.setString(6, record.getTreatmentDetails());
            ps.setString(7, record.getDoctorNotes());
            ps.setInt(8, record.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error updating medical record id: " + record.getId(), e);
            throw new DataAccessException("Failed to update medical record: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM medical_records WHERE record_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error deleting medical record id: " + id, e);
            throw new DataAccessException("Failed to delete medical record: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<MedicalRecord> getById(int id) {
        String sql = BASE_SELECT + "WHERE m.record_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToRecord(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error fetching medical record id: " + id, e);
            throw new DataAccessException("Failed to retrieve medical record: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MedicalRecord> getAll() {
        String sql = BASE_SELECT + "ORDER BY m.record_date DESC";
        List<MedicalRecord> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToRecord(rs));
            }
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error fetching all medical records", e);
            throw new DataAccessException("Failed to retrieve medical history: " + e.getMessage(), e);
        }
    }

    public List<MedicalRecord> getByPatientId(int patientId) {
        String sql = BASE_SELECT + "WHERE m.patient_id = ? ORDER BY m.record_date DESC";
        List<MedicalRecord> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToRecord(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error fetching records for patient: " + patientId, e);
            throw new DataAccessException("Failed to query patient medical records: " + e.getMessage(), e);
        }
    }

    public List<MedicalRecord> getByDoctorId(int doctorId) {
        String sql = BASE_SELECT + "WHERE m.doctor_id = ? ORDER BY m.record_date DESC";
        List<MedicalRecord> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToRecord(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error fetching records for doctor: " + doctorId, e);
            throw new DataAccessException("Failed to query doctor medical records: " + e.getMessage(), e);
        }
    }

    public Optional<MedicalRecord> getByAppointmentId(int appointmentId) {
        String sql = BASE_SELECT + "WHERE m.appointment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToRecord(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error fetching record for appointment: " + appointmentId, e);
            throw new DataAccessException("Failed to query record for appointment: " + e.getMessage(), e);
        }
    }

    private MedicalRecord mapRowToRecord(ResultSet rs) throws SQLException {
        int apptId = rs.getInt("appointment_id");
        Integer appointmentId = rs.wasNull() ? null : apptId;

        MedicalRecord record = new MedicalRecord(
                rs.getInt("record_id"),
                rs.getInt("patient_id"),
                rs.getInt("doctor_id"),
                appointmentId,
                rs.getString("diagnosis"),
                rs.getString("prescribed_medicines"),
                rs.getString("treatment_details"),
                rs.getString("doctor_notes")
        );
        Timestamp ts = rs.getTimestamp("record_date");
        if (ts != null) {
            record.setRecordDate(ts.toLocalDateTime());
        }
        record.setPatientName(rs.getString("patient_name"));
        record.setDoctorName(rs.getString("doctor_name"));
        return record;
    }
}
