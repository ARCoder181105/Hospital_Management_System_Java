package dal;

import model.*; // Import all models
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Date; // Import java.util.Date

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

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
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
        String sql = "INSERT INTO patients (name, age, gender, admitted_date, doctor_id, disease_severity, requested_bed_type_id, illness_id, other_illness_text) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, patient.getName());
            pstmt.setInt(2, patient.getAge());
            pstmt.setString(3, patient.getGender());
            pstmt.setDate(4, new java.sql.Date(patient.getAdmittedDate().getTime()));
            pstmt.setInt(5, patient.getDoctorId());
            pstmt.setString(6, patient.getDiseaseSeverity());

            if (patient.getRequestedBedTypeId() == 0) {
                pstmt.setNull(7, Types.INTEGER);
            } else {
                pstmt.setInt(7, patient.getRequestedBedTypeId());
            }

            pstmt.setInt(8, patient.getIllnessId());
            pstmt.setString(9, patient.getOtherIllnessText());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int patientId = generatedKeys.getInt(1);
                        if (!"Mild".equals(patient.getDiseaseSeverity())) {
                            assignAvailableBed(patientId, patient.getRequestedBedTypeId());
                        }
                    }
                }
            }
            return affectedRows > 0;
        }
    }

    public boolean updatePatient(Patient patient) throws SQLException {
        String sql = "UPDATE patients SET name = ?, age = ?, gender = ?, doctor_id = ?, disease_severity = ?, requested_bed_type_id = ?, illness_id = ?, other_illness_text = ? WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patient.getName());
            pstmt.setInt(2, patient.getAge());
            pstmt.setString(3, patient.getGender());
            pstmt.setInt(4, patient.getDoctorId());
            pstmt.setString(5, patient.getDiseaseSeverity());

            if (patient.getRequestedBedTypeId() == 0) {
                pstmt.setNull(6, Types.INTEGER);
            } else {
                pstmt.setInt(6, patient.getRequestedBedTypeId());
            }

            pstmt.setInt(7, patient.getIllnessId());
            pstmt.setString(8, patient.getOtherIllnessText());
            pstmt.setInt(9, patient.getPatientId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
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
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT p.*, d.name as doctor_name, b.bed_id, " +
                "ci.illness_name, cbt.bed_type_name " +
                "FROM patients p " +
                "LEFT JOIN doctors d ON p.doctor_id = d.doctor_id " +
                "LEFT JOIN beds b ON p.patient_id = b.patient_id " +
                "LEFT JOIN config_illnesses ci ON p.illness_id = ci.illness_id " +
                "LEFT JOIN config_bed_types cbt ON p.requested_bed_type_id = cbt.bed_type_id " +
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
                patient.setAdmittedDate(rs.getDate("admitted_date"));
                patient.setDoctorId(rs.getInt("doctor_id"));
                patient.setDiseaseSeverity(rs.getString("disease_severity"));
                patient.setBedId(rs.getInt("bed_id"));

                patient.setIllnessId(rs.getInt("illness_id"));
                patient.setOtherIllnessText(rs.getString("other_illness_text"));
                patient.setRequestedBedTypeId(rs.getInt("requested_bed_type_id"));

                // Joined fields
                patient.setAssignedDoctorName(rs.getString("doctor_name"));
                patient.setIllnessName(rs.getString("illness_name"));
                patient.setRequestedBedTypeName(rs.getString("bed_type_name"));

                patients.add(patient);
            }
        }
        return patients;
    }

    // [START] UPDATED/FIXED: getPatientById
    public Patient getPatientById(int patientId) throws SQLException {
        Patient patient = null;

        // [FIX] The query must join through `beds` (aliased b) to `config_bed_types`
        // (aliased cbt)
        // to get the price. The price is now at `cbt.price_per_day`.
        String sql = "SELECT p.*, cbt.price_per_day, ci.illness_name " +
                "FROM patients p " +
                "LEFT JOIN beds b ON p.patient_id = b.patient_id " +
                "LEFT JOIN config_bed_types cbt ON b.bed_type_id = cbt.bed_type_id " + // Join from beds to
                                                                                       // config_bed_types
                "LEFT JOIN config_illnesses ci ON p.illness_id = ci.illness_id " +
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
                    patient.setAdmittedDate(rs.getDate("admitted_date"));
                    patient.setDoctorId(rs.getInt("doctor_id"));
                    patient.setDiseaseSeverity(rs.getString("disease_severity"));

                    patient.setIllnessId(rs.getInt("illness_id"));
                    patient.setOtherIllnessText(rs.getString("other_illness_text"));
                    patient.setRequestedBedTypeId(rs.getInt("requested_bed_type_id"));

                    // Joined fields
                    patient.setPricePerDay(rs.getDouble("price_per_day")); // This now comes from cbt
                    patient.setIllnessName(rs.getString("illness_name"));
                }
            }
        }
        return patient;
    }
    // [END] UPDATED/FIXED: getPatientById

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

    public List<Patient> getPatientsByDoctorId(int doctorId) throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT p.patient_id, p.name, p.age, p.disease_severity, b.bed_id, " +
                "ci.illness_name, p.other_illness_text " +
                "FROM patients p " +
                "LEFT JOIN beds b ON p.patient_id = b.patient_id " +
                "LEFT JOIN config_illnesses ci ON p.illness_id = ci.illness_id " +
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
                    patient.setDiseaseSeverity(rs.getString("disease_severity"));
                    patient.setBedId(rs.getInt("bed_id"));
                    patient.setIllnessName(rs.getString("illness_name"));
                    patient.setOtherIllnessText(rs.getString("other_illness_text"));
                    patients.add(patient);
                }
            }
        }
        return patients;
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

    public Doctor getDoctorById(int doctorId) throws SQLException {
        String sql = "SELECT * FROM doctors WHERE doctor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, doctorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Doctor doctor = new Doctor();
                    doctor.setDoctorId(rs.getInt("doctor_id"));
                    doctor.setName(rs.getString("name"));
                    doctor.setSpecialization(rs.getString("specialization"));
                    doctor.setPhone(rs.getString("phone"));
                    doctor.setEmail(rs.getString("email"));
                    doctor.setConsultationFee(rs.getDouble("consultation_fee"));
                    doctor.setAvailableDays(rs.getString("available_days"));
                    return doctor;
                }
            }
        }
        return null; // Not found
    }

    // ========== BED METHODS ==========

    public List<Bed> getAllBeds() throws SQLException {
        List<Bed> beds = new ArrayList<>();
        String sql = "SELECT b.*, p.name as patient_name, cbt.bed_type_name, cbt.price_per_day " +
                "FROM beds b " +
                "LEFT JOIN patients p ON b.patient_id = p.patient_id " +
                "JOIN config_bed_types cbt ON b.bed_type_id = cbt.bed_type_id " +
                "ORDER BY b.bed_id";
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
                bed.setBedTypeId(rs.getInt("bed_type_id"));

                // Joined fields
                bed.setBedTypeName(rs.getString("bed_type_name"));
                bed.setPricePerDay(rs.getDouble("price_per_day"));

                beds.add(bed);
            }
        }
        return beds;
    }

    public Map<Integer, List<Bed>> getBedsGroupedByFloor() throws SQLException {
        Map<Integer, List<Bed>> floorMap = new TreeMap<>();
        List<Bed> allBeds = getAllBeds();

        for (Bed bed : allBeds) {
            int floor = bed.getFloor();
            floorMap.putIfAbsent(floor, new ArrayList<>());
            floorMap.get(floor).add(bed);
        }
        return floorMap;
    }

    private boolean assignAvailableBed(int patientId, int bedTypeId) throws SQLException {
        String findBedSql = "SELECT bed_id FROM beds WHERE status = 'Available' AND bed_type_id = ? LIMIT 1";
        String assignBedSql = "UPDATE beds SET status = 'Occupied', patient_id = ? WHERE bed_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            int bedId = -1;
            try (PreparedStatement findStmt = conn.prepareStatement(findBedSql)) {
                findStmt.setInt(1, bedTypeId);
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

    public List<ConfigBedType> getAllBedTypes() throws SQLException {
        List<ConfigBedType> bedTypes = new ArrayList<>();
        String sql = "SELECT * FROM config_bed_types ORDER BY price_per_day";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ConfigBedType bed = new ConfigBedType();
                bed.setBedTypeId(rs.getInt("bed_type_id"));
                bed.setBedTypeName(rs.getString("bed_type_name"));
                bed.setPricePerDay(rs.getDouble("price_per_day"));
                bedTypes.add(bed);
            }
        }
        return bedTypes;
    }

    public Bed getBedByPatientId(int patientId) throws SQLException {
        String sql = "SELECT b.*, cbt.bed_type_name, cbt.price_per_day " +
                "FROM beds b " +
                "JOIN config_bed_types cbt ON b.bed_type_id = cbt.bed_type_id " +
                "WHERE b.patient_id = ?";
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
                    bed.setBedTypeId(rs.getInt("bed_type_id"));

                    // Joined fields
                    bed.setBedTypeName(rs.getString("bed_type_name"));
                    bed.setPricePerDay(rs.getDouble("price_per_day"));
                    return bed;
                }
            }
        }
        return null;
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
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
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

    // ========== DASHBOARD METHODS ==========

    public Map<String, Long> getDashboardStats() throws SQLException {
        Map<String, Long> stats = new HashMap<>();
        String sql = "SELECT " +
                "  (SELECT COUNT(*) FROM patients WHERE discharged_date IS NULL) AS active_patients, " +
                "  (SELECT COUNT(*) FROM beds WHERE status = 'Available') AS available_beds, " +
                "  (SELECT COALESCE(SUM(total), 0) FROM billing) AS total_revenue, " +
                "  (SELECT COUNT(*) FROM doctors) AS total_doctors";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                stats.put("active_patients", rs.getLong("active_patients"));
                stats.put("available_beds", rs.getLong("available_beds"));
                stats.put("total_revenue", rs.getLong("total_revenue"));
                stats.put("total_doctors", rs.getLong("total_doctors"));
            }
        }
        return stats;
    }

    // ========== CONFIGURATION PANEL METHODS ==========

    public boolean addBedType(ConfigBedType bedType) throws SQLException {
        String sql = "INSERT INTO config_bed_types (bed_type_name, price_per_day) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bedType.getBedTypeName());
            pstmt.setDouble(2, bedType.getPricePerDay());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean updateBedType(ConfigBedType bedType) throws SQLException {
        String sql = "UPDATE config_bed_types SET bed_type_name = ?, price_per_day = ? WHERE bed_type_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bedType.getBedTypeName());
            pstmt.setDouble(2, bedType.getPricePerDay());
            pstmt.setInt(3, bedType.getBedTypeId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<ConfigIllness> getAllIllnesses() throws SQLException {
        List<ConfigIllness> illnesses = new ArrayList<>();
        String sql = "SELECT * FROM config_illnesses ORDER BY illness_name";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ConfigIllness illness = new ConfigIllness();
                illness.setIllnessId(rs.getInt("illness_id"));
                illness.setIllnessName(rs.getString("illness_name"));
                illnesses.add(illness);
            }
        }
        return illnesses;
    }

    public boolean addIllness(ConfigIllness illness) throws SQLException {
        String sql = "INSERT INTO config_illnesses (illness_name) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, illness.getIllnessName());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean updateIllness(ConfigIllness illness) throws SQLException {
        String sql = "UPDATE config_illnesses SET illness_name = ? WHERE illness_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, illness.getIllnessName());
            pstmt.setInt(2, illness.getIllnessId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteIllness(int illnessId) throws SQLException {
        String sql = "DELETE FROM config_illnesses WHERE illness_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, illnessId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteBedType(int bedTypeId) throws SQLException {
        // The FOREIGN KEY constraint on the 'beds' table will
        // automatically prevent deletion if any bed is still using this type.
        String sql = "DELETE FROM config_bed_types WHERE bed_type_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bedTypeId);
            return pstmt.executeUpdate() > 0;
        }
    }
    // ========== APPOINTMENT METHODS ==========

    /**
     * Gets all scheduled (not cancelled) appointments for a specific date.
     */
    public List<Appointment> getScheduledAppointments(Date date) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.*, p.name as patient_name, d.name as doctor_name " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                "WHERE a.appointment_date = ? " +
                "ORDER BY a.appointment_time";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, new java.sql.Date(date.getTime()));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Appointment appt = new Appointment();
                    appt.setAppointmentId(rs.getInt("appointment_id"));
                    appt.setAppointmentTime(rs.getString("appointment_time"));
                    appt.setPatientName(rs.getString("patient_name"));
                    appt.setDoctorName(rs.getString("doctor_name"));
                    appt.setStatus(rs.getString("status"));
                    appointments.add(appt);
                }
            }
        }
        return appointments;
    }

    /**
     * Gets a list of booked time slots for a specific doctor on a specific date.
     */
    public List<String> getBookedSlots(int doctorId, Date date) throws SQLException {
        List<String> slots = new ArrayList<>();
        String sql = "SELECT appointment_time FROM appointments " +
                "WHERE doctor_id = ? AND appointment_date = ? AND status = 'Scheduled'";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, doctorId);
            pstmt.setDate(2, new java.sql.Date(date.getTime()));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    slots.add(rs.getString("appointment_time"));
                }
            }
        }
        return slots;
    }

    /**
     * Adds a new appointment to the database.
     */
    public boolean addAppointment(Appointment appt) throws SQLException {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, appt.getPatientId());
            pstmt.setInt(2, appt.getDoctorId());
            pstmt.setDate(3, new java.sql.Date(appt.getAppointmentDate().getTime()));
            pstmt.setString(4, appt.getAppointmentTime());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Cancels an existing appointment by setting its status.
     */
    public boolean cancelAppointment(int appointmentId) throws SQLException {
        String sql = "UPDATE appointments SET status = 'Cancelled' WHERE appointment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, appointmentId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Gets all scheduled appointments for a specific doctor on a specific date.
     * (Used by the Doctor Portal)
     */
    public List<Appointment> getAppointmentsByDoctorAndDate(int doctorId, Date date) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.*, p.name as patient_name " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "WHERE a.doctor_id = ? AND a.appointment_date = ? AND a.status = 'Scheduled' " +
                "ORDER BY a.appointment_time";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, doctorId);
            pstmt.setDate(2, new java.sql.Date(date.getTime()));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Appointment appt = new Appointment();
                    appt.setAppointmentId(rs.getInt("appointment_id"));
                    appt.setAppointmentTime(rs.getString("appointment_time"));
                    appt.setPatientName(rs.getString("patient_name"));
                    appt.setStatus(rs.getString("status"));
                    appointments.add(appt);
                }
            }
        }
        return appointments;
    }
}