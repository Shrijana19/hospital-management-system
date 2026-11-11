package com.hospital.dao;

import com.hospital.database.DatabaseConnection;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Appointment;
import com.hospital.model.Bill;
import java.math.BigDecimal;
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
 * Data Access Object for Bill persistence.
 * Implements CrudDao<Bill>.
 * Supports ACID transactions for atomic billing and appointment completion.
 */
public class BillDao implements CrudDao<Bill> {

    private static final Logger LOGGER = Logger.getLogger(BillDao.class.getName());

    private static final String BASE_SELECT =
            "SELECT b.bill_id, b.patient_id, b.appointment_id, b.consultation_fee, "
            + "b.other_charges, b.total_amount, b.payment_status, b.bill_date, "
            + "p.name AS patient_name "
            + "FROM bills b "
            + "INNER JOIN patients p ON b.patient_id = p.patient_id ";

    @Override
    public Bill add(Bill bill) {
        String sql = "INSERT INTO bills (patient_id, appointment_id, consultation_fee, "
                   + "other_charges, total_amount, payment_status, bill_date) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bill.recomputeTotal();
            ps.setInt(1, bill.getPatientId());
            if (bill.getAppointmentId() != null) {
                ps.setInt(2, bill.getAppointmentId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setBigDecimal(3, bill.getConsultationFee());
            ps.setBigDecimal(4, bill.getOtherCharges());
            ps.setBigDecimal(5, bill.getTotalAmount());
            ps.setString(6, bill.getPaymentStatus());
            ps.setTimestamp(7, Timestamp.valueOf(bill.getBillDate()));

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Creating bill failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    bill.setId(generatedKeys.getInt(1));
                } else {
                    throw new DataAccessException("Creating bill failed, no ID obtained.");
                }
            }
            return bill;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error creating bill", e);
            throw new DataAccessException("Failed to generate bill: " + e.getMessage(), e);
        }
    }

    /**
     * Executes a transactional workflow:
     * 1. Inserts the Bill record.
     * 2. If an appointment is linked, transitions the appointment status to 'Completed'.
     * Guarantees all-or-nothing atomicity with explicit commit and rollback.
     */
    public Bill createBillWithAppointmentTransaction(Bill bill, Integer appointmentId) {
        String insertBillSql = "INSERT INTO bills (patient_id, appointment_id, consultation_fee, "
                             + "other_charges, total_amount, payment_status, bill_date) "
                             + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        String updateApptSql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            bill.recomputeTotal();
            int generatedBillId;
            try (PreparedStatement psBill = conn.prepareStatement(insertBillSql, Statement.RETURN_GENERATED_KEYS)) {
                psBill.setInt(1, bill.getPatientId());
                if (appointmentId != null && appointmentId > 0) {
                    psBill.setInt(2, appointmentId);
                    bill.setAppointmentId(appointmentId);
                } else {
                    psBill.setNull(2, Types.INTEGER);
                }
                psBill.setBigDecimal(3, bill.getConsultationFee());
                psBill.setBigDecimal(4, bill.getOtherCharges());
                psBill.setBigDecimal(5, bill.getTotalAmount());
                psBill.setString(6, bill.getPaymentStatus());
                psBill.setTimestamp(7, Timestamp.valueOf(bill.getBillDate()));

                psBill.executeUpdate();
                try (ResultSet rs = psBill.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedBillId = rs.getInt(1);
                        bill.setId(generatedBillId);
                    } else {
                        throw new SQLException("Failed to retrieve generated bill ID");
                    }
                }
            }

            // If appointment is present, mark it as Completed
            if (appointmentId != null && appointmentId > 0) {
                try (PreparedStatement psAppt = conn.prepareStatement(updateApptSql)) {
                    psAppt.setString(1, Appointment.STATUS_COMPLETED);
                    psAppt.setInt(2, appointmentId);
                    psAppt.executeUpdate();
                }
            }

            conn.commit(); // Commit transaction
            return bill;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    LOGGER.warning("Rolling back billing transaction due to error: " + e.getMessage());
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Rollback failed", rollbackEx);
                }
            }
            throw new DataAccessException("Billing transaction failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection", e);
                }
            }
        }
    }

    @Override
    public boolean update(Bill bill) {
        String sql = "UPDATE bills SET patient_id = ?, appointment_id = ?, consultation_fee = ?, "
                   + "other_charges = ?, total_amount = ?, payment_status = ? WHERE bill_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bill.recomputeTotal();
            ps.setInt(1, bill.getPatientId());
            if (bill.getAppointmentId() != null) {
                ps.setInt(2, bill.getAppointmentId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setBigDecimal(3, bill.getConsultationFee());
            ps.setBigDecimal(4, bill.getOtherCharges());
            ps.setBigDecimal(5, bill.getTotalAmount());
            ps.setString(6, bill.getPaymentStatus());
            ps.setInt(7, bill.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error updating bill id: " + bill.getId(), e);
            throw new DataAccessException("Failed to update bill: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM bills WHERE bill_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error deleting bill id: " + id, e);
            throw new DataAccessException("Failed to delete bill: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Bill> getById(int id) {
        String sql = BASE_SELECT + "WHERE b.bill_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToBill(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error fetching bill id: " + id, e);
            throw new DataAccessException("Failed to retrieve bill: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Bill> getAll() {
        String sql = BASE_SELECT + "ORDER BY b.bill_date DESC";
        List<Bill> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToBill(rs));
            }
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error fetching all bills", e);
            throw new DataAccessException("Failed to retrieve bills list: " + e.getMessage(), e);
        }
    }

    public List<Bill> getByPatientId(int patientId) {
        String sql = BASE_SELECT + "WHERE b.patient_id = ? ORDER BY b.bill_date DESC";
        List<Bill> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToBill(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error fetching bills for patient: " + patientId, e);
            throw new DataAccessException("Failed to retrieve patient bills: " + e.getMessage(), e);
        }
    }

    public boolean updatePaymentStatus(int billId, String status) {
        String sql = "UPDATE bills SET payment_status = ? WHERE bill_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, billId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error updating bill payment status: " + billId, e);
            throw new DataAccessException("Failed to update payment status: " + e.getMessage(), e);
        }
    }

    public int countPendingBills() {
        String sql = "SELECT COUNT(*) FROM bills WHERE payment_status = 'Pending'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error counting pending bills", e);
            return 0;
        }
    }

    public BigDecimal getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM bills WHERE payment_status = 'Paid'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
            return BigDecimal.ZERO;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error calculating total revenue", e);
            return BigDecimal.ZERO;
        }
    }

    private Bill mapRowToBill(ResultSet rs) throws SQLException {
        int apptId = rs.getInt("appointment_id");
        Integer appointmentId = rs.wasNull() ? null : apptId;

        Bill bill = new Bill(
                rs.getInt("bill_id"),
                rs.getInt("patient_id"),
                appointmentId,
                rs.getBigDecimal("consultation_fee"),
                rs.getBigDecimal("other_charges"),
                rs.getString("payment_status")
        );
        bill.setTotalAmount(rs.getBigDecimal("total_amount"));
        Timestamp ts = rs.getTimestamp("bill_date");
        if (ts != null) {
            bill.setBillDate(ts.toLocalDateTime());
        }
        bill.setPatientName(rs.getString("patient_name"));
        return bill;
    }
}
