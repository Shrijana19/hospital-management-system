# Viva Voce Examination Preparation Guide

This question-and-answer sheet is designed to help students confidently explain the architectural, object-oriented, and database design choices of the **Hospital Management System** to an examiner.

---

### Q1: Where and how did you implement the four pillars of OOPS in this project?
**Answer:**
1. **Encapsulation**: Implemented in model classes like `Person`, `Patient`, `Doctor`, and `Bill`. All instance variables are declared `private` and accessed exclusively via getters and validating setters. For example, `Person.setAge()` rejects negative numbers or ages above 125, and `Person.setPhone()` verifies exactly 10 digits before assigning state.
2. **Inheritance**: Implemented via an abstract parent class `Person` (`src/main/java/com/hospital/model/Person.java`), extended by both `Patient` and `Doctor`. Shared attributes (id, name, age, gender, phone, address) and validation logic are inherited, avoiding code duplication.
3. **Polymorphism**:
   - *Runtime Polymorphism (Method Overriding)*: `Person` defines abstract methods `getSummary()` and `displayInfo()`. Both `Patient` and `Doctor` override them. In `DirectoryPanel`, a polymorphic `List<Person>` holds both doctors and patients, and invoking `person.displayInfo()` dynamically triggers the correct subclass implementation at runtime.
   - *Compile-Time Polymorphism (Method Overloading)*: Implemented in `PatientService.search(int id)` vs `PatientService.search(String name)`, as well as `BillingService.calculateTotal(BigDecimal fee)` vs `calculateTotal(BigDecimal fee, BigDecimal otherCharges)`.
4. **Abstraction**: Implemented through the generic interface `CrudDao<T>` (`add`, `update`, `delete`, `getById`, `getAll`) and the `Billable` interface (`calculateTotal()`). The UI and business services interact only with these abstract contracts, knowing nothing of underlying MySQL driver details.

---

### Q2: Why is there no SQL in the Swing UI classes? What is layered architecture?
**Answer:**
We adopted a strict **Layered 3-Tier Architecture**:
1. **Presentation Layer (`com.hospital.ui`)**: Handles Swing rendering, button events, and user feedback.
2. **Business Service Layer (`com.hospital.service`)**: Handles business logic, validation rules, slot calculations, and transactions.
3. **Data Access Layer (`com.hospital.dao` & `database`)**: Formulates PreparedStatement SQL, manages JDBC connections, and maps ResultSets to Java entities.

**Examiner Defense:** If the UI directly issued SQL strings, any change in table schema (e.g., renaming a column or migrating from MySQL to PostgreSQL) would force rewriting every GUI screen. Decoupling ensures high maintainability, unit testability, and adherence to the Single Responsibility Principle.

---

### Q3: How do you prevent double-booking a doctor for the exact same slot?
**Answer:**
We enforce clash prevention at **two independent levels**:
1. **Database-Level Defense (Hard Constraint)**: In `appointments` table (`schema.sql`), we declared a composite unique key:
   ```sql
   CONSTRAINT uq_doctor_slot UNIQUE (doctor_id, appointment_date, appointment_time)
   ```
   Even if two users clicked submit at the same millisecond, the database engine will reject the second transaction with error code `1062` (Duplicate entry).
2. **Service & UI Level Defense (User Experience)**: In `AppointmentService.getAvailableTimeSlots()`, we query booked appointments for that doctor on that date, subtract them from clinic operating slots (09:00 - 17:00), and only populate the dropdown with genuinely unbooked, free slots.

---

### Q4: Why did you use `BigDecimal` instead of `float` or `double` for billing amounts?
**Answer:**
Binary floating-point types (`float` and `double`) represent numbers in base-2 scientific notation (IEEE 754). They cannot accurately represent decimal fractions like `0.1` or `0.05`, leading to catastrophic rounding errors in cumulative financial calculations (e.g. `0.1 + 0.2 = 0.30000000000000004`).
`BigDecimal` provides exact arbitrary-precision arithmetic with explicit scale (2 decimal places) and rounding modes (`RoundingMode.HALF_UP`), which is mandatory for hospital billing and compliance.

---

### Q5: How are passwords stored and authenticated? Why not MD5 or SHA-256?
**Answer:**
We never store plain-text passwords. We use **PBKDF2WithHmacSHA256** from the Java standard library (`javax.crypto.SecretKeyFactory`):
1. **Cryptographic Salt**: Each user gets a unique, cryptographically random 16-byte salt (`SecureRandom`). This prevents precomputed Rainbow Table attacks.
2. **65,536 Iterations**: Deliberately CPU-intensive to slow down brute-force and dictionary attacks.
3. **Constant-Time Comparison**: `verifyPassword` uses constant-time byte-by-byte comparison to prevent timing attacks.
Fast hash algorithms like plain MD5 or SHA-256 are insecure for passwords because modern GPUs can compute billions of MD5 hashes per second.

---

### Q6: Where did you use JDBC Transactions, and why were they necessary?
**Answer:**
In `BillDao.createBillWithAppointmentTransaction()`:
When generating a bill for a scheduled appointment, two operations must happen together:
1. Insert the new row into the `bills` table.
2. Update the `appointments` table setting `status = 'Completed'`.
We invoke `conn.setAutoCommit(false)`. If either query fails, we call `conn.rollback()`. Only if both queries succeed do we call `conn.commit()`. This guarantees the **ACID Atomicity property**—we never end up with an issued bill for an appointment that remained in "Scheduled" status, or vice-versa.

---

### Q7: Why do you use `SwingWorker` for database operations?
**Answer:**
Java Swing is single-threaded; all UI rendering and user interactions run on the **Event Dispatch Thread (EDT)**.
If a long-running database query (such as connecting to MySQL across a network) runs on the EDT, the entire desktop window freezes, stops repainting, and the OS marks the application as "Not Responding".
`SwingWorker` performs the blocking JDBC call on a background worker thread (`doInBackground()`) and publishes the resulting data back to the EDT (`done()`), keeping the user interface completely responsive and smooth.

---

### Q8: What is `try-with-resources` and where is it used?
**Answer:**
Introduced in Java 7, `try-with-resources` automatically closes any resource that implements `java.lang.AutoCloseable` at the end of the statement block, even if an exception is thrown.
We use it across all DAOs for `Connection`, `PreparedStatement`, and `ResultSet`. This guarantees zero database connection leaks or cursor exhaustion.
