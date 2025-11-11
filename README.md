# Hospital Management System

A comprehensive, multi-user Java Swing application for managing a hospital's core operations, including patient-doctor assignments, graphical bed management, role-based access control, and a dynamic billing system with PDF invoice generation.

-----

## ✨ Features

  * **Role-Based Access Control:** A secure login portal that shows different interfaces based on employee roles:

      * **Administrator:** Full access to all modules, including the Dashboard and Configuration.
      * **Doctor / Senior Doctor:** A dedicated "Doctor Portal" to view and search their assigned in-patients and daily appointments.
      * **Nurse / Head Nurse:** Access to patient management and the graphical bed management module.
      * **Receptionist:** Access to patient registration and the appointment scheduling system.
      * **Billing Staff:** Access to patient management and the billing/discharge module.

  * **Admin Dashboard:** An "at-a-glance" dashboard for administrators showing live hospital statistics, including active patients, available beds, total doctors, and total revenue.

  * **Patient Management (Full CRUD):**

      * Register, update, and delete patient records.
      * Assign patients a doctor from a dynamic list.
      * Assign illness from a database-driven dropdown, with an "Other..." option for manual entry.
      * Assign a "Severity" (Mild, Moderate, Severe).

  * **Doctor Management (Full CRUD):**

      * Add, update, and delete doctor profiles from the admin panel.
      * Automatically prefixes "Dr." to new entries.
      * Set doctor-specific details like **Consultation Fee** and **Available Days** (via checkboxes).

  * **Graphical Bed Management:**

      * A modern, graphical "block" layout replaces a traditional table.
      * Beds are color-coded (Green for Available, Red for Occupied).
      * Beds are visually grouped by floor and automatically wrap to fit the window size.
      * Hovering over any bed shows a detailed tooltip with patient info, bed type, and price.

  * **Smart Bed Allotment:**

      * Patients with "Mild" severity are registered as outpatients (no bed assigned).
      * For "Moderate" or "Severe" patients, the receptionist must select a **Bed Type** (e.g., General, Semi-Private, Private), which dynamically shows the price.
      * The system then assigns the first available bed of that requested type.

  * **Appointment Scheduling:**

      * A full-featured module for receptionists to book, view, and cancel outpatient appointments.
      * Automatically checks the doctor's `available_days` to prevent booking on the wrong day.
      * Automatically checks for time-slot conflicts.
      * Allows for easy rescheduling of existing appointments.

  * **Dynamic Billing System:**

      * A dedicated module for billing and discharging patients.
      * Calculates the total bill dynamically based on:
        1.  Number of days stayed.
        2.  The **actual price** of the specific bed type assigned.
        3.  The **actual consultation fee** of the specific doctor assigned.
      * A "Billing History" table shows all past transactions.

  * **PDF Invoice Generation:**

      * Double-click any bill in the history to open a clean, formatted "Bill Details" dialog.
      * From the dialog, a "Download PDF" button generates a professional, multi-page PDF invoice and saves it to the `generated_bills` folder.

  * **Admin Configuration Panel:**

      * An admin-only tab to manage the application's core data without touching the database.
      * Allows admins to **add, edit, and delete Bed Types** (e.g., "General", "ICU") and their prices.
      * Allows admins to **add, edit, and delete Illnesses** from the patient registration dropdown.

-----

## 🛠️ Technology Stack

  * **Language:** Java
  * **UI:** Java Swing
  * **Database:** PostgreSQL
  * **Libraries:**
      * **PostgreSQL JDBC Driver:** For database connectivity.
      * **Apache PDFBox (App):** For PDF generation.

-----

## 📁 Project Structure

```
Hospital_Management_System/
├── .vscode/
│   └── settings.json         (VS Code config)
├── generated_bills/
│   └── (PDFs will be saved here)
├── lib/
│   ├── pdfbox-app-3.0.2.jar
│   └── postgresql-42.7.8.jar
├── src/
│   ├── dal/
│   │   ├── DataAccess.java         (All SQL queries)
│   │   └── DatabaseConnection.java (DB connection logic)
│   ├── model/
│   │   ├── Appointment.java
│   │   ├── Bed.java
│   │   ├── Bill.java
│   │   ├── ConfigBedType.java
│   │   ├── ConfigIllness.java
│   │   ├── Doctor.java
│   │   ├── Employee.java
│   │   └── Patient.java
│   ├── ui/
│   │   ├── AppointmentPanel.java
│   │   ├── BedBlock.java
│   │   ├── BedManagementPanel.java
│   │   ├── BillDialog.java
│   │   ├── BillingPanel.java
│   │   ├── ConfigBedPanel.java
│   │   ├── ConfigIllnessPanel.java
│   │   ├── ConfigPanel.java
│   │   ├── DashboardPanel.java
│   │   ├── DoctorDetailDialog.java
│   │   ├── DoctorPanel.java
│   │   ├── DoctorPortalPanel.java
│   │   ├── LoginPanel.java
│   │   ├── MainFrame.java
│   │   ├── PatientDetailDialog.java
│   │   ├── PatientPanel.java
│   │   └── WrapLayout.java
│   ├── util/
│   │   └── PdfGenerator.java
│   └── Main.java                 (Main entry point)
├── setup.sql                     (Full database setup script)
└── README.md                     (This file)
```

-----

## 🚀 How to Run

### 1\. Prerequisites

  * **Java JDK 11 or higher** installed and configured.
  * **PostgreSQL Server** installed and running.
  * An IDE like **VS Code** or Eclipse/IntelliJ.

### 2\. Database Setup

1.  Open your PostgreSQL client (e.g., `psql`, DBeaver, pgAdmin).
2.  **Manually create the database** by running:
    ```sql
    CREATE DATABASE hospital_db;
    ```
3.  **Connect to `hospital_db`**.
4.  **Run the entire `setup.sql` script** provided in this project. This will create all the tables, relationships, and dummy data needed to run the application.

### 3\. Project Configuration

1.  **Libraries:** Ensure the `lib` folder contains:
      * `postgresql-42.7.8.jar`
      * `pdfbox-app-3.0.2.jar` (or a similar `app` version that includes all dependencies).
2.  **Database Credentials:** Open `src/dal/DatabaseConnection.java`. Update the `USER` and `PASSWORD` constants to match your PostgreSQL login.
    ```java
    private static final String URL = "jdbc:postgresql://localhost:5432/hospital_db";
    private static final String USER = "postgres"; // Change this if needed
    private static final String PASSWORD = "123456789"; // Change this
    ```
3.  **VS Code:** If using VS Code, your `.vscode/settings.json` file should be:
    ```json
    {
        "java.project.referencedLibraries": [
            "lib/**/*.jar"
        ]
    }
    ```

### 4\. Compile & Run

1.  Compile all the `.java` files in the project.
2.  Run the `src/Main.java` file to start the application.

-----

## 🔑 Login Credentials

Use these accounts (from the `setup.sql` script) to test the different roles:

| Role | Username | Password | Access |
| :--- | :--- | :--- | :--- |
| **Administrator** | `EMP001` | `admin123` | Full access to all tabs. |
| **Doctor** | `EMP003` | `doctor123` | Doctor Portal only. |
| **Doctor** | `EMP007` | `doctor456` | Doctor Portal only. |
| **Head Nurse** | `EMP002` | `nurse123` | Patient Mgt & Bed Mgt tabs. |
| **Receptionist** | `EMP004` | `reception123` | Patient Mgt & Appointments tabs. |
| **Billing Staff** | `EMP005` | `billing123` | Patient Mgt & Billing tabs. |
| **IT Admin** | `EMP008` | `admin456` | Full access to all tabs. |