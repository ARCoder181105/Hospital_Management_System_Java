package ui;

import dal.DataAccess;
import model.Doctor;
import model.Patient;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class PatientPanel extends JPanel {
    private final DataAccess dataAccess = new DataAccess();
    private JTextField nameField, ageField, illnessField;
    private JComboBox<String> genderComboBox;
    private JComboBox<Doctor> doctorComboBox;
    private JButton submitButton;
    private JTable patientTable;
    private DefaultTableModel tableModel;

    public PatientPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Patient Registration"));

        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Age:"));
        ageField = new JTextField();
        formPanel.add(ageField);

        formPanel.add(new JLabel("Gender:"));
        genderComboBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        formPanel.add(genderComboBox);

        formPanel.add(new JLabel("Illness:"));
        illnessField = new JTextField();
        formPanel.add(illnessField);
        
        formPanel.add(new JLabel("Assign Doctor:"));
        doctorComboBox = new JComboBox<>();
        formPanel.add(doctorComboBox);

        submitButton = new JButton("Register Patient");
        formPanel.add(new JLabel()); // Placeholder
        formPanel.add(submitButton);

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Admitted Patients"));
        String[] columnNames = {"ID", "Name", "Age", "Gender", "Illness", "Admitted Date", "Assigned Doctor", "Bed ID"};
        tableModel = new DefaultTableModel(columnNames, 0);
        patientTable = new JTable(tableModel);
        tablePanel.add(new JScrollPane(patientTable), BorderLayout.CENTER);

        add(formPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);

        // Bottom Panel for Refresh button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton refreshButton = new JButton("Refresh Lists");
        bottomPanel.add(refreshButton);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> {
            loadDoctors();
            loadPatients();
        });

        // Load initial data
        loadDoctors();
        loadPatients();

        // Add action listener
        submitButton.addActionListener(e -> registerPatient());
    }

    public void loadDoctors() {
        try {
            doctorComboBox.removeAllItems();
            List<Doctor> doctors = dataAccess.getAllDoctors();
            for (Doctor doctor : doctors) {
                doctorComboBox.addItem(doctor);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading doctors: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadPatients() {
        try {
            tableModel.setRowCount(0); // Clear existing data
            List<Patient> patients = dataAccess.getAllPatients();
            for (Patient p : patients) {
                Object[] row = {
                    p.getPatientId(),
                    p.getName(),
                    p.getAge(),
                    p.getGender(),
                    p.getIllness(),
                    p.getAdmittedDate().toString(),
                    p.getAssignedDoctorName() != null ? p.getAssignedDoctorName() : "N/A",
                    p.getBedId() == 0 ? "Unassigned" : p.getBedId()
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading patients: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerPatient() {
        try {
            // Basic validation
            if (nameField.getText().trim().isEmpty() || ageField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Patient Name and Age cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Patient patient = new Patient();
            patient.setName(nameField.getText().trim());
            patient.setAge(Integer.parseInt(ageField.getText().trim()));
            patient.setGender((String) genderComboBox.getSelectedItem());
            patient.setIllness(illnessField.getText().trim());
            patient.setAdmittedDate(new Date()); // Set current date as admitted date
            
            Doctor selectedDoctor = (Doctor) doctorComboBox.getSelectedItem();
            if (selectedDoctor == null) {
                JOptionPane.showMessageDialog(this, "Please add a doctor first before registering a patient.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            patient.setDoctorId(selectedDoctor.getDoctorId());

            if (dataAccess.addPatient(patient)) {
                JOptionPane.showMessageDialog(this, "Patient registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPatients(); // Refresh the table
                // Clear fields
                nameField.setText("");
                ageField.setText("");
                illnessField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to register patient. A bed may not be available.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for age.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error on registration: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}