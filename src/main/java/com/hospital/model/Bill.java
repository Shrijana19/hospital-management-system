package com.hospital.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model representing a hospital patient bill.
 * Demonstrates:
 * 1. Abstraction: Implements the Billable interface.
 * 2. Encapsulation: Ensures financial calculations use BigDecimal with 2 decimal places.
 * 3. Polymorphism: calculateTotal() fulfills the Billable contract.
 */
public class Bill implements Billable {

    public static final String STATUS_PAID = "Paid";
    public static final String STATUS_PENDING = "Pending";

    private int id;
    private int patientId;
    private Integer appointmentId; // nullable
    private BigDecimal consultationFee;
    private BigDecimal otherCharges;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private LocalDateTime billDate;

    // View-helper fields for UI display
    private String patientName;

    public Bill() {
        this.consultationFee = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.otherCharges = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.totalAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.paymentStatus = STATUS_PENDING;
        this.billDate = LocalDateTime.now();
    }

    public Bill(int id, int patientId, Integer appointmentId, BigDecimal consultationFee,
                BigDecimal otherCharges, String paymentStatus) {
        setId(id);
        setPatientId(patientId);
        setAppointmentId(appointmentId);
        setConsultationFee(consultationFee);
        setOtherCharges(otherCharges);
        setPaymentStatus(paymentStatus);
        this.totalAmount = calculateTotal();
        this.billDate = LocalDateTime.now();
    }

    public Bill(int patientId, Integer appointmentId, BigDecimal consultationFee, BigDecimal otherCharges) {
        this(0, patientId, appointmentId, consultationFee, otherCharges, STATUS_PENDING);
    }

    // -------------------------------------------------------------------------
    // Implementation of Billable Interface (Abstraction & Polymorphism)
    // -------------------------------------------------------------------------

    @Override
    public BigDecimal calculateTotal() {
        BigDecimal fee = (consultationFee != null) ? consultationFee : BigDecimal.ZERO;
        BigDecimal others = (otherCharges != null) ? otherCharges : BigDecimal.ZERO;
        return fee.add(others).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Recomputes and updates the total amount internally.
     */
    public void recomputeTotal() {
        this.totalAmount = calculateTotal();
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("Bill ID cannot be negative");
        }
        this.id = id;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        if (patientId <= 0) {
            throw new IllegalArgumentException("Valid patient ID required");
        }
        this.patientId = patientId;
    }

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        if (appointmentId != null && appointmentId <= 0) {
            this.appointmentId = null;
        } else {
            this.appointmentId = appointmentId;
        }
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        if (consultationFee == null) {
            this.consultationFee = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else if (consultationFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Consultation fee cannot be negative");
        } else {
            this.consultationFee = consultationFee.setScale(2, RoundingMode.HALF_UP);
        }
        recomputeTotal();
    }

    public BigDecimal getOtherCharges() {
        return otherCharges;
    }

    public void setOtherCharges(BigDecimal otherCharges) {
        if (otherCharges == null) {
            this.otherCharges = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else if (otherCharges.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Other charges cannot be negative");
        } else {
            this.otherCharges = otherCharges.setScale(2, RoundingMode.HALF_UP);
        }
        recomputeTotal();
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        if (totalAmount == null) {
            this.totalAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total amount cannot be negative");
        } else {
            this.totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);
        }
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        if (paymentStatus == null || (!paymentStatus.equalsIgnoreCase(STATUS_PAID)
                && !paymentStatus.equalsIgnoreCase(STATUS_PENDING))) {
            throw new IllegalArgumentException("Payment status must be 'Paid' or 'Pending'");
        }
        if (paymentStatus.equalsIgnoreCase(STATUS_PAID)) {
            this.paymentStatus = STATUS_PAID;
        } else {
            this.paymentStatus = STATUS_PENDING;
        }
    }

    public LocalDateTime getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDateTime billDate) {
        this.billDate = (billDate != null) ? billDate : LocalDateTime.now();
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getFormattedDate() {
        return billDate != null ? billDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A";
    }

    @Override
    public String toString() {
        return String.format("Bill #%d | Patient: %s | Total: $%.2f | Status: %s",
                id, patientName != null ? patientName : "#" + patientId, totalAmount.doubleValue(), paymentStatus);
    }
}
