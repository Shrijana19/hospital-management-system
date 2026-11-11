# Comprehensive Test Plan (Manual & Automated)

This document provides the formal test plan for the **Hospital Management System**, containing test scenarios, input vectors, negative test cases, and test results.

---

## 1. Automated Test Suite Summary

The automated suite is executable through:
1. **JUnit 5 / Maven**: `mvn test`
2. **Standalone Test Runner** (Zero external dependencies): `test.bat` or `java -cp bin com.hospital.test.StandaloneTestRunner`

| Test Category | Tested Target | Coverage / Assertions | Result |
| :--- | :--- | :--- | :--- |
| **OOP Hierarchy** | `Person`, `Patient`, `Doctor` | Verifies `instanceof Person`, inheritance of demographic attributes. | **PASS** |
| **Encapsulation** | Model Setters | Tests defensive validation: negative age rejection, length checks, 10-digit phone. | **PASS** |
| **Polymorphism** | `displayInfo()`, `getSummary()` | Verifies runtime dynamic dispatch on `List<Person>` collection. | **PASS** |
| **Abstraction** | `Billable` interface | Verifies interface contract implementation on `Bill` with `calculateTotal()`. | **PASS** |
| **Method Overloading** | `BillingService.calculateTotal` | Tests single-argument and double-argument monetary additions. | **PASS** |
| **Monetary Precision** | `BigDecimal` math | Verifies 2-decimal scaling and rejection of negative consultation fees/charges. | **PASS** |
| **Input Validation** | `Validator` utility | Checks boundaries for patient age (0-125), doctor age (21-100), phone patterns. | **PASS** |
| **State Machine** | `Appointment.setStatus()` | Ensures only valid transitions (`Scheduled` -> `Completed` / `Cancelled`) are allowed. | **PASS** |
| **Date Verification** | `Validator.validateDate` | Confirms rejection of appointment dates scheduled in the past. | **PASS** |
| **Security & Crypto** | `PasswordHasher` | Checks 16-byte random salt generation and constant-time PBKDF2 hash verification. | **PASS** |

---

## 2. Manual Test Cases & Execution Matrix

### Module A: Authentication & Access Control

| Test ID | Scenario | Steps to Execute | Input Data | Expected Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **AUTH-01** | Successful Admin Login | 1. Open login window.<br>2. Click 'Admin' quick-fill.<br>3. Click 'Log In'. | User: `admin`<br>Pass: `admin123` | Login succeeds; Dashboard opens showing Admin role badge and User Management menu. | **PASS** |
| **AUTH-02** | Successful Receptionist Login | 1. Open login window.<br>2. Click 'Receptionist' quick-fill.<br>3. Click 'Log In'. | User: `receptionist`<br>Pass: `recep123` | Login succeeds; Dashboard opens; User Management menu is hidden (RBAC enforced). | **PASS** |
| **AUTH-03** | (Negative) Invalid Password | 1. Enter username `admin`.<br>2. Enter wrong password.<br>3. Click 'Log In'. | User: `admin`<br>Pass: `wrongpass` | UI displays red error message: "Invalid username or password". Fields cleared. | **PASS** |
| **AUTH-04** | (Negative) Empty Credentials | 1. Leave fields blank.<br>2. Click 'Log In'. | Empty | Inline warning: "Please enter both username and password." | **PASS** |
| **AUTH-05** | (Negative) MySQL Server Down | 1. Stop MySQL service.<br>2. Launch application. | N/A | Startup modal appears: "The application could not establish a connection to MySQL" with Retry button. | **PASS** |

---

### Module B: Patient Management

| Test ID | Scenario | Steps to Execute | Input Data | Expected Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **PAT-01** | Register Valid Patient | 1. Navigate to Patients.<br>2. Fill all required fields.<br>3. Click 'Save Patient'. | Name: "David Miller"<br>Age: 45, Gender: Male<br>Phone: "9876543299"<br>Address: "12 Pine St" | Patient saved to MySQL; popup displays newly generated Patient ID; table updates immediately. | **PASS** |
| **PAT-02** | (Negative) Malformed Phone | 1. Enter 9-digit phone.<br>2. Click 'Save Patient'. | Phone: "987654321" | Rejected with message: "Patient Phone must contain exactly 10 numeric digits." | **PASS** |
| **PAT-03** | (Negative) Duplicate Phone | 1. Enter an already registered phone.<br>2. Click 'Save Patient'. | Phone: `9876543210` | Rejected with message: "A patient with phone number '9876543210' is already registered..." | **PASS** |
| **PAT-04** | (Negative) Invalid Age | 1. Enter age -5 or 150.<br>2. Click 'Save Patient'. | Age: -5 | Rejected with message: "Patient Age must be between 0 and 125 years." | **PASS** |
| **PAT-05** | Overloaded Search (By ID) | 1. Enter numeric ID `1` in search.<br>2. Click 'Search'. | Query: `1` | Invokes `search(int id)`; table displays single matched patient record. | **PASS** |
| **PAT-06** | Overloaded Search (By Name) | 1. Enter partial text "Eleanor".<br>2. Click 'Search'. | Query: "Eleanor" | Invokes `search(String name)`; table displays Eleanor Vance. | **PASS** |
| **PAT-07** | (Negative) Safe Deletion Block | 1. Select patient with existing appointments.<br>2. Click 'Delete'. | Patient #1 | Deletion blocked with message: "Cannot delete patient #1 because they have linked appointments..." | **PASS** |

---

### Module C: Doctor Management

| Test ID | Scenario | Steps to Execute | Input Data | Expected Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **DOC-01** | Register New Specialist | 1. Navigate to Doctors.<br>2. Fill doctor form.<br>3. Click 'Save Doctor'. | Name: "Dr. Karen Page"<br>Age: 38, Gender: Female<br>Spec: "Neurology"<br>Fee: 850.00 | Doctor saved to MySQL; table updates immediately. | **PASS** |
| **DOC-02** | Filter Available Only | 1. Check 'Available Only'. | Checkbox: True | Table displays only doctors with `available = 1`; inactive doctors hidden. | **PASS** |
| **DOC-03** | (Negative) Negative Fee | 1. Enter fee `-50.00`.<br>2. Click 'Save Doctor'. | Fee: -50.00 | Rejected with message: "Consultation Fee cannot be negative." | **PASS** |

---

### Module D: Appointment Scheduling & Clash Prevention

| Test ID | Scenario | Steps to Execute | Input Data | Expected Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **APPT-01** | Dynamic Free Slot Discovery | 1. Select Dr. Marcus Welby.<br>2. Select date `2026-11-20`. | Doctor #1, Date: 2026-11-20 | Slot dropdown queries DB and populates only unbooked slots (09:00, 10:00, etc.; 09:30 is excluded). | **PASS** |
| **APPT-02** | (Negative) Prevent Duplicate Slot | 1. Attempt to force-book slot 09:30 on 2026-11-20 for Doctor #1. | Doctor #1, Date: 2026-11-20, Slot: 09:30 | Service layer blocks booking: "Doctor already has an appointment booked on this slot." Database UNIQUE constraint also protects. | **PASS** |
| **APPT-03** | (Negative) Booking in Past | 1. Enter yesterday's date.<br>2. Click 'Confirm Appointment'. | Date: Yesterday | Rejected with message: "Cannot schedule an appointment in the past." | **PASS** |
| **APPT-04** | Mark Completed | 1. Select Scheduled appointment.<br>2. Click 'Mark Selected as Completed'. | Appt #6 | Status transitions from `Scheduled` to `Completed`. | **PASS** |
| **APPT-05** | (Negative) Reopen Completed | 1. Try to reschedule a completed appointment. | Status: Completed | Rejected with message: "A Completed appointment cannot be changed or reopened." | **PASS** |

---

### Module E: Billing & Financial Management

| Test ID | Scenario | Steps to Execute | Input Data | Expected Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **BILL-01** | Automatic Total Calculation | 1. Link appointment with $750.00 fee.<br>2. Enter other charges $250.00. | Fee: 750.00, Other: 250.00 | Total Due auto-updates to `$1000.00` in real-time. User cannot type total. | **PASS** |
| **BILL-02** | Transactional Billing & Appt | 1. Generate bill for an appointment.<br>2. Check appointment status. | Linked Appt #6 | Bill record created and Appointment #6 status atomically updated to `Completed`. | **PASS** |
| **BILL-03** | Mark Bill as Paid | 1. Select Pending bill.<br>2. Click 'Mark Selected as Paid'. | Bill #3 | Bill status updated to `Paid`; total revenue counter on dashboard increments. | **PASS** |
| **BILL-04** | Printable Invoice Dialog | 1. Select bill.<br>2. Click 'View Printable Invoice'. | Bill #1 | Displays modal with formatted header, itemized breakdown, total due, and official hospital signature text. | **PASS** |

---

### Module F: Medical Records (EMR)

| Test ID | Scenario | Steps to Execute | Input Data | Expected Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **EMR-01** | Record Clinical Encounter | 1. Select Patient and Doctor.<br>2. Enter diagnosis & Rx.<br>3. Click 'Save Medical Record'. | Patient #1, Doctor #1, Diagnosis: "Stage 1 Hypertension" | Record persisted with current timestamp; table updates with new entry. | **PASS** |
| **EMR-02** | View Chronological History | 1. Select Patient #1.<br>2. Click 'View Full Patient Clinical History'. | Patient #1 | Opens formatted historical dossier showing all consultations ordered newest first. | **PASS** |
