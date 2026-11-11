# Hospital Management System (HMS)

> **College OOPS Project** | Java 17 LTS, Java Swing, JDBC, and MySQL 8.x  
> **Core Principle**: *"One patient, one record, one clear history. Clean OOP in front, reliable SQL behind."*

[![Java 17](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://www.oracle.com/java/)
[![MySQL 8.x](https://img.shields.io/badge/Database-MySQL%208.x-orange.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-Academic%20Use%20Only-green.svg)]()

---

## 1. Project Purpose & Summary

Small hospitals and outpatient clinics often struggle with fragmented record-keeping across paper files and spreadsheets. This leads to double-booked doctors, lost medical histories, slow billing, and data loss.

The **Hospital Management System** is a modular desktop application engineered to demonstrate clean Object-Oriented Programming (OOPS) and enterprise architectural patterns. It enables hospital staff to register patients, manage clinicians, book clash-free appointments, generate automated itemized bills, and track patient clinical records within an ACID-compliant MySQL database.

---

## 2. Key Features

- **Staff Authentication & RBAC**:
  - Salted PBKDF2 cryptographic hashing (NIST-compliant, 65,536 iterations).
  - Role-Based Access Control: `ADMIN` (full privileges & staff management) and `RECEPTIONIST` (OPD queue, patient records, and billing).
  - Brute-force rate limiting with exponential backoff.
- **Patient Management**:
  - Unique database-generated ID display.
  - Validation against malformed phone numbers, illegal ages, or empty fields.
  - Prevention of duplicate registrations by contact number.
  - Overloaded search by unique ID or partial patient name.
- **Doctor Management**:
  - Specialty classification, consultation fee management, and active OPD availability toggles.
  - Overloaded search by ID, name, or medical specialty.
- **Appointment Scheduling & Clash Prevention**:
  - **Dynamic Free Slot Calculation**: Automatically queries the database to show only available 30-minute consultation slots.
  - **Double-Booking Defense**: Enforced by a database composite unique constraint `(doctor_id, appointment_date, appointment_time)` and service-layer validation.
  - Rejection of past dates and enforcement of status lifecycle (`Scheduled` -> `Completed` / `Cancelled`).
- **Automated Billing Management**:
  - Automatic calculation of totals using `BigDecimal` (user cannot type total).
  - Overloaded calculation methods (`calculateTotal(fee)` vs `calculateTotal(fee, charges)`).
  - **Transactional Billing**: Atomically saves the bill and marks the linked appointment as `Completed`.
  - Printable invoice dialog with official header, itemized breakdown, and status stamps.
- **Electronic Medical Records (EMR)**:
  - Longitudinal clinical dossier: records diagnoses, prescriptions, treatments, and doctor notes.
  - Patient history viewer sorted chronologically (newest first).
- **Runtime Polymorphism Showcase**:
  - Dedicated **Unified Directory** panel that loads heterogeneous `Patient` and `Doctor` objects into a single `List<Person>`.
  - Demonstrates dynamic method dispatch by calling abstract contracts `getSummary()` and `displayInfo()`.

---

## 3. Architecture & Design

### Layered 3-Tier Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│        (Java Swing UI - Login, Dashboard, Panels)           │
└──────────────────────────────┬──────────────────────────────┘
                               │ Method Calls (No SQL)
┌──────────────────────────────▼──────────────────────────────┐
│                   Business Service Layer                    │
│   (Validator, PatientService, BillingService, AuthService)   │
└──────────────────────────────┬──────────────────────────────┘
                               │ CRUD Operations
┌──────────────────────────────▼──────────────────────────────┐
│                  Data Access Layer (DAO)                    │
│        (CrudDao<T>, PatientDao, BillDao, UserDao)           │
└──────────────────────────────┬──────────────────────────────┘
                               │ JDBC (PreparedStatements)
┌──────────────────────────────▼──────────────────────────────┐
│                    MySQL 8.x Database                       │
│      (hospital_management: InnoDB, Constraints, Indexes)     │
└─────────────────────────────────────────────────────────────┘
```

### OOPS Implementation Summary

| Principle | Primary Implementation | Location |
| :--- | :--- | :--- |
| **Encapsulation** | Private fields with validating setters (`setAge`, `setPhone`, `setFee`) | `com.hospital.model.Person`, `Patient`, `Doctor` |
| **Inheritance** | `Patient extends Person`, `Doctor extends Person` | `src/main/java/com/hospital/model/` |
| **Polymorphism (Overriding)** | Abstract `displayInfo()` and `getSummary()` dynamically dispatched on `List<Person>` | `DirectoryPanel.java`, `Person.java` |
| **Polymorphism (Overloading)** | `search(int id)` vs `search(String name)`, `calculateTotal(fee)` vs `calculateTotal(fee, charges)` | `PatientService.java`, `BillingService.java` |
| **Abstraction** | `CrudDao<T>` interface and `Billable` interface | `com.hospital.dao.CrudDao`, `com.hospital.model.Billable` |

*For complete details, see [docs/OOPS_map.md](docs/OOPS_map.md).*

---

## 4. Technology Stack

- **Language**: Java 17 LTS (Compatible with JDK 11 through 21)
- **GUI Framework**: Java Swing (Custom modern design system with Segoe UI typography and medical color palette)
- **Database**: MySQL Server 8.x (InnoDB engine, UTF-8 mb4 encoding)
- **Connectivity**: JDBC with MySQL Connector/J (`com.mysql.cj.jdbc.Driver`)
- **Password Security**: Java Crypto PBKDF2WithHmacSHA256 (65,536 iterations, 16-byte random salt)
- **Testing**: JUnit 5 test suite + Standalone Automated Test Runner (zero dependency)
- **Build System**: Apache Maven or native Windows batch scripts

---

## 5. Project Directory Structure

```
HospitalManagementSystem/
├── pom.xml                               # Maven project descriptor
├── build.bat                             # Windows compilation script
├── run.bat                               # Windows application launch script
├── test.bat                              # Standalone automated test runner script
├── README.md                             # Project documentation
├── sql/
│   ├── schema.sql                        # DDL: database, tables, constraints, indexes
│   └── sample_data.sql                   # DML: fictional sample data and seed users
├── resources/
│   └── db.properties.example             # Database configuration template
├── docs/
│   ├── OOPS_map.md                       # Comprehensive OOPS principle matrix
│   ├── er_diagram.md                     # ER diagram and relational integrity description
│   ├── test_plan.md                      # Manual test plan & negative test scenarios
│   ├── setup_guide.md                    # Step-by-step setup walkthrough
│   └── viva_questions.md                 # Exam/viva preparation Q&A guide
└── src/
    ├── main/
    │   ├── resources/
    │   │   ├── db.properties.example     # Classpath configuration template
    │   │   └── db.properties             # Active local database settings
    │   └── java/com/hospital/
    │       ├── Main.java                 # Startup entry point & pre-flight connection check
    │       ├── model/                    # Encapsulated entities (Person, Patient, Doctor, etc.)
    │       ├── database/                 # JDBC DatabaseConnection manager
    │       ├── dao/                      # Generic CrudDao<T> & table DAO implementations
    │       ├── service/                  # Business logic, Validator, and Services
    │       ├── exception/                # DataAccessException and ValidationException
    │       ├── util/                     # PasswordHasher (PBKDF2)
    │       ├── ui/                       # Swing UI (LoginFrame, DashboardFrame, Panels)
    │       └── test/                     # StandaloneTestRunner
    └── test/
        └── java/com/hospital/            # JUnit 5 test classes (ValidatorTest, BillingTest, etc.)
```

---

## 6. Setup & Installation

### Step 1: Database Setup
1. Launch **MySQL Workbench** or open your terminal.
2. Run `sql/schema.sql` to generate the `hospital_management` database.
3. Run `sql/sample_data.sql` to populate sample patients, doctors, appointments, EMR notes, and users.

### Step 2: Configure `db.properties`
Copy `src/main/resources/db.properties.example` to `src/main/resources/db.properties` and provide your MySQL root password:
```properties
db.url=jdbc:mysql://localhost:3306/hospital_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=root
db.password=your_mysql_password
db.loginTimeoutSeconds=5
```

### Step 3: Launching the Application (Desktop or Web)

You can run the system in either of two modes:

#### Mode 1: Web Application (Browser at `http://localhost:8080/`)
- Double-click **`run_web.bat`**  
  *This starts the built-in HTTP server and automatically opens `http://localhost:8080/` in your browser.*
- Or from command line:
  ```cmd
  java -cp bin com.hospital.Main --web
  ```

#### Mode 2: Desktop GUI Window (Java Swing)
- Double-click **`run.bat`**  
  *This opens the native desktop application window.*
- Or from command line:
  ```cmd
  java -cp bin com.hospital.Main
  ```

---

## 7. Demo Accounts for Viva / Testing

| Username | Password | Role | Permissions |
| :--- | :--- | :--- | :--- |
| `admin` | `admin123` | **ADMIN** | Full administrative privileges, user management, and all clinical tabs. |
| `receptionist` | `recep123` | **RECEPTIONIST** | Outpatient desk duties: patient intake, appointment scheduling, billing, and EMR records. |

*Tip: Click the "Admin" or "Receptionist" quick-fill buttons on the login screen to sign in instantly.*

---

## 8. Running Automated Tests

- **Via Standalone Script**:
  ```cmd
  test.bat
  ```
  *Output: 11 / 11 automated test suites passing (OOP hierarchy, encapsulation setters, dynamic dispatch polymorphism, billing precision, and security).*
- **Via Maven**:
  ```bash
  mvn test
  ```

---

## 9. Known Limitations & Future Improvements

### Limitations (Educational Scope)
- **Local Desktop Architecture**: Designed as a single-node desktop application using JDBC; not configured for multi-server distributed clustering.
- **Educational Medical Data**: Sample clinical observations and prescription records are purely fictional and must not be used for real patient treatment.

### Future Improvements
1. **SMS & Email Alerts**: Integrate JavaMail or Twilio API to dispatch appointment reminders to patients.
2. **Barcode & QR Code Printing**: Generate scanable QR codes on printed invoices for instant cashier verification.
3. **Audit Log Table**: Add an append-only audit trail recording user logins and patient record modifications.
