package com.hospital.model;

import java.time.LocalDate;

/**
 * Concrete class representing a patient.
 * Demonstrates:
 * 1. Inheritance: Extends the abstract Person class, inheriting id, name, age, gender, phone, address.
 * 2. Encapsulation: Adds private medicalHistory and registeredOn with safe accessors.
 * 3. Polymorphism: Overrides displayInfo() and getSummary() specifically for patient records.
 */
public class Patient extends Person {

    private String medicalHistory;
    private LocalDate registeredOn;

    public Patient() {
        super();
        this.registeredOn = LocalDate.now();
    }

    public Patient(int id, String name, int age, String gender, String phone, String address,
                   String medicalHistory, LocalDate registeredOn) {
        super(id, name, age, gender, phone, address);
        setMedicalHistory(medicalHistory);
        setRegisteredOn(registeredOn);
    }

    public Patient(String name, int age, String gender, String phone, String address,
                   String medicalHistory) {
        this(0, name, age, gender, phone, address, medicalHistory, LocalDate.now());
    }

    // -------------------------------------------------------------------------
    // Method Overriding (Runtime Polymorphism)
    // -------------------------------------------------------------------------

    @Override
    public String getSummary() {
        return String.format("[Patient #%d] %s (%d yrs, %s) - Ph: %s",
                getId(), getName(), getAge(), getGender(), getPhone());
    }

    @Override
    public String displayInfo() {
        return new StringBuilder()
                .append("========================================\n")
                .append("           PATIENT RECORD CARD          \n")
                .append("========================================\n")
                .append(String.format("Patient ID     : %d\n", getId()))
                .append(String.format("Full Name      : %s\n", getName()))
                .append(String.format("Age & Gender   : %d years, %s\n", getAge(), getGender()))
                .append(String.format("Contact Phone  : %s\n", getPhone()))
                .append(String.format("Address        : %s\n", getAddress()))
                .append(String.format("Registered On  : %s\n", registeredOn != null ? registeredOn.toString() : "N/A"))
                .append("Medical History: ")
                .append(medicalHistory != null && !medicalHistory.trim().isEmpty() ? medicalHistory : "None recorded")
                .append("\n========================================")
                .toString();
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = (medicalHistory != null) ? medicalHistory.trim() : "";
    }

    public LocalDate getRegisteredOn() {
        return registeredOn;
    }

    public void setRegisteredOn(LocalDate registeredOn) {
        this.registeredOn = (registeredOn != null) ? registeredOn : LocalDate.now();
    }
}
