package com.hospital.dao;

import com.hospital.database.DatabaseConnection;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Appointment;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Appointment persistence.
 * Implements CrudDao<Appointment>.
 * Automatically joins patient and doctor details for display efficiency.
 */
public class AppointmentDao implements CrudDao<Appointment> {

    private static final Logger LOGGER = Logger.getLogger(AppointmentDao.class.getName());

    private static final String BASE_SELECT =
            "SELECT a.appointment_id, a.patient_id, a.doctor_id, a.appointment_date, "
            + "a.appointment_time, a.status, a.created_at, "
            + "p.name AS patient_name, d.name AS doctor_name, d.specialization AS doctor_specialization "
            + "FROM appointments a "
            + "INNER JOIN patients p ON a.patient_id = p.patient_id "
            + "INNER JOIN doctors d ON a.doctor_id = d.doctor_id ";

    @Override
    public Appointment add(Appointment appointment) {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, appointment.getPatientId());
            ps.setInt(2, appointment.getDoctorId());
            ps.setDate(3, Date.valueOf(appointment.getAppointmentDate()));
            ps.setTime(4, Time.valueOf(appointment.getAppointmentTime()));
            ps.setString(5, appointment.getStatus());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Booking appointment failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    appointment.setId(generatedKeys.getInt(1));
                } else {
                    throw new DataAccessException("Booking appointment failed, no ID obtained.");
                }
            }
            return appointment;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error adding appointment: " + appointment, e);
            if (e.getErrorCode() == 1062) {
                throw new DataAccessException("Slot Clash: Doctor already has an appointment booked for "
                        + appointment.getAppointmentDate() + " at " + appointment.getAppointmentTime() + ".", e);
            }
            throw new DataAccessException("Failed to book appointment: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Appointment appointment) {
        String sql = "UPDATE appointments SET patient_id = ?, doctor_id = ?, appointment_date = ?, "
                   + "appointment_time = ?, status = ? WHERE appointment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointment.getPatientId());
            ps.setInt(2, appointment.getDoctorId());
            ps.setDate(3, Date.valueOf(appointment.getAppointmentDate()));
            ps.setTime(4, Time.valueOf(appointment.getAppointmentTime()));
            ps.setString(5, appointment.getStatus());
            ps.setInt(6, appointment.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error updating appointment id: " + appointment.getId(), e);
            if (e.getErrorCode() == 1062) {
                throw new DataAccessException("Slot Clash: Another appointment is already booked for this doctor at that slot.", e);
            }
            throw new DataAccessException("Failed to update appointment: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM appointments WHERE appointment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error deleting appointment id: " + id, e);
            throw new DataAccessException("Failed to delete appointment: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Appointment> getById(int id) {
        String sql = BASE_SELECT + "WHERE a.appointment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToAppointment(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error fetching appointment id: " + id, e);
            throw new DataAccessException("Failed to retrieve appointment: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Appointment> getAll() {
        String sql = BASE_SELECT + "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        List<Appointment> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToAppointment(rs));
            }
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error fetching all appointments", e);
            throw new DataAccessException("Failed to retrieve appointments list: " + e.getMessage(), e);
        }
    }

    public boolean updateStatus(int appointmentId, String newStatus) {
        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, appointmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error updating appointment status: " + appointmentId, e);
            throw new DataAccessException("Failed to update appointment status: " + e.getMessage(), e);
        }
    }

    public Optional<Appointment> findByDoctorAndSlot(int doctorId, LocalDate date, LocalTime time) {
        String sql = BASE_SELECT + "WHERE a.doctor_id = ? AND a.appointment_date = ? AND a.appointment_time = ? "
                   + "AND a.status != 'Cancelled'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            ps.setDate(2, Date.valueOf(date));
            ps.setTime(3, Time.valueOf(time));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToAppointment(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error checking slot clash", e);
            throw new DataAccessException("Failed to verify slot availability: " + e.getMessage(), e);
        }
    }

    public Set<LocalTime> getBookedTimesForDoctorAndDate(int doctorId, LocalDate date) {
        String sql = "SELECT appointment_time FROM appointments WHERE doctor_id = ? "
                   + "AND appointment_date = ? AND status != 'Cancelled'";
        Set<LocalTime> booked = new HashSet<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            ps.setDate(2, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Time t = rs.getTime("appointment_time");
                    if (t != null) {
                        booked.add(t.toLocalTime());
                    }
                }
            }
            return booked;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error fetching booked times", e);
            throw new DataAccessException("Failed to retrieve booked slots: " + e.getMessage(), e);
        }
    }

    public List<Appointment> filterAppointments(Integer patientId, Integer doctorId, LocalDate date, String status) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (patientId != null && patientId > 0) {
            sql.append("AND a.patient_id = ? ");
            params.add(patientId);
        }
        if (doctorId != null && doctorId > 0) {
            sql.append("AND a.doctor_id = ? ");
            params.add(doctorId);
        }
        if (date != null) {
            sql.append("AND a.appointment_date = ? ");
            params.add(Date.valueOf(date));
        }
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("All")) {
            sql.append("AND a.status = ? ");
            params.add(status.trim());
        }

        sql.append("ORDER BY a.appointment_date DESC, a.appointment_time DESC");

        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToAppointment(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error filtering appointments", e);
            throw new DataAccessException("Failed to query appointments: " + e.getMessage(), e);
        }
    }

    public int countTodayAppointments() {
        String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_date = CURRENT_DATE AND status = 'Scheduled'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error counting today's appointments", e);
            return 0;
        }
    }

    private Appointment mapRowToAppointment(ResultSet rs) throws SQLException {
        Date d = rs.getDate("appointment_date");
        Time t = rs.getTime("appointment_time");
        Appointment appt = new Appointment(
                rs.getInt("appointment_id"),
                rs.getInt("patient_id"),
                rs.getInt("doctor_id"),
                d != null ? d.toLocalDate() : null,
                t != null ? t.toLocalTime() : null,
                rs.getString("status")
        );
        java.sql.Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            appt.setCreatedAt(ts.toLocalDateTime());
        }
        appt.setPatientName(rs.getString("patient_name"));
        appt.setDoctorName(rs.getString("doctor_name"));
        appt.setDoctorSpecialization(rs.getString("doctor_specialization"));
        return appt;
    }
}
