package dal;

import model.Bed;
import model.Bill;
import model.Doctor;
import model.Patient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataAccess {

    // ========== PATIENT METHODS ==========

    public boolean addPatient(Patient patient) throws SQLException {
        String sql = "INSERT INTO patients (name, age, gender, illness, admitted_date, doctor_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, patient.getName());
            pstmt.setInt(2, patient.getAge());
            pstmt.setString(3, patient.getGender());
            pstmt.setString(4, patient.getIllness());
            pstmt.setDate(5, new java.sql.Date(patient.getAdmittedDate().getTime()));
            pstmt.setInt(6, patient.getDoctorId());
            
            int affectedRows = pstmt.executeUpdate();

            // After adding patient, assign them to the first available bed
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int patientId = generatedKeys.getInt(1);
                        assignFirstAvailableBed(patientId);
                    }
                }
            }
            return affectedRows > 0;
        }
    }

    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT p.*, d.name as doctor_name, b.bed_id " +
                     "FROM patients p " +
                     "LEFT JOIN doctors d ON p.doctor_id = d.doctor_id " +
                     "LEFT JOIN beds b ON p.patient_id = b.patient_id " +
                     "WHERE p.discharged_date IS NULL " +
                     "ORDER BY p.patient_id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Patient patient = new Patient();
                patient.setPatientId(rs.getInt("patient_id"));
                patient.setName(rs.getString("name"));
                patient.setAge(rs.getInt("age"));
                patient.setGender(rs.getString("gender"));
                patient.setIllness(rs.getString("illness"));
                patient.setAdmittedDate(rs.getDate("admitted_date"));
                patient.setDoctorId(rs.getInt("doctor_id"));
                patient.setAssignedDoctorName(rs.getString("doctor_name"));
                patient.setBedId(rs.getInt("bed_id"));
                patients.add(patient);
            }
        }
        return patients;
    }

    public List<Patient> getAdmittedPatients() throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE discharged_date IS NULL";
         try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Patient patient = new Patient();
                patient.setPatientId(rs.getInt("patient_id"));
                patient.setName(rs.getString("name"));
                patients.add(patient);
            }
        }
        return patients;
    }

    public Patient getPatientById(int patientId) throws SQLException {
        Patient patient = null;
        String sql = "SELECT * FROM patients WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    patient = new Patient();
                    patient.setPatientId(rs.getInt("patient_id"));
                    patient.setName(rs.getString("name"));
                    patient.setAdmittedDate(rs.getDate("admitted_date"));
                }
            }
        }
        return patient;
    }

    // ========== DOCTOR METHODS ==========

    public boolean addDoctor(Doctor doctor) throws SQLException {
        String sql = "INSERT INTO doctors (name, specialization, phone, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, doctor.getName());
            pstmt.setString(2, doctor.getSpecialization());
            pstmt.setString(3, doctor.getPhone());
            pstmt.setString(4, doctor.getEmail());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public List<Doctor> getAllDoctors() throws SQLException {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctors ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Doctor doctor = new Doctor();
                doctor.setDoctorId(rs.getInt("doctor_id"));
                doctor.setName(rs.getString("name"));
                doctor.setSpecialization(rs.getString("specialization"));
                doctor.setPhone(rs.getString("phone"));
                doctor.setEmail(rs.getString("email"));
                doctors.add(doctor);
            }
        }
        return doctors;
    }

    // ========== BED METHODS ==========

    public List<Bed> getAllBeds() throws SQLException {
        List<Bed> beds = new ArrayList<>();
        String sql = "SELECT b.bed_id, b.ward, b.status, b.patient_id, p.name as patient_name " +
                     "FROM beds b LEFT JOIN patients p ON b.patient_id = p.patient_id ORDER BY b.bed_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Bed bed = new Bed();
                bed.setBedId(rs.getInt("bed_id"));
                bed.setWard(rs.getString("ward"));
                bed.setStatus(rs.getString("status"));
                bed.setPatientId(rs.getInt("patient_id"));
                bed.setPatientName(rs.getString("patient_name"));
                beds.add(bed);
            }
        }
        return beds;
    }

    private void assignFirstAvailableBed(int patientId) throws SQLException {
        String findBedSql = "SELECT bed_id FROM beds WHERE status = 'Available' LIMIT 1";
        String assignBedSql = "UPDATE beds SET status = 'Occupied', patient_id = ? WHERE bed_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            int bedId = -1;
            // Find an available bed
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(findBedSql)) {
                if (rs.next()) {
                    bedId = rs.getInt("bed_id");
                }
            }

            // If a bed was found, assign it
            if (bedId != -1) {
                try (PreparedStatement pstmt = conn.prepareStatement(assignBedSql)) {
                    pstmt.setInt(1, patientId);
                    pstmt.setInt(2, bedId);
                    pstmt.executeUpdate();
                }
            }
        }
    }

    // ========== BILLING & DISCHARGE METHODS ==========

    public boolean dischargePatient(int patientId, Bill bill) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Insert the bill
            String billSql = "INSERT INTO billing (patient_id, bed_charge, service_charge, doctor_fee, total, bill_date) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmtBill = conn.prepareStatement(billSql)) {
                pstmtBill.setInt(1, bill.getPatientId());
                pstmtBill.setDouble(2, bill.getBedCharge());
                pstmtBill.setDouble(3, bill.getServiceCharge());
                pstmtBill.setDouble(4, bill.getDoctorFee());
                pstmtBill.setDouble(5, bill.getTotal());
                pstmtBill.setDate(6, new java.sql.Date(bill.getBillDate().getTime()));
                pstmtBill.executeUpdate();
            }

            // 2. Free the bed
            String bedSql = "UPDATE beds SET status = 'Available', patient_id = NULL WHERE patient_id = ?";
            try (PreparedStatement pstmtBed = conn.prepareStatement(bedSql)) {
                pstmtBed.setInt(1, patientId);
                pstmtBed.executeUpdate();
            }

            // 3. Update patient's discharge date
            String patientSql = "UPDATE patients SET discharged_date = ? WHERE patient_id = ?";
            try (PreparedStatement pstmtPatient = conn.prepareStatement(patientSql)) {
                pstmtPatient.setDate(1, new java.sql.Date(System.currentTimeMillis()));
                pstmtPatient.setInt(2, patientId); // Corrected line
                pstmtPatient.executeUpdate();
            }
            
            conn.commit(); // Commit transaction
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e; // Re-throw the exception to notify the caller
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Bill> getBillingHistory() throws SQLException {
        List<Bill> billingHistory = new ArrayList<>();
        String sql = "SELECT b.*, p.name as patient_name FROM billing b " +
                     "JOIN patients p ON b.patient_id = p.patient_id " +
                     "ORDER BY b.bill_date DESC, b.bill_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Bill bill = new Bill();
                bill.setBillId(rs.getInt("bill_id"));
                bill.setPatientId(rs.getInt("patient_id"));
                bill.setPatientName(rs.getString("patient_name"));
                bill.setBedCharge(rs.getDouble("bed_charge"));
                bill.setServiceCharge(rs.getDouble("service_charge"));
                bill.setDoctorFee(rs.getDouble("doctor_fee"));
                bill.setTotal(rs.getDouble("total"));
                bill.setBillDate(rs.getDate("bill_date"));
                billingHistory.add(bill);
            }
        }
        return billingHistory;
    }
}