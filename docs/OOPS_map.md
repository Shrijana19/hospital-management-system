# Object-Oriented Programming (OOPS) Implementation Map

This document serves as the examiner's reference guide for the **Hospital Management System (HMS)** college project. It maps the four fundamental pillars of Object-Oriented Programming to the exact classes, methods, and files where they are implemented in production code.

---

## 1. Summary Matrix for Viva / Examination

| OOPS Principle | Implementation Class & Interface | Source File Path | Line / Method Reference | One-Line Explanation for Examiner |
| :--- | :--- | :--- | :--- | :--- |
| **Encapsulation** | `Person`, `Patient`, `Doctor` | `src/main/java/com/hospital/model/Person.java`<br>`Patient.java`, `Doctor.java` | `setAge(int)`<br>`setPhone(String)`<br>`setName(String)` | All state variables are declared `private`; public setters enforce strict data integrity rules and reject invalid states (e.g., negative age, malformed 10-digit phone). |
| **Inheritance** | `Patient extends Person`<br>`Doctor extends Person` | `src/main/java/com/hospital/model/Patient.java`<br>`src/main/java/com/hospital/model/Doctor.java` | Class declarations (`extends Person`) | Specializes the abstract `Person` base entity into clinical personnel (`Doctor`) and recipients of care (`Patient`), inheriting shared demographic attributes. |
| **Polymorphism (Runtime / Overriding)** | `Person` (abstract contracts)<br>`Patient`, `Doctor` | `src/main/java/com/hospital/model/Person.java`<br>`Patient.java`<br>`Doctor.java`<br>`DirectoryPanel.java` | `public abstract String getSummary();`<br>`public abstract String displayInfo();`<br>`DirectoryPanel.inspectSelectedPerson()` | `Person` declares abstract contracts; `Patient` and `Doctor` provide specialized implementations. A `List<Person>` in `DirectoryPanel` dynamically resolves calls at runtime. |
| **Polymorphism (Compile-time / Overloading)** | `PatientService`<br>`DoctorService`<br>`BillingService` | `src/main/java/com/hospital/service/PatientService.java`<br>`BillingService.java` | `search(int id)` vs `search(String name)`<br>`calculateTotal(BigDecimal fee)` vs `calculateTotal(fee, charges)` | Multiple methods share the same name with different parameter signatures, providing flexible lookup keys and multi-tiered financial calculations. |
| **Abstraction** | `CrudDao<T>` interface<br>`Billable` interface<br>`Person` abstract class | `src/main/java/com/hospital/dao/CrudDao.java`<br>`src/main/java/com/hospital/model/Billable.java`<br>`src/main/java/com/hospital/model/Person.java` | `CrudDao<T>` contract<br>`BigDecimal calculateTotal()` | Hides raw JDBC and persistence complexity behind generic repository interfaces, and defines financial contracts (`Billable`) independent of table schemas. |

---

## 2. In-Depth Principle Breakdown

### A. Encapsulation
- **Private State, Public Contracts**: Every field in `Person`, `Patient`, `Doctor`, `Appointment`, `Bill`, `MedicalRecord`, and `User` is marked `private`.
- **Defensive Setters**:
  - `Person.setAge(int age)`: Rejects negative ages or ages exceeding 125 with an `IllegalArgumentException`.
  - `Person.setPhone(String phone)`: Sanitizes input and validates exactly 10 numeric digits.
  - `Doctor.setConsultationFee(BigDecimal fee)`: Prevents negative values and enforces two decimal places (`RoundingMode.HALF_UP`).
  - `Bill.setTotalAmount(BigDecimal amount)`: Protects financial amounts from floating-point inaccuracies.
- **Data Protection**: Raw database result sets and connections are strictly confined within the `dao` package; model objects never leak raw SQL or database pointers to the UI.

### B. Inheritance
- **Hierarchical Domain Structure**:
  ```
               ┌───────────────────────┐
               │    abstract Person    │
               │ id, name, age, gender │
               │     phone, address    │
               └───────────┬───────────┘
                           │
             ┌─────────────┴─────────────┐
             ▼                           ▼
  ┌──────────────────────┐    ┌──────────────────────┐
  │       Patient        │    │        Doctor        │
  │  medicalHistory      │    │  specialization      │
  │  registeredOn        │    │  consultationFee     │
  │                      │    │  available           │
  └──────────────────────┘    └──────────────────────┘
  ```
- **Code Reusability**: Common demographic attributes and standard setter validations are written once in `Person` and inherited seamlessly by both `Patient` and `Doctor`.

### C. Polymorphism
1. **Method Overriding (Dynamic Method Dispatch)**:
   - `Person` declares two abstract methods:
     ```java
     public abstract String getSummary();
     public abstract String displayInfo();
     ```
   - `Patient` overrides `getSummary()` to display patient ID, age, and medical history.
   - `Doctor` overrides `getSummary()` to display doctor ID, clinical specialty, and OPD consultation fee.
   - **Real Execution in UI**: The `DirectoryPanel` fetches `List<Person> list = patientService.getHospitalDirectory();`. When the user clicks "Inspect", the code invokes `person.displayInfo()`. The Java Virtual Machine resolves the exact method dynamically based on the underlying object type.
2. **Method Overloading (Compile-Time Resolution)**:
   - In `PatientService`:
     - `search(int id)`: Executes indexed numeric lookup.
     - `search(String name)`: Executes partial case-insensitive wildcard search.
   - In `BillingService`:
     - `calculateTotal(BigDecimal fee)`: Standard consultation charge only.
     - `calculateTotal(BigDecimal fee, BigDecimal otherCharges)`: Composite invoice including pharmacy and lab charges.

### D. Abstraction
1. **Generic Repository Interface (`CrudDao<T>`)**:
   ```java
   public interface CrudDao<T> {
       T add(T entity);
       boolean update(T entity);
       boolean delete(int id);
       Optional<T> getById(int id);
       List<T> getAll();
   }
   ```
   The Service and UI layers depend strictly on the `CrudDao` interface, never on MySQL-specific statements or driver configurations.
2. **Domain Contract (`Billable`)**:
   ```java
   public interface Billable {
       BigDecimal calculateTotal();
   }
   ```
   `Bill` implements `Billable`, guaranteeing that any commercial entity within the hospital system can be audited for its final payable sum without knowing internal invoice structures.
