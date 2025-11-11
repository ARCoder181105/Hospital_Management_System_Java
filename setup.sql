-- ========== STEP 1: CREATE THE DATABASE ==========
-- Note: You may need to run this command separately from the rest of the script.
CREATE DATABASE hospital_db;

-- ========== STEP 2: CONNECT TO THE NEW DATABASE ==========
-- In psql, run: \c hospital_db
-- In a GUI tool, just reconnect and select "hospital_db".

---
--- ========== STEP 3: DROP ALL EXISTING TABLES ==========
---
DROP TABLE IF EXISTS billing CASCADE;
DROP TABLE IF EXISTS beds CASCADE;
DROP TABLE IF EXISTS patients CASCADE;
DROP TABLE IF EXISTS doctors CASCADE;
DROP TABLE IF EXISTS employees CASCADE;
DROP TABLE IF EXISTS config_bed_types CASCADE;
DROP TABLE IF EXISTS config_illnesses CASCADE;

---
--- ========== STEP 4: CREATE FRESH TABLES ==========
---

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

CREATE TABLE config_illnesses (
    illness_id SERIAL PRIMARY KEY,
    illness_name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE config_bed_types (
    bed_type_id SERIAL PRIMARY KEY,
    bed_type_name VARCHAR(100) UNIQUE NOT NULL,
    price_per_day NUMERIC(10, 2) NOT NULL
);

CREATE TABLE patients (
    patient_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(20),
    admitted_date DATE NOT NULL,
    discharged_date DATE,
    doctor_id INT,
    disease_severity VARCHAR(50),
    requested_bed_type_id INT,
    illness_id INT,
    other_illness_text VARCHAR(255),
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id),
    FOREIGN KEY (illness_id) REFERENCES config_illnesses(illness_id),
    FOREIGN KEY (requested_bed_type_id) REFERENCES config_bed_types(bed_type_id)
);

CREATE TABLE beds (
    bed_id SERIAL PRIMARY KEY,
    ward VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'Available',
    patient_id INT UNIQUE,
    floor INT,
    bed_type_id INT NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
    FOREIGN KEY (bed_type_id) REFERENCES config_bed_types(bed_type_id)
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

---
--- ========== STEP 5: INSERT DUMMY DATA ==========
---

-- Config Tables (ID 4 = ICU)
INSERT INTO config_bed_types (bed_type_name, price_per_day) VALUES
('General', 1500.00),
('Semi-Private', 3000.00),
('Private', 6000.00),
('ICU', 12000.00); 

INSERT INTO config_illnesses (illness_name) VALUES
('Fever'), ('Flu (Influenza)'), ('Pneumonia'), ('Bronchitis'),
('Appendicitis'), ('Gallstones'), ('Kidney Stones'),
('Heart Attack'), ('Stroke'), ('Diabetes Management'), ('Other...');

-- Main Tables
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

-- Beds (IDs 1-20)
-- Floor 1: General (5 beds, bed_type_id=1)
INSERT INTO beds (ward, floor, bed_type_id) VALUES
('General-A', 1, 1), ('General-A', 1, 1), ('General-A', 1, 1), 
('General-B', 1, 1), ('General-B', 1, 1);
-- Floor 2: Semi-Private (5 beds, bed_type_id=2)
INSERT INTO beds (ward, floor, bed_type_id) VALUES
('Semi-Private-A', 2, 2), ('Semi-Private-A', 2, 2), ('Semi-Private-B', 2, 2), 
('Semi-Private-B', 2, 2), ('Semi-Private-B', 2, 2);
-- Floor 3: Private (5 beds, bed_type_id=3)
INSERT INTO beds (ward, floor, bed_type_id) VALUES
('Private-Wing', 3, 3), ('Private-Wing', 3, 3), ('Private-Wing', 3, 3), 
('Private-Wing', 3, 3), ('Private-Wing', 3, 3);
-- Floor 4: ICU (5 beds, bed_type_id=4)
INSERT INTO beds (ward, floor, bed_type_id) VALUES
('ICU-A', 4, 4), ('ICU-A', 4, 4), ('ICU-B', 4, 4), 
('ICU-B', 4, 4), ('ICU-B', 4, 4);

-- Patients (IDs 1-8)
INSERT INTO patients (name, age, gender, admitted_date, doctor_id, disease_severity, requested_bed_type_id, illness_id) VALUES
('Aarav Kumar', 68, 'Male', '2025-11-11', 1, 'Severe', 3, 8), -- Private, Heart Attack
('Saanvi Desai', 55, 'Female', '2025-11-10', 1, 'Moderate', 2, 3), -- Semi-Private, Pneumonia
('Rohan Joshi', 32, 'Male', '2025-11-11', 3, 'Moderate', 2, 5), -- Semi-Private, Appendicitis
('Myra Khan', 8, 'Female', '2025-11-10', 2, 'Moderate', 1, 4), -- General, Bronchitis
('Diya Patel', 45, 'Female', '2025-11-11', 1, 'Moderate', 1, 10), -- General, Diabetes
('Vihaan Singh', 22, 'Male', '2025-11-12', 2, 'Mild', NULL, 2), -- NULL bed, Flu
('Ishaan Gupta', 6, 'Male', '2025-11-12', 2, 'Moderate', 1, 1), -- General, Fever
('Advik Iyer', 75, 'Male', '2025-11-09', 3, 'Severe', 4, 9); -- ICU, Stroke

---
--- ========== STEP 6: LINK PATIENTS TO BEDS ==========
---
UPDATE beds SET status = 'Occupied', patient_id = 1 WHERE bed_id = 11;
UPDATE beds SET status = 'Occupied', patient_id = 2 WHERE bed_id = 6;
UPDATE beds SET status = 'Occupied', patient_id = 3 WHERE bed_id = 7;
UPDATE beds SET status = 'Occupied', patient_id = 4 WHERE bed_id = 1;
UPDATE beds SET status = 'Occupied', patient_id = 5 WHERE bed_id = 2;
-- Patient 6 (Vihaan Singh) is 'Mild' and gets no bed.
UPDATE beds SET status = 'Occupied', patient_id = 7 WHERE bed_id = 3;
UPDATE beds SET status = 'Occupied', patient_id = 8 WHERE bed_id = 16; -- Linked to an ICU bed

---
--- SETUP COMPLETE
---