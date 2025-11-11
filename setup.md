-- ╔════════════════════════════════════════════════════════════════════════════╗
-- ║                    HOSPITAL MANAGEMENT SYSTEM DATABASE                     ║
-- ║                          PostgreSQL Setup Script                           ║
-- ╚════════════════════════════════════════════════════════════════════════════╝
--
-- Description: Complete database schema and initial data for Hospital Management System
-- Database: PostgreSQL 12+
-- Author: Hospital Management System Team
-- Last Updated: November 2025
--
-- PREREQUISITES:
--   • PostgreSQL 12 or higher installed
--   • Database superuser access or CREATE DATABASE privileges
--   • psql client or database GUI tool (DBeaver, pgAdmin, etc.)
--
-- USAGE INSTRUCTIONS:
--   Method 1 (psql command line):
--     $ psql -U postgres -f setup.sql
--
--   Method 2 (Manual execution):
--     1. Create database: CREATE DATABASE hospital_db;
--     2. Connect to database: \c hospital_db
--     3. Run remaining script sections
--
--   Method 3 (GUI Tools):
--     • Open this file in DBeaver/pgAdmin
--     • Execute each section sequentially
--     • Verify data after completion
--
-- ════════════════════════════════════════════════════════════════════════════


-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- STEP 1: DATABASE CREATION
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- Note: If database already exists, this will fail. That's okay - just connect
--       to existing database and continue with cleanup steps below.

DROP DATABASE IF EXISTS hospital_db;
CREATE DATABASE hospital_db
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TEMPLATE = template0;

\c hospital_db;

\echo '✓ Database created and connected successfully'


-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- STEP 2: CLEANUP EXISTING TABLES
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- Ensures clean slate for fresh installation
-- CASCADE removes all dependent objects (foreign keys, indexes, etc.)

DROP TABLE IF EXISTS billing CASCADE;
DROP TABLE IF EXISTS beds CASCADE;
DROP TABLE IF EXISTS patients CASCADE;
DROP TABLE IF EXISTS doctors CASCADE;
DROP TABLE IF EXISTS employees CASCADE;

\echo '✓ Existing tables cleaned up'


-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- STEP 3: CREATE SCHEMA - TABLE DEFINITIONS
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- ┌─────────────────────────────────────────────────────────────────────────┐
-- │ TABLE: employees                                                        │
-- │ Purpose: Store all hospital staff members with login credentials       │
-- │ Key Features: Auto-incrementing ID, unique employee numbers            │
-- └─────────────────────────────────────────────────────────────────────────┘
CREATE TABLE employees (
    employee_id     SERIAL PRIMARY KEY,
    employee_number VARCHAR(50) UNIQUE NOT NULL,
    password        VARCHAR(100) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    role            VARCHAR(100) NOT NULL,
    department      VARCHAR(100),
    active          BOOLEAN DEFAULT true,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE employees IS 'Hospital staff records with authentication credentials';
COMMENT ON COLUMN employees.employee_number IS 'Unique identifier for login (format: EMP001, EMP002, etc.)';
COMMENT ON COLUMN employees.active IS 'Indicates if employee account is active';

-- ┌─────────────────────────────────────────────────────────────────────────┐
-- │ TABLE: doctors                                                          │
-- │ Purpose: Extended information for medical practitioners                │
-- │ Relationship: doctor_id references employees.employee_id               │
-- └─────────────────────────────────────────────────────────────────────────┘
CREATE TABLE doctors (
    doctor_id        INT PRIMARY KEY,
    name             VARCHAR(100) NOT NULL,
    specialization   VARCHAR(100) NOT NULL,
    phone            VARCHAR(20),
    email            VARCHAR(100),
    consultation_fee NUMERIC(10, 2) CHECK (consultation_fee >= 0),
    available_days   VARCHAR(100),
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE doctors IS 'Medical practitioners with specialization and fee details';
COMMENT ON COLUMN doctors.available_days IS 'Comma-separated days (e.g., Mon,Wed,Fri)';
COMMENT ON COLUMN doctors.consultation_fee IS 'Consultation fee in INR';

-- ┌─────────────────────────────────────────────────────────────────────────┐
-- │ TABLE: patients                                                         │
-- │ Purpose: Patient admission and medical records                         │
-- │ Relationships: Links to doctors and beds                               │
-- └─────────────────────────────────────────────────────────────────────────┘
CREATE TABLE patients (
    patient_id         SERIAL PRIMARY KEY,
    name               VARCHAR(100) NOT NULL,
    age                INT NOT NULL CHECK (age > 0 AND age <= 150),
    gender             VARCHAR(20),
    illness            VARCHAR(255) NOT NULL,
    admitted_date      DATE NOT NULL DEFAULT CURRENT_DATE,
    discharged_date    DATE,
    doctor_id          INT,
    disease_severity   VARCHAR(50) CHECK (disease_severity IN ('Mild', 'Moderate', 'Severe')),
    requested_bed_type VARCHAR(100),
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_patient_doctor FOREIGN KEY (doctor_id) 
        REFERENCES doctors(doctor_id) ON DELETE SET NULL,
    CONSTRAINT valid_discharge_date CHECK (discharged_date IS NULL OR discharged_date >= admitted_date)
);

COMMENT ON TABLE patients IS 'Patient admission records and medical history';
COMMENT ON COLUMN patients.disease_severity IS 'Severity levels: Mild (no bed), Moderate (general/semi-private), Severe (private)';
COMMENT ON COLUMN patients.requested_bed_type IS 'Patient preference: General, Semi-Private, or Private';

-- ┌─────────────────────────────────────────────────────────────────────────┐
-- │ TABLE: beds                                                             │
-- │ Purpose: Hospital bed inventory and occupancy tracking                 │
-- │ Relationship: Links to current patient (if occupied)                   │
-- └─────────────────────────────────────────────────────────────────────────┘
CREATE TABLE beds (
    bed_id        SERIAL PRIMARY KEY,
    ward          VARCHAR(100) NOT NULL,
    status        VARCHAR(50) NOT NULL DEFAULT 'Available' 
                  CHECK (status IN ('Available', 'Occupied', 'Maintenance', 'Reserved')),
    patient_id    INT UNIQUE,
    floor         INT CHECK (floor > 0),
    bed_type      VARCHAR(100) NOT NULL CHECK (bed_type IN ('General', 'Semi-Private', 'Private', 'ICU')),
    price_per_day NUMERIC(10, 2) NOT NULL CHECK (price_per_day >= 0),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_bed_patient FOREIGN KEY (patient_id) 
        REFERENCES patients(patient_id) ON DELETE SET NULL
);

COMMENT ON TABLE beds IS 'Hospital bed inventory with real-time occupancy status';
COMMENT ON COLUMN beds.status IS 'Current bed status: Available, Occupied, Maintenance, Reserved';
COMMENT ON COLUMN beds.bed_type IS 'Bed category affecting pricing and amenities';

-- ┌─────────────────────────────────────────────────────────────────────────┐
-- │ TABLE: billing                                                          │
-- │ Purpose: Financial records for patient services                        │
-- │ Relationship: Links to patient records                                 │
-- └─────────────────────────────────────────────────────────────────────────┘
CREATE TABLE billing (
    bill_id        SERIAL PRIMARY KEY,
    patient_id     INT NOT NULL,
    bed_charge     NUMERIC(10, 2) DEFAULT 0 CHECK (bed_charge >= 0),
    service_charge NUMERIC(10, 2) DEFAULT 0 CHECK (service_charge >= 0),
    doctor_fee     NUMERIC(10, 2) DEFAULT 0 CHECK (doctor_fee >= 0),
    total          NUMERIC(10, 2) NOT NULL CHECK (total >= 0),
    bill_date      DATE NOT NULL DEFAULT CURRENT_DATE,
    payment_status VARCHAR(50) DEFAULT 'Pending' CHECK (payment_status IN ('Pending', 'Paid', 'Partial', 'Cancelled')),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_bill_patient FOREIGN KEY (patient_id) 
        REFERENCES patients(patient_id) ON DELETE CASCADE
);

COMMENT ON TABLE billing IS 'Financial records and invoices for patient services';
COMMENT ON COLUMN billing.total IS 'Total amount = bed_charge + service_charge + doctor_fee';

-- Create useful indexes for performance
CREATE INDEX idx_beds_status ON beds(status);
CREATE INDEX idx_beds_patient ON beds(patient_id);
CREATE INDEX idx_patients_doctor ON patients(doctor_id);
CREATE INDEX idx_patients_admitted ON patients(admitted_date);
CREATE INDEX idx_billing_patient ON billing(patient_id);
CREATE INDEX idx_billing_date ON billing(bill_date);

\echo '✓ Database schema created successfully'


-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- STEP 4: SEED DATA - EMPLOYEES
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- Default credentials for system access (employee_number / password)

INSERT INTO employees (employee_number, password, name, role, department) VALUES
('EMP001', 'admin123',      'Dr. Sarah Wilson',    'Administrator',  'Management'),
('EMP002', 'nurse123',      'Jennifer Martinez',   'Head Nurse',     'Nursing'),
('EMP003', 'doctor123',     'Dr. Michael Chen',    'Senior Doctor',  'Cardiology'),
('EMP004', 'reception123',  'Robert Johnson',      'Receptionist',   'Front Desk'),
('EMP005', 'billing123',    'Lisa Thompson',       'Billing Staff',  'Finance'),
('EMP006', 'nurse456',      'David Brown',         'Nurse',          'Emergency'),
('EMP007', 'doctor456',     'Dr. Priya Sharma',    'Doctor',         'Pediatrics'),
('EMP008', 'admin456',      'Maria Garcia',        'IT Admin',       'IT');

\echo '✓ Employee accounts created (8 users)'


-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- STEP 5: SEED DATA - DOCTORS
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- Medical staff with specializations (includes employee-doctors and external consultants)

-- Employee-Doctors (IDs match employee_id: 3, 7)
INSERT INTO doctors (doctor_id, name, specialization, phone, email, consultation_fee, available_days) VALUES
(3, 'Dr. Michael Chen',   'Cardiology',   '9876543210', 'michael.chen@hospital.com',  2000.00, 'Mon,Wed,Fri'),
(7, 'Dr. Priya Sharma',   'Pediatrics',   '9876543211', 'priya.sharma@hospital.com',  1200.00, 'Tue,Thu,Sat');

-- External Consultant (not in employees table)
INSERT INTO doctors (doctor_id, name, specialization, phone, email, consultation_fee, available_days) VALUES
(9, 'Dr. Emily White',    'Neurology',    '9876543212', 'emily.white@hospital.com',   2500.00, 'Mon,Tue,Wed,Thu,Fri');

\echo '✓ Doctor profiles created (3 doctors)'


-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- STEP 6: SEED DATA - HOSPITAL BEDS
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- Bed inventory across different floors and ward types

-- Floor 1: General Ward (₹1,500/day) - 5 beds
INSERT INTO beds (ward, floor, bed_type, price_per_day, status) VALUES
('General Ward A', 1, 'General', 1500.00, 'Available'),
('General Ward A', 1, 'General', 1500.00, 'Available'),
('General Ward A', 1, 'General', 1500.00, 'Available'),
('General Ward B', 1, 'General', 1500.00, 'Available'),
('General Ward B', 1, 'General', 1500.00, 'Available');

-- Floor 2: Semi-Private Ward (₹3,000/day) - 5 beds
INSERT INTO beds (ward, floor, bed_type, price_per_day, status) VALUES
('Semi-Private A', 2, 'Semi-Private', 3000.00, 'Available'),
('Semi-Private A', 2, 'Semi-Private', 3000.00, 'Available'),
('Semi-Private B', 2, 'Semi-Private', 3000.00, 'Available'),
('Semi-Private B', 2, 'Semi-Private', 3000.00, 'Available'),
('Semi-Private B', 2, 'Semi-Private', 3000.00, 'Available');

-- Floor 3: Private Wing (₹6,000/day) - 5 beds
INSERT INTO beds (ward, floor, bed_type, price_per_day, status) VALUES
('Private Wing', 3, 'Private', 6000.00, 'Available'),
('Private Wing', 3, 'Private', 6000.00, 'Available'),
('Private Wing', 3, 'Private', 6000.00, 'Available'),
('Private Wing', 3, 'Private', 6000.00, 'Available'),
('Private Wing', 3, 'Private', 6000.00, 'Available');

\echo '✓ Hospital beds configured (15 beds across 3 floors)'


-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- STEP 7: SEED DATA - PATIENTS
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- Current admitted patients with medical details

INSERT INTO patients (name, age, gender, illness, admitted_date, doctor_id, disease_severity, requested_bed_type) VALUES
('Emma Wilson',      68, 'Female', 'Heart Attack',          '2025-11-11', 3, 'Severe',   'Private'),
('James Anderson',   55, 'Male',   'Pneumonia',             '2025-11-10', 3, 'Moderate', 'Semi-Private'),
('Sophia Martinez',  32, 'Female', 'Appendicitis',          '2025-11-11', 9, 'Moderate', 'Semi-Private'),
('William Brown',     8, 'Male',   'Bronchitis',            '2025-11-10', 7, 'Moderate', 'General'),
('Olivia Garcia',    45, 'Female', 'Diabetes Management',   '2025-11-11', 3, 'Moderate', 'General'),
('Liam Johnson',     22, 'Male',   'Flu (Influenza)',       '2025-11-12', 7, 'Mild',     NULL),  -- No bed needed
('Ava Davis',         6, 'Female', 'Fever',                 '2025-11-12', 7, 'Moderate', 'General'),
('Noah Miller',      75, 'Male',   'Stroke',                '2025-11-09', 9, 'Severe',   'Private');

\echo '✓ Patient records created (8 patients)'


-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- STEP 8: BED ASSIGNMENTS
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- Link patients to specific beds based on severity and preferences

-- Severe patients → Private beds (Floor 3)
UPDATE beds SET status = 'Occupied', patient_id = 1 WHERE bed_id = 11;  -- Emma Wilson
UPDATE beds SET status = 'Occupied', patient_id = 8 WHERE bed_id = 12;  -- Noah Miller

-- Moderate patients → Semi-Private beds (Floor 2)
UPDATE beds SET status = 'Occupied', patient_id = 2 WHERE bed_id = 6;   -- James Anderson
UPDATE beds SET status = 'Occupied', patient_id = 3 WHERE bed_id = 7;   -- Sophia Martinez

-- Moderate patients → General beds (Floor 1)
UPDATE beds SET status = 'Occupied', patient_id = 4 WHERE bed_id = 1;   -- William Brown
UPDATE beds SET status = 'Occupied', patient_id = 5 WHERE bed_id = 2;   -- Olivia Garcia
UPDATE beds SET status = 'Occupied', patient_id = 7 WHERE bed_id = 3;   -- Ava Davis

-- Note: Patient 6 (Liam Johnson) has 'Mild' severity → No bed assigned

\echo '✓ Bed assignments completed (7 beds occupied)'


-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- STEP 9: VERIFICATION & SUMMARY
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

\echo ''
\echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'
\echo '                    DATABASE SETUP COMPLETE                      '
\echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'
\echo ''

-- Display summary statistics
\echo '📊 SETUP SUMMARY:'
\echo '──────────────────────────────────────────────────────────────────'

SELECT 
    'Employees' as entity, 
    COUNT(*) as count 
FROM employees
UNION ALL
SELECT 'Doctors', COUNT(*) FROM doctors
UNION ALL
SELECT 'Patients', COUNT(*) FROM patients
UNION ALL
SELECT 'Total Beds', COUNT(*) FROM beds
UNION ALL
SELECT 'Occupied Beds', COUNT(*) FROM beds WHERE status = 'Occupied'
UNION ALL
SELECT 'Available Beds', COUNT(*) FROM beds WHERE status = 'Available'
UNION ALL
SELECT 'Billing Records', COUNT(*) FROM billing;

\echo ''
\echo '🔐 DEFAULT LOGIN CREDENTIALS:'
\echo '──────────────────────────────────────────────────────────────────'
\echo '  Administrator  → EMP001 / admin123'
\echo '  Head Nurse     → EMP002 / nurse123'
\echo '  Doctor         → EMP003 / doctor123'
\echo '  Receptionist   → EMP004 / reception123'
\echo '  Billing Staff  → EMP005 / billing123'
\echo ''
\echo '📋 QUICK VERIFICATION QUERIES:'
\echo '──────────────────────────────────────────────────────────────────'
\echo '  View all employees:    SELECT * FROM employees;'
\echo '  View all doctors:      SELECT * FROM doctors;'
\echo '  View all patients:     SELECT * FROM patients;'
\echo '  View bed occupancy:    SELECT * FROM beds;'
\echo '  View billing records:  SELECT * FROM billing;'
\echo ''
\echo '  Patient-Bed mapping:'
\echo '    SELECT p.name, p.illness, b.bed_type, b.ward, b.price_per_day'
\echo '    FROM patients p'
\echo '    LEFT JOIN beds b ON p.patient_id = b.patient_id'
\echo '    ORDER BY p.patient_id;'
\echo ''
\echo '✅ Setup completed successfully! Database is ready for use.'
\echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'