# Hospital Management System

> A comprehensive, multi-user Java Swing desktop application for managing a hospital's core operations: patient-doctor assignments, graphical bed management, and a role-based billing system with PDF invoice generation.

This desktop application (Java + Swing) simulates a real hospital management environment with role-based logins and tailored interfaces for each staff role.

---

## ✨ Features

* **Role-Based Access Control**

  * Administrator: Full access to all modules.
  * Doctor / Senior Doctor: Doctor Portal — view/search assigned patients.
  * Nurse / Head Nurse: Patient management & graphical bed management.
  * Receptionist: Patient registration and management.
  * Billing Staff: Patient management and billing/discharge.

* **Patient Management (CRUD)**

  * Register, update, delete patient records.
  * Assign doctor (dynamic list).
  * Select illness from dropdown or enter custom illness.
  * Set disease severity (Mild, Moderate, Severe).
  * Requested bed type for inpatients (if severity requires admission).

* **Doctor Management (CRUD)**

  * Add, update, delete doctor profiles.
  * Automatic "Dr." prefix for new doctor entries.
  * Consultation fee and available days configurable.

* **Graphical Bed Management**

  * Visual block layout instead of plain tables.
  * Color-coded beds (Available / Occupied).
  * Beds grouped by floor and wrapped to fit window.
  * Hover shows tooltip with patient info, bed type, and price.

* **Smart Bed Allotment**

  * Mild cases are outpatients (no bed assigned).
  * For Moderate/Severe, receptionist chooses bed type; system assigns the first available bed of that type.

* **Billing & PDF Invoices**

  * Generate bills on discharge.
  * Bill calculation based on stay duration, dynamic bed price, doctor fee, and service charges.
  * Billing history with double-click bill details.
  * Generate/download professional PDF invoices (saved to `generated_bills/`).

* **Quick Details Views**

  * Double-click patient or doctor rows to open read-only details dialogs (doctor dialog includes a list of that doctor's active patients).

---

## 🛠️ Technology Stack

* **Language:** Java
* **UI:** Java Swing
* **Database:** PostgreSQL
* **Libraries / JARs**

  * `postgresql-42.7.8.jar` (PostgreSQL JDBC driver)
  * `pdfbox-app-3.0.2.jar` (Apache PDFBox for PDF generation)

---

## 🚀 How to Run

### 1. Database Setup

Run the SQL script below in your PostgreSQL client (e.g., `psql`, DBeaver). The script creates the database, tables, and sample data.

> **Note:** Creating the database may require superuser privileges. If you cannot run `CREATE DATABASE`, create an empty database named `hospital_db` via your GUI and then run the SQL from `-- STEP 3` onward while connected to `hospital_db`.

```sql
-- ========== STEP 1: CREATE THE DATABASE ==========
CREATE DATABASE hospital_db;

-- ========== STEP 2: CONNECT TO THE NEW DATABASE ==========
-- In psql: \c hospital_db

-- ========== STEP 3: DROP EXISTING TABLES (CLEAN START) ==========
DROP TABLE IF EXISTS billing CASCADE;
DROP TABLE IF EXISTS beds CASCADE;
DROP TABLE IF EXISTS patients CASCADE;
DROP TABLE IF EXISTS doctors CASCADE;
DROP TABLE IF EXISTS employees CASCADE;

-- ========== STEP 4: CREATE TABLES ==========
CREATE TABLE employees (
    employee_id SERIAL PRIMARY KEY,
    employee_number VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    active BOOLEAN DEFAULT true
);

CREATE TABLE doctors (
    doctor_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    consultation_fee NUMERIC(10, 2),
    available_days VARCHAR(100)
);

CREATE TABLE patients (
    patient_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(20),
    illness VARCHAR(255) NOT NULL,
    admitted_date DATE NOT NULL,
    discharged_date DATE,
    doctor_id INT,
    disease_severity VARCHAR(50),
    requested_bed_type VARCHAR(100),
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id)
);

CREATE TABLE beds (
    bed_id SERIAL PRIMARY KEY,
    ward VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'Available',
    patient_id INT UNIQUE,
    floor INT,
    bed_type VARCHAR(100),
    price_per_day NUMERIC(10, 2),
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id)
);

CREATE TABLE billing (
    bill_id SERIAL PRIMARY KEY,
    patient_id INT NOT NULL,
    bed_charge NUMERIC(10, 2),
    service_charge NUMERIC(10, 2),
    doctor_fee NUMERIC(10, 2),
    total NUMERIC(10, 2) NOT NULL,
    bill_date DATE NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id)
);

-- ========== STEP 5: INSERT SAMPLE DATA ==========
INSERT INTO employees (employee_number, password, name, role, department) VALUES
('EMP001', 'admin123', 'Dr. Sarah Wilson', 'Administrator', 'Management'),
('EMP002', 'nurse123', 'Jennifer Martinez', 'Head Nurse', 'Nursing'),
('EMP003', 'doctor123', 'Dr. Michael Chen', 'Senior Doctor', 'Cardiology'),
('EMP004', 'reception123', 'Robert Johnson', 'Receptionist', 'Front Desk'),
('EMP005', 'billing123', 'Lisa Thompson', 'Billing Staff', 'Finance'),
('EMP006', 'nurse456', 'David Brown', 'Nurse', 'Emergency'),
('EMP007', 'doctor456', 'Dr. Priya Sharma', 'Doctor', 'Pediatrics'),
('EMP008', 'admin456', 'Maria Garcia', 'IT Admin', 'IT');

INSERT INTO doctors (name, specialization, phone, email, consultation_fee, available_days) VALUES
('Dr. Michael Chen', 'Cardiology', '9876543210', 'michael.chen@hospital.com', 2000.00, 'Mon,Wed,Fri'),
('Dr. Priya Sharma', 'Pediatrics', '9876543211', 'priya.sharma@hospital.com', 1200.00, 'Tue,Thu,Sat'),
('Dr. Arjun Reddy', 'Neurology', '9876543212', 'arjun.reddy@hospital.com', 2500.00, 'Mon,Tue,Wed,Thu,Fri');

INSERT INTO beds (ward, floor, bed_type, price_per_day) VALUES
('General Ward A', 1, 'General', 1500.00),
('General Ward A', 1, 'General', 1500.00),
('General Ward A', 1, 'General', 1500.00),
('General Ward B', 1, 'General', 1500.00),
('General Ward B', 1, 'General', 1500.00),
('Semi-Private A', 2, 'Semi-Private', 3000.00),
('Semi-Private A', 2, 'Semi-Private', 3000.00),
('Semi-Private B', 2, 'Semi-Private', 3000.00),
('Semi-Private B', 2, 'Semi-Private', 3000.00),
('Semi-Private B', 2, 'Semi-Private', 3000.00),
('Private Wing', 3, 'Private', 6000.00),
('Private Wing', 3, 'Private', 6000.00),
('Private Wing', 3, 'Private', 6000.00),
('Private Wing', 3, 'Private', 6000.00),
('Private Wing', 3, 'Private', 6000.00);

INSERT INTO patients (name, age, gender, illness, admitted_date, doctor_id, disease_severity, requested_bed_type) VALUES
('Aarav Kumar', 68, 'Male', 'Heart Attack', '2025-11-11', 1, 'Severe', 'Private'),
('Saanvi Desai', 55, 'Female', 'Pneumonia', '2025-11-10', 1, 'Moderate', 'Semi-Private'),
('Rohan Joshi', 32, 'Male', 'Appendicitis', '2025-11-11', 3, 'Moderate', 'Semi-Private'),
('Myra Khan', 8, 'Female', 'Bronchitis', '2025-11-10', 2, 'Moderate', 'General'),
('Diya Patel', 45, 'Female', 'Diabetes Management', '2025-11-11', 1, 'Moderate', 'General'),
('Vihaan Singh', 22, 'Male', 'Flu (Influenza)', '2025-11-12', 2, 'Mild', NULL),
('Ishaan Gupta', 6, 'Male', 'Fever', '2025-11-12', 2, 'Moderate', 'General'),
('Advik Iyer', 75, 'Male', 'Stroke', '2025-11-09', 3, 'Severe', 'Private');

UPDATE beds SET status = 'Occupied', patient_id = 1 WHERE bed_id = 11;
UPDATE beds SET status = 'Occupied', patient_id = 2 WHERE bed_id = 6;
UPDATE beds SET status = 'Occupied', patient_id = 3 WHERE bed_id = 7;
UPDATE beds SET status = 'Occupied', patient_id = 4 WHERE bed_id = 1;
UPDATE beds SET status = 'Occupied', patient_id = 5 WHERE bed_id = 2;
UPDATE beds SET status = 'Occupied', patient_id = 7 WHERE bed_id = 3;
UPDATE beds SET status = 'Occupied', patient_id = 8 WHERE bed_id = 12;
```

---

### 2. Project Setup (IDE)

1. **Libraries:**
   Place the following `.jar` files in your project’s `lib/` folder:

   * `postgresql-42.7.8.jar`
   * `pdfbox-app-3.0.2.jar`

2. **Database Credentials:**
   Update `src/dal/DatabaseConnection.java`:

   ```java
   private static final String URL = "jdbc:postgresql://localhost:5432/hospital_db";
   private static final String USER = "postgres"; // Change if needed
   private static final String PASSWORD = "123456789"; // Change if needed
   ```

3. **🧩 VS Code Setup:**
   To make VS Code recognize your external JAR libraries, create or update the file `.vscode/settings.json` with the following:

   ```json
   {
       "java.project.referencedLibraries": [
           "lib/**/*.jar"
       ]
   }
   ```

---

### 3. Compile & Run

1. Compile all `.java` files in the `src/` directory.
2. Run `src/Main.java` to launch the application.

---

## 🔑 Sample Login Credentials

| Role            | Username | Password       |
| --------------- | -------- | -------------- |
| Administrator   | `EMP001` | `admin123`     |
| Head Nurse      | `EMP002` | `nurse123`     |
| Doctor (Senior) | `EMP003` | `doctor123`    |
| Receptionist    | `EMP004` | `reception123` |
| Billing Staff   | `EMP005` | `billing123`   |
| Nurse           | `EMP006` | `nurse456`     |
| Doctor          | `EMP007` | `doctor456`    |
| IT Admin        | `EMP008` | `admin456`     |

---

## 📁 Project Structure

```
Hospital_Management_System/
├── .vscode/
│   └── settings.json
├── generated_bills/
│   └── (PDF invoices saved here)
├── lib/
│   ├── pdfbox-app-3.0.2.jar
│   └── postgresql-42.7.8.jar
├── src/
│   ├── dal/
│   │   ├── DataAccess.java
│   │   └── DatabaseConnection.java
│   ├── model/
│   │   ├── Bed.java
│   │   ├── Bill.java
│   │   ├── Doctor.java
│   │   ├── Employee.java
│   │   └── Patient.java
│   ├── ui/
│   │   ├── BedBlock.java
│   │   ├── BedManagementPanel.java
│   │   ├── BillDialog.java
│   │   ├── BillingPanel.java
│   │   ├── DoctorDetailDialog.java
│   │   ├── DoctorPanel.java
│   │   ├── DoctorPortalPanel.java
│   │   ├── LoginPanel.java
│   │   ├── MainFrame.java
│   │   ├── PatientDetailDialog.java
│   │   ├── PatientPanel.java
│   │   └── WrapLayout.java
│   └── util/
│       └── PdfGenerator.java
├── Main.java
└── README.md
```

---

## 🧠 Notes

* **Passwords** are in plaintext for testing only — use hashing (e.g., BCrypt) in production.
* **Transactions**: Billing + discharge operations are atomic (via transactions in `DataAccess`).
* **PDF generation**: All generated invoices are saved inside `generated_bills/`.
* **Refresh logic**: Tabs reload their data dynamically on switch.
* **Database**: Make sure PostgreSQL is running locally on `localhost:5432`.

---


