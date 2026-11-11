package com.hospital.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Concrete class representing a medical doctor.
 * Demonstrates:
 * 1. Inheritance: Extends Person, adding clinical specialization and consultation fee.
 * 2. Encapsulation: Protects consultation fee from negative values, ensures 2 decimal places.
 * 3. Polymorphism: Overrides displayInfo() and getSummary() specifically for doctor profiles.
 */
public class Doctor extends Person {

    private String specialization;
    private BigDecimal consultationFee;
    private boolean available;

    public Doctor() {
        super();
        this.consultationFee = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.available = true;
    }

    public Doctor(int id, String name, int age, String gender, String phone, String address,
                  String specialization, BigDecimal consultationFee, boolean available) {
        super(id, name, age, gender, phone, address);
        setSpecialization(specialization);
        setConsultationFee(consultationFee);
        setAvailable(available);
    }

    public Doctor(String name, int age, String gender, String phone, String address,
                  String specialization, BigDecimal consultationFee) {
        this(0, name, age, gender, phone, address, specialization, consultationFee, true);
    }

    // -------------------------------------------------------------------------
    // Method Overriding (Runtime Polymorphism)
    // -------------------------------------------------------------------------

    @Override
    public String getSummary() {
        return String.format("[Doctor #%d] %s (%s) - Fee: $%.2f | Status: %s",
                getId(), getName(), getSpecialization(),
                consultationFee.doubleValue(), available ? "Available" : "On Leave");
    }

    @Override
    public String displayInfo() {
        return new StringBuilder()
                .append("========================================\n")
                .append("           DOCTOR PROFILE CARD          \n")
                .append("========================================\n")
                .append(String.format("Doctor ID      : %d\n", getId()))
                .append(String.format("Doctor Name    : %s\n", getName()))
                .append(String.format("Age & Gender   : %d years, %s\n", getAge(), getGender()))
                .append(String.format("Specialization : %s\n", specialization))
                .append(String.format("Consultation   : $%.2f\n", consultationFee.doubleValue()))
                .append(String.format("Availability   : %s\n", available ? "Active / In OPD" : "Not Available"))
                .append(String.format("Phone          : %s\n", getPhone()))
                .append(String.format("Clinic Address : %s\n", getAddress()))
                .append("========================================")
                .toString();
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        if (specialization == null || specialization.trim().isEmpty()) {
            throw new IllegalArgumentException("Specialization cannot be null or empty");
        }
        this.specialization = specialization.trim();
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        if (consultationFee == null) {
            throw new IllegalArgumentException("Consultation fee cannot be null");
        }
        if (consultationFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Consultation fee cannot be negative");
        }
        this.consultationFee = consultationFee.setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
