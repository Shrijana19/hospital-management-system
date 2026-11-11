-- ============================================================================
-- HOSPITAL MANAGEMENT SYSTEM - SAMPLE DATA
-- Fictional dataset for testing and demonstration.
-- WARNING: All names, contact numbers, and clinical details are entirely fictional.
-- ============================================================================

USE hospital_management;

-- ----------------------------------------------------------------------------
-- 1. SEED USERS
-- Passwords hashed with PBKDF2WithHmacSHA256 (65536 iterations, 16-byte salt):
-- admin / admin123
-- receptionist / recep123
-- ----------------------------------------------------------------------------
INSERT INTO users (username, password_hash, salt, role, full_name) VALUES
('admin', '3ccb86d31ae0846fdb0af21fc91815aa3e8335b888ef0c568259af5c5b13ab1f', 'f2fcb0e7691bc49d60d68c4296b3dfee', 'ADMIN', 'Dr. Alistair Vance (System Admin)'),
('receptionist', '252879c8a9f65a25cf980cbecfc93a79355b65fffa4c502d13a175ceb85823c2', 'a3b76fb48bd7717236b7551ba8b9ae68', 'RECEPTIONIST', 'Sarah Jenkins (Chief Receptionist)');

-- ----------------------------------------------------------------------------
-- 2. SEED PATIENTS
-- ----------------------------------------------------------------------------
INSERT INTO patients (name, age, gender, phone, address, medical_history, registered_on) VALUES
('Arthur Pendelton', 58, 'Male', '9876543210', '14 Cedar Lane, Greendale', 'Mild Hypertension, Type 2 Diabetes diagnosed 2021', '2026-01-15'),
('Eleanor Vance', 34, 'Female', '9876543211', '88 Maple Grove, Greendale', 'Seasonal Bronchial Asthma, Penicillin allergy', '2026-02-02'),
('Rohan Verma', 26, 'Male', '9876543212', '402 Riverside Towers, Northbrook', 'No prior chronic conditions. ACL tear repair 2023', '2026-02-18'),
('Meera Nambiar', 42, 'Female', '9876543213', '77 Hillcrest Blvd, Sunbury', 'Hypothyroidism, treated with Levothyroxine', '2026-03-01'),
('Liam Gallagher', 9, 'Male', '9876543214', '22 Orchard Way, Greendale', 'Childhood Eczema, routine immunization up to date', '2026-03-10'),
('Grace O Connor', 67, 'Female', '9876543215', '104 Sunset Crest, Lakeview', 'Osteoarthritis (both knees), Cataract surgery 2024', '2026-03-25');

-- ----------------------------------------------------------------------------
-- 3. SEED DOCTORS
-- ----------------------------------------------------------------------------
INSERT INTO doctors (name, age, gender, phone, address, specialization, consultation_fee, available) VALUES
('Dr. Marcus Welby', 52, 'Male', '9123456780', '12 Medical Enclave, Greendale', 'Cardiology', 750.00, 1),
('Dr. Serena Sterling', 44, 'Female', '9123456781', '45 Park Avenue, Greendale', 'Dermatology', 600.00, 1),
('Dr. David Banner', 49, 'Male', '9123456782', '89 University Road, Sunbury', 'Orthopedics', 700.00, 1),
('Dr. Anita Desai', 39, 'Female', '9123456783', '31 Green View, Greendale', 'Pediatrics', 500.00, 1),
('Dr. Robert Thorne', 60, 'Male', '9123456784', '5 Lakeview Drive, Northbrook', 'General Medicine', 450.00, 1),
('Dr. Evelyn Reed', 38, 'Female', '9123456785', '19 Crescent Moon, Greendale', 'Neurology', 800.00, 0);

-- ----------------------------------------------------------------------------
-- 4. SEED APPOINTMENTS
-- Varied statuses: Completed, Scheduled, Cancelled
-- Note: UNIQUE (doctor_id, appointment_date, appointment_time) is respected
-- ----------------------------------------------------------------------------
INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status) VALUES
(1, 1, '2026-04-10', '09:00:00', 'Completed'),
(2, 5, '2026-04-10', '10:00:00', 'Completed'),
(3, 3, '2026-04-11', '11:00:00', 'Completed'),
(4, 2, '2026-04-12', '14:30:00', 'Completed'),
(5, 4, '2026-04-14', '15:00:00', 'Cancelled'),
(1, 1, '2026-11-20', '09:30:00', 'Scheduled'),
(2, 2, '2026-11-20', '10:30:00', 'Scheduled'),
(3, 3, '2026-11-21', '11:30:00', 'Scheduled'),
(6, 3, '2026-11-22', '14:00:00', 'Scheduled');

-- ----------------------------------------------------------------------------
-- 5. SEED MEDICAL RECORDS
-- Linked to completed consultations
-- ----------------------------------------------------------------------------
INSERT INTO medical_records (patient_id, doctor_id, appointment_id, diagnosis, prescribed_medicines, treatment_details, doctor_notes) VALUES
(1, 1, 1, 'Essential Stage 1 Hypertension with stable glycemic control',
 'Telmisartan 40mg (OD morning), Metformin 500mg (BD after meals)',
 'Dietary sodium restriction (<2g/day), 30 minutes brisk walking daily',
 'BP recorded 142/88 mmHg. ECG normal sinus rhythm. Follow-up scheduled in 6 months.'),

(2, 5, 2, 'Acute Bronchospasm secondary to seasonal pollen exposure',
 'Salbutamol Inhaler 100mcg (2 puffs PRN), Montelukast 10mg (OD night for 14 days)',
 'Steam inhalation twice daily, peak flow monitoring at home',
 'Bilateral expiratory wheeze heard. Patient reminded of Penicillin contraindication. Condition improved after nebulization in OPD.'),

(3, 3, 3, 'Post-reconstruction Right Knee Patellofemoral Pain Syndrome',
 'Aceclofenac 100mg + Paracetamol 325mg (BD x 5 days), Glucosamine sulphate',
 'Quadriceps isometric strengthening exercises, avoid deep squatting and running on hard surfaces',
 'X-Ray right knee shows mild joint space narrowing. Stable ACL graft. Referred to physical therapy.'),

(4, 2, 4, 'Contact Dermatitis (nickel/cosmetics suspected)',
 'Hydrocortisone cream 1% (topical BD x 7 days), Cetirizine 10mg (OD night)',
 'Patch testing advised if rash recurs; avoid scented face washes and artificial jewelry',
 'Mild erythematous maculopapular rash across cheeks and neck. No signs of secondary infection.');

-- ----------------------------------------------------------------------------
-- 6. SEED BILLS
-- Auto-calculated total = consultation_fee + other_charges
-- ----------------------------------------------------------------------------
INSERT INTO bills (patient_id, appointment_id, consultation_fee, other_charges, total_amount, payment_status, bill_date) VALUES
(1, 1, 750.00, 350.00, 1100.00, 'Paid', '2026-04-10 09:45:00'),
(2, 2, 450.00, 250.00, 700.00, 'Paid', '2026-04-10 10:40:00'),
(3, 3, 700.00, 650.00, 1350.00, 'Pending', '2026-04-11 11:50:00'),
(4, 4, 600.00, 150.00, 750.00, 'Paid', '2026-04-12 15:15:00'),
(6, NULL, 0.00, 1200.00, 1200.00, 'Pending', '2026-04-15 16:00:00');
