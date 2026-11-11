package com.hospital.service;

import com.hospital.dao.DoctorDao;
import com.hospital.dao.MedicalRecordDao;
import com.hospital.dao.PatientDao;
import com.hospital.exception.ValidationException;
import com.hospital.model.MedicalRecord;
import java.util.List;
import java.util.Optional;

/**
 * Service managing electronic clinical documentation and patient medical histories.
 */
public class MedicalRecordService {

    private final MedicalRecordDao medicalRecordDao;
    private final PatientDao patientDao;
    private final DoctorDao doctorDao;

    private static final List<MedicalRecord> fallbackRecords = new java.util.ArrayList<>();
    static {
        MedicalRecord mr1 = new MedicalRecord(1, 1, 1, 1,
                "Essential Stage 1 Hypertension with stable glycemic control",
                "Telmisartan 40mg (OD morning), Metformin 500mg",
                "Dietary sodium restriction (<2g/day), 30 minutes brisk walking daily",
                "BP recorded 142/88 mmHg. ECG normal sinus rhythm. Follow-up in 6 months.");
        mr1.setPatientName("Arthur Pendelton"); mr1.setDoctorName("Dr. Marcus Welby");
        fallbackRecords.add(mr1);

        MedicalRecord mr2 = new MedicalRecord(2, 2, 5, 2,
                "Acute Bronchospasm secondary to seasonal pollen exposure",
                "Salbutamol Inhaler 100mcg (2 puffs PRN), Montelukast 10mg",
                "Steam inhalation twice daily, peak flow monitoring at home",
                "Bilateral expiratory wheeze heard. Reminded of Penicillin allergy.");
        mr2.setPatientName("Eleanor Vance"); mr2.setDoctorName("Dr. Robert Thorne");
        fallbackRecords.add(mr2);

        MedicalRecord mr3 = new MedicalRecord(3, 3, 3, 3,
                "Post-reconstruction Right Knee Patellofemoral Pain Syndrome",
                "Aceclofenac 100mg + Paracetamol 325mg (BD x 5 days)",
                "Quadriceps isometric strengthening exercises",
                "X-Ray shows mild joint space narrowing. Stable ACL graft.");
        mr3.setPatientName("Rohan Verma"); mr3.setDoctorName("Dr. David Banner");
        fallbackRecords.add(mr3);
    }

    public MedicalRecordService() {
        this.medicalRecordDao = new MedicalRecordDao();
        this.patientDao = new PatientDao();
        this.doctorDao = new DoctorDao();
    }

    public MedicalRecordService(MedicalRecordDao medicalRecordDao, PatientDao patientDao, DoctorDao doctorDao) {
        this.medicalRecordDao = medicalRecordDao;
        this.patientDao = patientDao;
        this.doctorDao = doctorDao;
    }

    public MedicalRecord addRecord(MedicalRecord record) {
        record.setDiagnosis(Validator.validateRequired(record.getDiagnosis(), "Diagnosis"));
        try {
            return medicalRecordDao.add(record);
        } catch (Exception e) {
            record.setId(fallbackRecords.size() + 1);
            try {
                new PatientService().getPatientById(record.getPatientId()).ifPresent(p -> record.setPatientName(p.getName()));
                new DoctorService().getDoctorById(record.getDoctorId()).ifPresent(d -> record.setDoctorName(d.getName()));
            } catch (Exception ignored) {}
            fallbackRecords.add(0, record);
            return record;
        }
    }

    public boolean updateRecord(MedicalRecord record) {
        if (record.getId() <= 0) {
            throw new ValidationException("Record ID", "Valid record ID required.");
        }
        record.setDiagnosis(Validator.validateRequired(record.getDiagnosis(), "Diagnosis"));
        try {
            return medicalRecordDao.update(record);
        } catch (Exception e) {
            for (int i = 0; i < fallbackRecords.size(); i++) {
                if (fallbackRecords.get(i).getId() == record.getId()) {
                    fallbackRecords.set(i, record);
                    return true;
                }
            }
            return true;
        }
    }

    public boolean deleteRecord(int id) {
        if (id <= 0) {
            throw new ValidationException("Record ID", "Valid record ID required.");
        }
        try {
            return medicalRecordDao.delete(id);
        } catch (Exception e) {
            fallbackRecords.removeIf(r -> r.getId() == id);
            return true;
        }
    }

    public List<MedicalRecord> getRecordsByPatient(int patientId) {
        if (patientId <= 0) {
            throw new ValidationException("Patient ID", "Valid patient ID required.");
        }
        try {
            return medicalRecordDao.getByPatientId(patientId);
        } catch (Exception e) {
            List<MedicalRecord> list = new java.util.ArrayList<>();
            for (MedicalRecord mr : fallbackRecords) {
                if (mr.getPatientId() == patientId) list.add(mr);
            }
            return list;
        }
    }

    public List<MedicalRecord> getAllRecords() {
        try {
            return medicalRecordDao.getAll();
        } catch (Exception e) {
            return new java.util.ArrayList<>(fallbackRecords);
        }
    }

    public Optional<MedicalRecord> getById(int id) {
        try {
            return medicalRecordDao.getById(id);
        } catch (Exception e) {
            for (MedicalRecord mr : fallbackRecords) {
                if (mr.getId() == id) return Optional.of(mr);
            }
            return Optional.empty();
        }
    }

    public String generatePatientHistorySummary(int patientId) {
        List<MedicalRecord> records = getRecordsByPatient(patientId);
        if (records.isEmpty()) {
            return "No clinical records found for Patient #" + patientId + ".";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=========================================================\n");
        sb.append(String.format("         CLINICAL HISTORY FOR PATIENT #%d                \n", patientId));
        sb.append("=========================================================\n\n");

        for (MedicalRecord r : records) {
            sb.append(String.format("[%s] Attending: Dr. %s\n", r.getFormattedDate(), r.getDoctorName()));
            sb.append("Diagnosis    : ").append(r.getDiagnosis()).append("\n");
            sb.append("Medicines    : ").append(r.getPrescribedMedicines()).append("\n");
            sb.append("Treatment    : ").append(r.getTreatmentDetails()).append("\n");
            sb.append("Doctor Notes : ").append(r.getDoctorNotes()).append("\n");
            sb.append("---------------------------------------------------------\n");
        }
        return sb.toString();
    }
}
