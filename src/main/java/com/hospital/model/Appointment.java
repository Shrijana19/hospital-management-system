package com.hospital.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Model representing a medical consultation appointment.
 * Encapsulates the scheduling state between a Patient and a Doctor.
 */
public class Appointment {

    public static final String STATUS_SCHEDULED = "Scheduled";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_CANCELLED = "Cancelled";

    private int id;
    private int patientId;
    private int doctorId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String status;
    private LocalDateTime createdAt;

    // View-helper fields for UI presentation
    private String patientName;
    private String doctorName;
    private String doctorSpecialization;

    public Appointment() {
        this.status = STATUS_SCHEDULED;
        this.createdAt = LocalDateTime.now();
    }

    public Appointment(int id, int patientId, int doctorId, LocalDate appointmentDate,
                       LocalTime appointmentTime, String status) {
        setId(id);
        setPatientId(patientId);
        setDoctorId(doctorId);
        setAppointmentDate(appointmentDate);
        setAppointmentTime(appointmentTime);
        setStatus(status);
        this.createdAt = LocalDateTime.now();
    }

    public Appointment(int patientId, int doctorId, LocalDate appointmentDate, LocalTime appointmentTime) {
        this(0, patientId, doctorId, appointmentDate, appointmentTime, STATUS_SCHEDULED);
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("Appointment ID cannot be negative");
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

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        if (doctorId <= 0) {
            throw new IllegalArgumentException("Valid doctor ID required");
        }
        this.doctorId = doctorId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        if (appointmentDate == null) {
            throw new IllegalArgumentException("Appointment date cannot be null");
        }
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        if (appointmentTime == null) {
            throw new IllegalArgumentException("Appointment time cannot be null");
        }
        this.appointmentTime = appointmentTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status == null || (!status.equalsIgnoreCase(STATUS_SCHEDULED)
                && !status.equalsIgnoreCase(STATUS_COMPLETED)
                && !status.equalsIgnoreCase(STATUS_CANCELLED))) {
            throw new IllegalArgumentException("Invalid status. Must be 'Scheduled', 'Completed', or 'Cancelled'");
        }
        // Normalize casing
        if (status.equalsIgnoreCase(STATUS_SCHEDULED)) this.status = STATUS_SCHEDULED;
        else if (status.equalsIgnoreCase(STATUS_COMPLETED)) this.status = STATUS_COMPLETED;
        else this.status = STATUS_CANCELLED;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorSpecialization() {
        return doctorSpecialization;
    }

    public void setDoctorSpecialization(String doctorSpecialization) {
        this.doctorSpecialization = doctorSpecialization;
    }

    public String getFormattedDateTime() {
        String d = appointmentDate != null ? appointmentDate.toString() : "";
        String t = appointmentTime != null ? appointmentTime.format(DateTimeFormatter.ofPattern("HH:mm")) : "";
        return d + " " + t;
    }

    @Override
    public String toString() {
        return String.format("Appointment #%d | %s with %s on %s [%s]",
                id,
                patientName != null ? patientName : "Patient #" + patientId,
                doctorName != null ? doctorName : "Doctor #" + doctorId,
                getFormattedDateTime(),
                status);
    }
}
