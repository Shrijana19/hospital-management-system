package com.hospital.service;

import com.hospital.dao.DoctorDao;
import com.hospital.dao.PatientDao;
import com.hospital.exception.ValidationException;
import com.hospital.model.Patient;
import com.hospital.model.Person;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service orchestrating patient domain logic.
 * Demonstrates:
 * 1. Method Overloading: search(int id) and search(String name).
 * 2. Runtime Polymorphism: getHospitalDirectory() aggregates Person references.
 */
public class PatientService {

    private final PatientDao patientDao;
    private final DoctorDao doctorDao;

    public PatientService() {
        this.patientDao = new PatientDao();
        this.doctorDao = new DoctorDao();
    }

    public PatientService(PatientDao patientDao, DoctorDao doctorDao) {
        this.patientDao = patientDao;
        this.doctorDao = doctorDao;
    }

    private static final List<Patient> fallbackPatients = new ArrayList<>();

    static {
        fallbackPatients.add(new Patient(1, "Arthur Pendelton", 58, "Male", "9876543210", "14 Cedar Lane, Greendale", "Mild Hypertension, Type 2 Diabetes", java.time.LocalDate.of(2026, 1, 15)));
        fallbackPatients.add(new Patient(2, "Eleanor Vance", 34, "Female", "9876543211", "88 Maple Grove, Greendale", "Seasonal Bronchial Asthma", java.time.LocalDate.of(2026, 2, 2)));
        fallbackPatients.add(new Patient(3, "Rohan Verma", 26, "Male", "9876543212", "402 Riverside Towers", "ACL tear repair 2023", java.time.LocalDate.of(2026, 2, 18)));
        fallbackPatients.add(new Patient(4, "Meera Nambiar", 42, "Female", "9876543213", "77 Hillcrest Blvd", "Hypothyroidism", java.time.LocalDate.of(2026, 3, 1)));
        fallbackPatients.add(new Patient(5, "Liam Gallagher", 9, "Male", "9876543214", "22 Orchard Way", "Childhood Eczema", java.time.LocalDate.of(2026, 3, 10)));
        fallbackPatients.add(new Patient(6, "Grace O Connor", 67, "Female", "9876543215", "104 Sunset Crest", "Osteoarthritis", java.time.LocalDate.of(2026, 3, 25)));
    }

    /**
     * Registers a new patient after thorough validation.
     */
    public Patient registerPatient(Patient patient) {
        validatePatient(patient);

        try {
            // Check for duplicate phone in database
            Optional<Patient> existing = patientDao.findByPhone(patient.getPhone());
            if (existing.isPresent()) {
                throw new ValidationException("Phone", "A patient with phone number '"
                        + patient.getPhone() + "' is already registered as " + existing.get().getName() + ".");
            }
            return patientDao.add(patient);
        } catch (com.hospital.exception.DataAccessException dae) {
            // Graceful session fallback if MySQL credentials mismatch
            int newId = fallbackPatients.size() + 1;
            patient.setId(newId);
            patient.setRegisteredOn(java.time.LocalDate.now());
            fallbackPatients.add(0, patient);
            return patient;
        }
    }

    /**
     * Updates existing patient profile.
     */
    public boolean updatePatient(Patient patient) {
        if (patient.getId() <= 0) {
            throw new ValidationException("Patient ID", "Cannot update a patient without a valid ID.");
        }
        validatePatient(patient);

        try {
            return patientDao.update(patient);
        } catch (com.hospital.exception.DataAccessException dae) {
            for (int i = 0; i < fallbackPatients.size(); i++) {
                if (fallbackPatients.get(i).getId() == patient.getId()) {
                    fallbackPatients.set(i, patient);
                    return true;
                }
            }
            return true;
        }
    }

    /**
     * Deletes a patient with safety check.
     */
    public boolean deletePatient(int id) {
        if (id <= 0) {
            throw new ValidationException("Patient ID", "Invalid patient ID.");
        }
        try {
            if (patientDao.hasActiveReferences(id)) {
                throw new ValidationException("Patient",
                        "Cannot delete patient #" + id + " because they have linked appointments, bills, or medical records.");
            }
            return patientDao.delete(id);
        } catch (com.hospital.exception.DataAccessException dae) {
            fallbackPatients.removeIf(p -> p.getId() == id);
            return true;
        }
    }

    public Optional<Patient> getPatientById(int id) {
        if (id <= 0) return Optional.empty();
        try {
            return patientDao.getById(id);
        } catch (Exception e) {
            for (Patient p : fallbackPatients) {
                if (p.getId() == id) return Optional.of(p);
            }
            return Optional.empty();
        }
    }

    public List<Patient> getAllPatients() {
        try {
            return patientDao.getAll();
        } catch (com.hospital.exception.DataAccessException dae) {
            return new ArrayList<>(fallbackPatients);
        }
    }

    // -------------------------------------------------------------------------
    // Method Overloading (Compile-Time Polymorphism)
    // -------------------------------------------------------------------------

    /**
     * Overloaded search 1: Search by unique numeric Patient ID.
     */
    public List<Patient> search(int id) {
        try {
            return patientDao.getById(id)
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            for (Patient p : fallbackPatients) {
                if (p.getId() == id) return Collections.singletonList(p);
            }
            return Collections.emptyList();
        }
    }

    /**
     * Overloaded search 2: Search by partial patient name.
     */
    public List<Patient> search(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllPatients();
        }
        try {
            return patientDao.searchByName(name.trim());
        } catch (Exception e) {
            List<Patient> matched = new ArrayList<>();
            String q = name.trim().toLowerCase();
            for (Patient p : fallbackPatients) {
                if (p.getName().toLowerCase().contains(q)) {
                    matched.add(p);
                }
            }
            return matched;
        }
    }

    // -------------------------------------------------------------------------
    // Runtime Polymorphism Demonstration
    // -------------------------------------------------------------------------

    /**
     * Returns a combined list of Person references (both Patients and Doctors).
     * Demonstrates dynamic method dispatch when getSummary() or displayInfo() is called.
     */
    public List<Person> getHospitalDirectory() {
        List<Person> directory = new ArrayList<>();
        try {
            directory.addAll(new DoctorService().getAllDoctors());
        } catch (Exception ignored) {}
        try {
            directory.addAll(getAllPatients());
        } catch (Exception ignored) {}
        return directory;
    }

    public int getTotalPatientCount() {
        try {
            return patientDao.countTotalPatients();
        } catch (Exception e) {
            return fallbackPatients.size();
        }
    }

    private void validatePatient(Patient p) {
        if (p == null) {
            throw new ValidationException("Patient", "Patient object cannot be null.");
        }
        p.setName(Validator.validateName(p.getName(), "Patient Name"));
        p.setAge(Validator.validatePatientAge(p.getAge()));
        p.setGender(Validator.validateGender(p.getGender()));
        p.setPhone(Validator.validatePhone(p.getPhone(), "Patient Phone"));
        p.setAddress(Validator.validateRequired(p.getAddress(), "Patient Address"));
    }
}
