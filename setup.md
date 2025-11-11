-- ========== STEP 1: CREATE THE DATABASE ==========
-- This command must be run first, or you can create the database manually.
-- Note: You might need to run this command separately from the rest of the script.
CREATE DATABASE hospital_db;

-- ========== STEP 2: CONNECT TO THE NEW DATABASE ==========
-- In psql, you would run this command.
-- In a GUI tool like DBeaver, just right-click and "Reconnect" to see the new DB.
\c hospital_db;

---
--- ========== STEP 3: DROP ALL EXISTING TABLES ==========
---
-- This ensures a clean setup if the script is run multiple times.
DROP TABLE IF EXISTS billing CASCADE;
DROP TABLE IF EXISTS beds CASCADE;
DROP TABLE IF EXISTS patients CASCADE;
DROP TABLE IF EXISTS doctors CASCADE;
DROP TABLE IF EXISTS employees CASCADE;

---
--- ========== STEP 4: CREATE FRESH TABLES ==========
---

-- Table for all staff, including login credentials
CREATE TABLE employees (
    employee_id SERIAL PRIMARY KEY,
    employee_number VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    active BOOLEAN DEFAULT true
);

-- Table for doctor-specific details
CREATE TABLE doctors (
    doctor_id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    consultation_fee NUMERIC(10, 2),
    available_days VARCHAR(100) -- e.g., "Mon,Tue,Wed"
);

-- Table for patient records
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

-- Table for all hospital beds
CREATE TABLE beds (
    bed_id SERIAL PRIMARY KEY,
    ward VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'Available', -- Available, Occupied
    patient_id INT UNIQUE,
    floor INT,
    bed_type VARCHAR(100),
    price_per_day NUMERIC(10, 2),
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id)
);

-- Table for all billing history (will be empty)
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

---
--- ========== STEP 5: INSERT DUMMY DATA ==========
---

-- Insert Employees (based on credentials.md)
-- IDs 1-8 will be auto-generated
INSERT INTO employees (employee_number, password, name, role, department) VALUES
('EMP001', 'admin123', 'Dr. Sarah Wilson', 'Administrator', 'Management'),
('EMP002', 'nurse123', 'Jennifer Martinez', 'Head Nurse', 'Nursing'),
('EMP003', 'doctor123', 'Dr. Michael Chen', 'Senior Doctor', 'Cardiology'),
('EMP004', 'reception123', 'Robert Johnson', 'Receptionist', 'Front Desk'),
('EMP005', 'billing123', 'Lisa Thompson', 'Billing Staff', 'Finance'),
('EMP006', 'nurse456', 'David Brown', 'Nurse', 'Emergency'),
('EMP007', 'doctor456', 'Dr. Priya Sharma', 'Doctor', 'Pediatrics'),
('EMP008', 'admin456', 'Maria Garcia', 'IT Admin', 'IT');

-- Insert Doctors
-- IDs 3 and 7 MUST match the employee_ids from above
INSERT INTO doctors (doctor_id, name, specialization, phone, email, consultation_fee, available_days) VALUES
(3, 'Dr. Michael Chen', 'Cardiology', '9876543210', 'michael.chen@hospital.com', 2000.00, 'Mon,Wed,Fri'),
(7, 'Dr. Priya Sharma', 'Pediatrics', '9876543211', 'priya.sharma@hospital.com', 1200.00, 'Tue,Thu,Sat');

-- Insert a doctor who is NOT an employee (ID 9)
INSERT INTO doctors (doctor_id, name, specialization, phone, email, consultation_fee, available_days) VALUES
(9, 'Dr. Emily White', 'Neurology', '9876543212', 'emily.white@hospital.com', 2500.00, 'Mon,Tue,Wed,Thu,Fri');


-- Insert Beds (All are 'Available' by default)
-- Floor 1: General (5 beds)
INSERT INTO beds (ward, floor, bed_type, price_per_day) VALUES
('General Ward A', 1, 'General', 1500.00),
('General Ward A', 1, 'General', 1500.00),
('General Ward A', 1, 'General', 1500.00),
('General Ward B', 1, 'General', 1500.00),
('General Ward B', 1, 'General', 1500.00);

-- Floor 2: Semi-Private (5 beds)
INSERT INTO beds (ward, floor, bed_type, price_per_day) VALUES
('Semi-Private A', 2, 'Semi-Private', 3000.00),
('Semi-Private A', 2, 'Semi-Private', 3000.00),
('Semi-Private B', 2, 'Semi-Private', 3000.00),
('Semi-Private B', 2, 'Semi-Private', 3000.00),
('Semi-Private B', 2, 'Semi-Private', 3000.00);

-- Floor 3: Private (5 beds)
INSERT INTO beds (ward, floor, bed_type, price_per_day) VALUES
('Private Wing', 3, 'Private', 6000.00),
('Private Wing', 3, 'Private', 6000.00),
('Private Wing', 3, 'Private', 6000.00),
('Private Wing', 3, 'Private', 6000.00),
('Private Wing', 3, 'Private', 6000.00);


-- Insert Patients (Patient IDs 1-8 will be auto-generated)
INSERT INTO patients (name, age, gender, illness, admitted_date, doctor_id, disease_severity, requested_bed_type) VALUES
('Emma Wilson', 68, 'Female', 'Heart Attack', '2025-11-11', 3, 'Severe', 'Private'),
('James Anderson', 55, 'Male', 'Pneumonia', '2025-11-10', 3, 'Moderate', 'Semi-Private'),
('Sophia Martinez', 32, 'Female', 'Appendicitis', '2025-11-11', 9, 'Moderate', 'Semi-Private'),
('William Brown', 8, 'Male', 'Bronchitis', '2025-11-10', 7, 'Moderate', 'General'),
('Olivia Garcia', 45, 'Female', 'Diabetes Management', '2025-11-11', 3, 'Moderate', 'General'),
('Liam Johnson', 22, 'Male', 'Flu (Influenza)', '2025-11-12', 7, 'Mild', NULL),
('Ava Davis', 6, 'Female', 'Fever', '2025-11-12', 7, 'Moderate', 'General'),
('Noah Miller', 75, 'Male', 'Stroke', '2025-11-09', 9, 'Severe', 'Private');

---
--- ========== STEP 6: LINK PATIENTS TO BEDS ==========
---

-- Assign Emma Wilson (Patient 1, Severe) to a Private bed (Bed 11)
UPDATE beds SET status = 'Occupied', patient_id = 1 WHERE bed_id = 11;

-- Assign James Anderson (Patient 2, Moderate) to a Semi-Private bed (Bed 6)
UPDATE beds SET status = 'Occupied', patient_id = 2 WHERE bed_id = 6;

-- Assign Sophia Martinez (Patient 3, Moderate) to a Semi-Private bed (Bed 7)
UPDATE beds SET status = 'Occupied', patient_id = 3 WHERE bed_id = 7;

-- Assign William Brown (Patient 4, Moderate) to a General bed (Bed 1)
UPDATE beds SET status = 'Occupied', patient_id = 4 WHERE bed_id = 1;

-- Assign Olivia Garcia (Patient 5, Moderate) to a General bed (Bed 2)
UPDATE beds SET status = 'Occupied', patient_id = 5 WHERE bed_id = 2;

-- Patient 6 (Liam Johnson) is 'Mild' and gets no bed.

-- Assign Ava Davis (Patient 7, Moderate) to a General bed (Bed 3)
UPDATE beds SET status = 'Occupied', patient_id = 7 WHERE bed_id = 3;

-- Assign Noah Miller (Patient 8, Severe) to a Private bed (Bed 12)
UPDATE beds SET status = 'Occupied', patient_id = 8 WHERE bed_id = 12;

---
--- ========== STEP 7: VERIFY DATA (Optional) ==========
---
SELECT * FROM employees;
SELECT * FROM doctors;
SELECT * FROM patients;
SELECT * FROM beds;
SELECT * FROM billing; -- This should be empty