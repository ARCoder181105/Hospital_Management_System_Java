package dal;

import model.Bed;
import model.Bill;
import model.Doctor;
import model.Employee;
import model.Patient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DataAccess {

    // ========== EMPLOYEE/LOGIN METHODS ==========

    public Employee authenticateEmployee(String employeeNumber, String password) throws SQLException {
        String sql = "SELECT * FROM employees WHERE employee_number = ? AND password = ? AND active = true";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, employeeNumber);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Employee employee = new Employee();
                    employee.setEmployeeId(rs.getInt("employee_id"));
                    employee.setEmployeeNumber(rs.getString("employee_number"));
                    employee.setName(rs.getString("name"));
                    employee.setRole(rs.getString("role"));
                    employee.setDepartment(rs.getString("department"));
                    return employee;
                }
            }
        }
        return null;
    }

    public boolean addEmployee(Employee employee) throws SQLException {
        String sql = "INSERT INTO employees (employee_number, password, name, role, department) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, employee.getEmployeeNumber());
            pstmt.setString(2, employee.getPassword());
            pstmt.setString(3, employee.getName());
            pstmt.setString(4, employee.getRole());
            pstmt.setString(5, employee.getDepartment());

            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Employee> getAllEmployees() throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE active = true ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Employee employee = new Employee();
                employee.setEmployeeId(rs.getInt("employee_id"));
                employee.setEmployeeNumber(rs.getString("employee_number"));
                employee.setName(rs.getString("name"));
                employee.setRole(rs.getString("role"));
                employee.setDepartment(rs.getString("department"));
                employees.add(employee);
            }
        }
        return employees;
    }

    // ========== PATIENT METHODS ==========

    public boolean addPatient(Patient patient) throws SQLException {
        String sql = "INSERT INTO patients (name, age, gender, illness, admitted_date, doctor_id, disease_severity, requested_bed_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, patient.getName());
            pstmt.setInt(2, patient.getAge());
            pstmt.setString(3, patient.getGender());
            pstmt.setString(4, patient.getIllness());
            pstmt.setDate(5, new java.sql.Date(patient.getAdmittedDate().getTime()));
            pstmt.setInt(6, patient.getDoctorId());
            pstmt.setString(7, patient.getDiseaseSeverity());
            pstmt.setString(8, patient.getRequestedBedType());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int patientId = generatedKeys.getInt(1);
                        if (!"Mild".equalsIgnoreCase(patient.getDiseaseSeverity())) {
                            boolean bedAssigned = assignAvailableBed(patientId, patient.getRequestedBedType());
                            if (!bedAssigned) {
                                System.err
                                        .println("Warning: No available bed of type " + patient.getRequestedBedType());
                            }
                        }
                    }
                }
            }
            return affectedRows > 0;
        }
    }

    public boolean updatePatient(Patient patient) throws SQLException {
        String sql = "UPDATE patients SET name = ?, age = ?, gender = ?, illness = ?, doctor_id = ?, disease_severity = ?, requested_bed_type = ? WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patient.getName());
            pstmt.setInt(2, patient.getAge());
            pstmt.setString(3, patient.getGender());
            pstmt.setString(4, patient.getIllness());
            pstmt.setInt(5, patient.getDoctorId());
            pstmt.setString(6, patient.getDiseaseSeverity());
            pstmt.setString(7, patient.getRequestedBedType());
            pstmt.setInt(8, patient.getPatientId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deletePatient(int patientId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String bedSql = "UPDATE beds SET status = 'Available', patient_id = NULL WHERE patient_id = ?";
            try (PreparedStatement pstmtBed = conn.prepareStatement(bedSql)) {
                pstmtBed.setInt(1, patientId);
                pstmtBed.executeUpdate();
            }

            String patientSql = "DELETE FROM patients WHERE patient_id = ?";
            try (PreparedStatement pstmtPatient = conn.prepareStatement(patientSql)) {
                pstmtPatient.setInt(1, patientId);
                int affectedRows = pstmtPatient.executeUpdate();
                conn.commit();
                return affectedRows > 0;
            }

        } catch (SQLException e) {
            if (conn != null)
                conn.rollback();
            throw e;
        } finally {
            if (conn != null)
                conn.setAutoCommit(true);
        }
    }

    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT p.*, d.name AS doctor_name, b.bed_id FROM patients p " +
                "LEFT JOIN doctors d ON p.doctor_id = d.doctor_id " +
                "LEFT JOIN beds b ON p.patient_id = b.patient_id " +
                "WHERE p.discharged_date IS NULL ORDER BY p.patient_id";

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
                patient.setDiseaseSeverity(rs.getString("disease_severity"));
                patient.setRequestedBedType(rs.getString("requested_bed_type"));
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
        // This query now joins with beds and gets all patient columns
        String sql = "SELECT p.*, b.price_per_day " +
                "FROM patients p " +
                "LEFT JOIN beds b ON p.patient_id = b.patient_id " +
                "WHERE p.patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    patient = new Patient();
                    patient.setPatientId(rs.getInt("patient_id"));
                    patient.setName(rs.getString("name"));
                    patient.setAge(rs.getInt("age"));
                    patient.setGender(rs.getString("gender"));
                    patient.setIllness(rs.getString("illness"));
                    patient.setDiseaseSeverity(rs.getString("disease_severity"));
                    patient.setAdmittedDate(rs.getDate("admitted_date"));
                    patient.setDoctorId(rs.getInt("doctor_id"));
                    patient.setRequestedBedType(rs.getString("requested_bed_type"));
                    patient.setPricePerDay(rs.getDouble("price_per_day")); // Price from bed
                }
            }
        }
        return patient;
    }

    // ========== DOCTOR METHODS ==========

    public boolean addDoctor(Doctor doctor) throws SQLException {
        String sql = "INSERT INTO doctors (name, specialization, phone, email, consultation_fee, available_days) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, doctor.getName());
            pstmt.setString(2, doctor.getSpecialization());
            pstmt.setString(3, doctor.getPhone());
            pstmt.setString(4, doctor.getEmail());
            pstmt.setDouble(5, doctor.getConsultationFee());
            pstmt.setString(6, doctor.getAvailableDays());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public boolean updateDoctor(Doctor doctor) throws SQLException {
        String sql = "UPDATE doctors SET name = ?, specialization = ?, phone = ?, email = ?, consultation_fee = ?, available_days = ? WHERE doctor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, doctor.getName());
            pstmt.setString(2, doctor.getSpecialization());
            pstmt.setString(3, doctor.getPhone());
            pstmt.setString(4, doctor.getEmail());
            pstmt.setDouble(5, doctor.getConsultationFee());
            pstmt.setString(6, doctor.getAvailableDays());
            pstmt.setInt(7, doctor.getDoctorId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public boolean deleteDoctor(int doctorId) throws SQLException {
        String sql = "DELETE FROM doctors WHERE doctor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
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
                doctor.setConsultationFee(rs.getDouble("consultation_fee"));
                doctor.setAvailableDays(rs.getString("available_days"));
                doctors.add(doctor);
            }
        }
        return doctors;
    }

    // ========== BED METHODS ==========

    public List<Bed> getAllBeds() throws SQLException {
        List<Bed> beds = new ArrayList<>();
        String sql = "SELECT b.*, p.name as patient_name FROM beds b LEFT JOIN patients p ON b.patient_id = p.patient_id ORDER BY b.bed_id";
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
                bed.setFloor(rs.getInt("floor"));
                bed.setBedType(rs.getString("bed_type"));
                bed.setPricePerDay(rs.getDouble("price_per_day"));
                beds.add(bed);
            }
        }
        return beds;
    }

    private boolean assignAvailableBed(int patientId, String bedType) throws SQLException {
        String findBedSql = "SELECT bed_id FROM beds WHERE status = 'Available' AND bed_type = ? LIMIT 1";
        String assignBedSql = "UPDATE beds SET status = 'Occupied', patient_id = ? WHERE bed_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            int bedId = -1;
            try (PreparedStatement findStmt = conn.prepareStatement(findBedSql)) {
                findStmt.setString(1, bedType);
                try (ResultSet rs = findStmt.executeQuery()) {
                    if (rs.next()) {
                        bedId = rs.getInt("bed_id");
                    }
                }
            }

            if (bedId != -1) {
                try (PreparedStatement assignStmt = conn.prepareStatement(assignBedSql)) {
                    assignStmt.setInt(1, patientId);
                    assignStmt.setInt(2, bedId);
                    assignStmt.executeUpdate();
                    return true;
                }
            }
        }
        return false;
    }

    public List<Bed> getBedTypes() throws SQLException {
        List<Bed> bedTypes = new ArrayList<>();
        String sql = "SELECT DISTINCT bed_type, price_per_day FROM beds ORDER BY price_per_day";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Bed bed = new Bed();
                bed.setBedType(rs.getString("bed_type"));
                bed.setPricePerDay(rs.getDouble("price_per_day"));
                bedTypes.add(bed);
            }
        }
        return bedTypes;
    }

    // ========== BILLING & DISCHARGE METHODS ==========

    public boolean dischargePatient(int patientId, Bill bill) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

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

            String bedSql = "UPDATE beds SET status = 'Available', patient_id = NULL WHERE patient_id = ?";
            try (PreparedStatement pstmtBed = conn.prepareStatement(bedSql)) {
                pstmtBed.setInt(1, patientId);
                pstmtBed.executeUpdate();
            }

            String patientSql = "UPDATE patients SET discharged_date = ? WHERE patient_id = ?";
            try (PreparedStatement pstmtPatient = conn.prepareStatement(patientSql)) {
                pstmtPatient.setDate(1, new java.sql.Date(System.currentTimeMillis()));
                pstmtPatient.setInt(2, patientId);
                pstmtPatient.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null)
                conn.rollback();
            throw e;
        } finally {
            if (conn != null)
                conn.setAutoCommit(true);
        }
    }

    public List<Bill> getBillingHistory() throws SQLException {
        List<Bill> billingHistory = new ArrayList<>();
        String sql = "SELECT b.*, p.name as patient_name FROM billing b JOIN patients p ON b.patient_id = p.patient_id ORDER BY b.bill_date DESC, b.bill_id DESC";
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

    public List<Patient> getPatientsByDoctorId(int doctorId) throws SQLException {
        List<Patient> patients = new ArrayList<>();
        // This query is for the Doctor Portal table
        String sql = "SELECT p.patient_id, p.name, p.age, p.illness, p.disease_severity, b.bed_id " +
                "FROM patients p " +
                "LEFT JOIN beds b ON p.patient_id = b.patient_id " +
                "WHERE p.discharged_date IS NULL AND p.doctor_id = ? " +
                "ORDER BY p.name";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, doctorId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Patient patient = new Patient();
                    patient.setPatientId(rs.getInt("patient_id"));
                    patient.setName(rs.getString("name"));
                    patient.setAge(rs.getInt("age"));
                    patient.setIllness(rs.getString("illness"));
                    patient.setDiseaseSeverity(rs.getString("disease_severity"));
                    patient.setBedId(rs.getInt("bed_id"));
                    patients.add(patient);
                }
            }
        }
        return patients;
    }

    public Bed getBedByPatientId(int patientId) throws SQLException {
        String sql = "SELECT * FROM beds WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Bed bed = new Bed();
                    bed.setBedId(rs.getInt("bed_id"));
                    bed.setWard(rs.getString("ward"));
                    bed.setStatus(rs.getString("status"));
                    bed.setPatientId(rs.getInt("patient_id"));
                    bed.setFloor(rs.getInt("floor"));
                    bed.setBedType(rs.getString("bed_type"));
                    bed.setPricePerDay(rs.getDouble("price_per_day"));
                    return bed;
                }
            }
        }
        return null; // No bed assigned
    }

    public Map<Integer, List<Bed>> getBedsGroupedByFloor() throws SQLException {
        Map<Integer, List<Bed>> floorMap = new TreeMap<>();
        List<Bed> allBeds = getAllBeds(); // Re-use the existing method

        for (Bed bed : allBeds) {
            int floor = bed.getFloor();
            // If the map doesn't have this floor yet, create a new list
            floorMap.putIfAbsent(floor, new ArrayList<>());
            // Add the bed to its corresponding floor's list
            floorMap.get(floor).add(bed);
        }
        return floorMap;
    }

}