# Complete Installation & Setup Guide

This guide walks you step-by-step through configuring MySQL 8.x, importing the database schema, configuring JDBC credentials, and running the **Hospital Management System** on your local machine.

---

## 1. Prerequisites

Ensure you have the following installed on your machine:
- **Java Development Kit (JDK)**: Java 17 LTS or later (Oracle OpenJDK, Eclipse Adoptium, Corretto, etc.). Check via `javac -version`.
- **MySQL Server 8.x**: Installed and running as a local service on default port `3306`.
- **MySQL Workbench** (or MySQL CLI): Used to execute the SQL setup scripts.
- **IDE** (Optional): IntelliJ IDEA, Eclipse, NetBeans, or VS Code with Java extension pack.
- **Build Tool** (Optional): Apache Maven 3.8+ (standalone compilation scripts are also included).

---

## 2. Step 1: Initialize the MySQL Database

1. Open **MySQL Workbench** (or log in via terminal: `mysql -u root -p`).
2. Open the file `sql/schema.sql` located inside the project repository.
3. Execute the script (`Ctrl + Shift + Enter` in Workbench).
   - This creates the database `hospital_management` and sets up the six tables: `users`, `patients`, `doctors`, `appointments`, `medical_records`, and `bills` with all constraints, foreign keys, and indexes.
4. Open the file `sql/sample_data.sql`.
5. Execute the script.
   - This seeds the system with fictional sample patients, specialists, appointments, EMR notes, bills, and two pre-hashed user accounts (`admin` and `receptionist`).

> **Quick Command Line Alternative**:
> ```bash
> mysql -u root -p < sql/schema.sql
> mysql -u root -p < sql/sample_data.sql
> ```

---

## 3. Step 2: Configure Database Credentials (`db.properties`)

1. Navigate to `src/main/resources/` (or the root `resources/` folder).
2. Copy `db.properties.example` and name the copy `db.properties`.
3. Open `db.properties` in any text editor and adjust your MySQL password:

```properties
# MySQL Connector/J Connection URL
db.url=jdbc:mysql://localhost:3306/hospital_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC

# Your local MySQL username (usually root)
db.username=root

# Your local MySQL password
db.password=your_mysql_password_here

# Connection timeout in seconds
db.loginTimeoutSeconds=5
```

> **Security Note**: Never commit `db.properties` with real credentials to Git. It is already added to `.gitignore`.

---

## 4. Step 3: Running the Application

You can run the system in either of two user interface modes:

### Mode 1: Web Application (`http://localhost:8080/`)
1. Double-click [**`run_web.bat`**](file:///c:/Users/Shrijana/Downloads/ooppro/run_web.bat).
2. It launches the built-in HTTP server on port `8080` and automatically opens your browser to:
   ```
   http://localhost:8080/
   ```
3. The web portal features the exact same functionality (OPD dashboard, patient intake, clash-free appointment booking, automatic billing, EMR history, and OOP polymorphism inspection) all running against MySQL via JDBC!

### Mode 2: Java Swing Desktop Window
1. Double-click [**`run.bat`**](file:///c:/Users/Shrijana/Downloads/ooppro/run.bat).
2. Launches the native desktop GUI window.

### Option B: Using Maven
If Apache Maven is installed on your system:
```bash
# Clean, compile, and run all automated tests
mvn clean test

# Package into a runnable JAR
mvn package

# Run the packaged application
java -jar target/hospital-management-system-1.0.0.jar
```

### Option C: Opening in an IDE

#### 1. IntelliJ IDEA
1. Open IntelliJ IDEA -> **File** -> **Open...**
2. Select the `HospitalManagementSystem` folder.
3. When prompted, choose **Open as Maven Project**.
4. IntelliJ will download dependencies automatically.
5. Expand `src/main/java/com/hospital/` -> Right-click `Main.java` -> Select **Run 'Main.main()'**.

#### 2. Eclipse IDE
1. Open Eclipse -> **File** -> **Import...**
2. Choose **Maven** -> **Existing Maven Projects** -> Click **Next**.
3. Browse to the project folder and click **Finish**.
4. In Project Explorer, locate `src/main/java/com/hospital/Main.java`.
5. Right-click `Main.java` -> **Run As** -> **Java Application**.

#### 3. Apache NetBeans
1. Open NetBeans -> **File** -> **Open Project...**
2. Select the project directory (NetBeans automatically recognizes the Maven icon).
3. Right-click the project -> Click **Clean and Build**.
4. Right-click `Main.java` -> Click **Run File**.

---

## 5. Step 4: System Login Credentials

The database comes pre-seeded with two demo accounts:

| Username | Password | Role | Permissions |
| :--- | :--- | :--- | :--- |
| **admin** | `admin123` | **ADMIN** | Full administrative rights, User Management, Doctor directory, Appointments, Billing, EMR. |
| **receptionist** | `recep123` | **RECEPTIONIST** | Operational OPD access: Patient registration, clash-free appointment booking, billing, and medical records. |

> **Viva Tip**: On the login screen, you can click the **Quick Fill: Admin** or **Quick Fill: Receptionist** buttons to immediately test both roles without manually typing credentials.

---

## 6. Troubleshooting Common Issues

### Issue 1: "Cannot reach MySQL server at 127.0.0.1:3306"
- **Cause**: MySQL Server service is stopped.
- **Fix**: Open Windows Services (`services.msc`), locate `MySQL80` (or `MySQL`), right-click and select **Start**. Alternatively in PowerShell (as Administrator): `Start-Service MySQL80`.

### Issue 2: "Access denied for user 'root'@'localhost'"
- **Cause**: Incorrect MySQL password in `src/main/resources/db.properties`.
- **Fix**: Verify your root password in MySQL Workbench and update `db.password=...` in `db.properties`.

### Issue 3: "Database 'hospital_management' not found"
- **Cause**: The schema creation script has not yet been executed.
- **Fix**: Open `sql/schema.sql` in MySQL Workbench and run it to create the schema.

### Issue 4: "MySQL Connector/J driver not found"
- **Cause**: JDBC driver JAR missing from classpath when running manually without Maven.
- **Fix**: Either place `mysql-connector-j-8.3.0.jar` inside a `lib/` folder, or use `mvn compile exec:java`, or run via your IDE which handles Maven dependencies automatically.
