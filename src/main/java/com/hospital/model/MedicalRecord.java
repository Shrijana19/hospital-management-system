package com.hospital.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model representing a confidential clinical medical record.
 * Connects diagnosis, prescriptions, treatments, and clinician observations.
 */
public class MedicalRecord {

    private int id;
    private int patientId;
    private int doctorId;
    private Integer appointmentId; // nullable
    private String diagnosis;
    private String prescribedMedicines;
    private String treatmentDetails;
    private String doctorNotes;
    private LocalDateTime recordDate;

    // View-helper fields
    private String patientName;
    private String doctorName;

    public MedicalRecord() {
        this.recordDate = LocalDateTime.now();
    }

    public MedicalRecord(int id, int patientId, int doctorId, Integer appointmentId,
                         String diagnosis, String prescribedMedicines,
                         String treatmentDetails, String doctorNotes) {
        setId(id);
        setPatientId(patientId);
        setDoctorId(doctorId);
        setAppointmentId(appointmentId);
        setDiagnosis(diagnosis);
        setPrescribedMedicines(prescribedMedicines);
        setTreatmentDetails(treatmentDetails);
        setDoctorNotes(doctorNotes);
        this.recordDate = LocalDateTime.now();
    }

    public MedicalRecord(int patientId, int doctorId, Integer appointmentId,
                         String diagnosis, String prescribedMedicines,
                         String treatmentDetails, String doctorNotes) {
        this(0, patientId, doctorId, appointmentId, diagnosis, prescribedMedicines, treatmentDetails, doctorNotes);
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("Record ID cannot be negative");
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

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        if (diagnosis == null || diagnosis.trim().isEmpty()) {
            throw new IllegalArgumentException("Diagnosis cannot be null or empty");
        }
        this.diagnosis = diagnosis.trim();
    }

    public String getPrescribedMedicines() {
        return prescribedMedicines;
    }

    public void setPrescribedMedicines(String prescribedMedicines) {
        this.prescribedMedicines = (prescribedMedicines != null) ? prescribedMedicines.trim() : "";
    }

    public String getTreatmentDetails() {
        return treatmentDetails;
    }

    public void setTreatmentDetails(String treatmentDetails) {
        this.treatmentDetails = (treatmentDetails != null) ? treatmentDetails.trim() : "";
    }

    public String getDoctorNotes() {
        return doctorNotes;
    }

    public void setDoctorNotes(String doctorNotes) {
        this.doctorNotes = (doctorNotes != null) ? doctorNotes.trim() : "";
    }

    public LocalDateTime getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDateTime recordDate) {
        this.recordDate = (recordDate != null) ? recordDate : LocalDateTime.now();
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

    public String getFormattedDate() {
        return recordDate != null ? recordDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A";
    }

    @Override
    public String toString() {
        return String.format("MedicalRecord #%d [Patient #%d, Doctor #%d] on %s: %s",
                id, patientId, doctorId, getFormattedDate(), diagnosis);
    }
}
