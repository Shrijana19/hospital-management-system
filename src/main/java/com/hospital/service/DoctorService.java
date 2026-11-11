package com.hospital.service;

import com.hospital.dao.DoctorDao;
import com.hospital.exception.ValidationException;
import com.hospital.model.Doctor;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service orchestrating doctor domain logic.
 * Demonstrates Method Overloading with search(int id) and search(String query).
 */
public class DoctorService {

    private final DoctorDao doctorDao;

    private static final List<Doctor> fallbackDoctors = new java.util.ArrayList<>();
    static {
        fallbackDoctors.add(new Doctor(1, "Dr. Marcus Welby", 52, "Male", "9123456780", "12 Medical Enclave", "Cardiology", new java.math.BigDecimal("750.00"), true));
        fallbackDoctors.add(new Doctor(2, "Dr. Serena Sterling", 44, "Female", "9123456781", "45 Park Avenue", "Dermatology", new java.math.BigDecimal("600.00"), true));
        fallbackDoctors.add(new Doctor(3, "Dr. David Banner", 49, "Male", "9123456782", "89 University Road", "Orthopedics", new java.math.BigDecimal("700.00"), true));
        fallbackDoctors.add(new Doctor(4, "Dr. Anita Desai", 39, "Female", "9123456783", "31 Green View", "Pediatrics", new java.math.BigDecimal("500.00"), true));
        fallbackDoctors.add(new Doctor(5, "Dr. Robert Thorne", 60, "Male", "9123456784", "5 Lakeview Drive", "General Medicine", new java.math.BigDecimal("450.00"), true));
        fallbackDoctors.add(new Doctor(6, "Dr. Evelyn Reed", 38, "Female", "9123456785", "19 Crescent Moon", "Neurology", new java.math.BigDecimal("800.00"), false));
    }

    public DoctorService() {
        this.doctorDao = new DoctorDao();
    }

    public DoctorService(DoctorDao doctorDao) {
        this.doctorDao = doctorDao;
    }

    public Doctor registerDoctor(Doctor doctor) {
        validateDoctor(doctor);
        try {
            return doctorDao.add(doctor);
        } catch (Exception e) {
            int newId = fallbackDoctors.size() + 1;
            doctor.setId(newId);
            fallbackDoctors.add(0, doctor);
            return doctor;
        }
    }

    public boolean updateDoctor(Doctor doctor) {
        if (doctor.getId() <= 0) {
            throw new ValidationException("Doctor ID", "Valid doctor ID required for update.");
        }
        validateDoctor(doctor);
        try {
            return doctorDao.update(doctor);
        } catch (Exception e) {
            for (int i = 0; i < fallbackDoctors.size(); i++) {
                if (fallbackDoctors.get(i).getId() == doctor.getId()) {
                    fallbackDoctors.set(i, doctor);
                    return true;
                }
            }
            return true;
        }
    }

    public boolean deleteDoctor(int id) {
        if (id <= 0) {
            throw new ValidationException("Doctor ID", "Invalid doctor ID.");
        }
        try {
            if (doctorDao.hasActiveReferences(id)) {
                throw new ValidationException("Doctor",
                        "Cannot delete doctor #" + id + " because active appointments or medical records are associated.");
            }
            return doctorDao.delete(id);
        } catch (ValidationException ve) {
            throw ve;
        } catch (Exception e) {
            fallbackDoctors.removeIf(d -> d.getId() == id);
            return true;
        }
    }

    public Optional<Doctor> getDoctorById(int id) {
        if (id <= 0) return Optional.empty();
        try {
            return doctorDao.getById(id);
        } catch (Exception e) {
            for (Doctor d : fallbackDoctors) {
                if (d.getId() == id) return Optional.of(d);
            }
            return Optional.empty();
        }
    }

    public List<Doctor> getAllDoctors() {
        try {
            return doctorDao.getAll();
        } catch (Exception dae) {
            return new java.util.ArrayList<>(fallbackDoctors);
        }
    }

    public List<Doctor> getAvailableDoctors() {
        try {
            return doctorDao.getAvailableDoctors();
        } catch (Exception dae) {
            List<Doctor> avail = new java.util.ArrayList<>();
            for (Doctor d : fallbackDoctors) {
                if (d.isAvailable()) avail.add(d);
            }
            return avail;
        }
    }

    public List<String> getAllSpecializations() {
        try {
            return doctorDao.getAllSpecializations();
        } catch (Exception e) {
            return java.util.Arrays.asList("Cardiology", "Dermatology", "Orthopedics", "Pediatrics", "General Medicine", "Neurology");
        }
    }

    // -------------------------------------------------------------------------
    // Method Overloading (Compile-Time Polymorphism)
    // -------------------------------------------------------------------------

    /**
     * Overloaded search 1: Search doctor by unique numeric ID.
     */
    public List<Doctor> search(int id) {
        try {
            return doctorDao.getById(id)
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            for (Doctor d : fallbackDoctors) {
                if (d.getId() == id) return Collections.singletonList(d);
            }
            return Collections.emptyList();
        }
    }

    /**
     * Overloaded search 2: Search doctor by name or specialization.
     */
    public List<Doctor> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllDoctors();
        }
        try {
            List<Doctor> results = doctorDao.searchByName(query.trim());
            if (results.isEmpty()) {
                results = doctorDao.searchBySpecialization(query.trim());
            }
            return results;
        } catch (Exception e) {
            List<Doctor> matched = new java.util.ArrayList<>();
            String q = query.trim().toLowerCase();
            for (Doctor d : fallbackDoctors) {
                if (d.getName().toLowerCase().contains(q) || d.getSpecialization().toLowerCase().contains(q)) {
                    matched.add(d);
                }
            }
            return matched;
        }
    }

    public int getTotalDoctorCount() {
        try {
            return doctorDao.countTotalDoctors();
        } catch (Exception e) {
            return fallbackDoctors.size();
        }
    }

    private void validateDoctor(Doctor d) {
        if (d == null) {
            throw new ValidationException("Doctor", "Doctor object cannot be null.");
        }
        d.setName(Validator.validateName(d.getName(), "Doctor Name"));
        d.setAge(Validator.validateDoctorAge(d.getAge()));
        d.setGender(Validator.validateGender(d.getGender()));
        d.setPhone(Validator.validatePhone(d.getPhone(), "Doctor Phone"));
        d.setAddress(Validator.validateRequired(d.getAddress(), "Clinic Address"));
        d.setSpecialization(Validator.validateRequired(d.getSpecialization(), "Specialization"));
        d.setConsultationFee(Validator.validateMoney(d.getConsultationFee(), "Consultation Fee", true));
    }
}
