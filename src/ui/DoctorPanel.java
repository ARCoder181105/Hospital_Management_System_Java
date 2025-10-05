package ui;

import dal.DataAccess;
import model.Doctor;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class DoctorPanel extends JPanel {
    private final DataAccess dataAccess = new DataAccess();
    private JTextField nameField, specializationField, phoneField, emailField;
    private JButton submitButton;
    private JTable doctorTable;
    private DefaultTableModel tableModel;
    private PatientPanel patientPanel; // Reference to update doctor list

    public DoctorPanel(PatientPanel patientPanel) {
        this.patientPanel = patientPanel;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Doctor Management"));
        
        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Specialization (Department):"));
        specializationField = new JTextField();
        formPanel.add(specializationField);

        formPanel.add(new JLabel("Phone:"));
        phoneField = new JTextField();
        formPanel.add(phoneField);

        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        submitButton = new JButton("Add Doctor");
        formPanel.add(new JLabel()); // Placeholder
        formPanel.add(submitButton);

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Available Doctors"));
        String[] columnNames = {"ID", "Name", "Specialization", "Phone", "Email"};
        tableModel = new DefaultTableModel(columnNames, 0);
        doctorTable = new JTable(tableModel);
        tablePanel.add(new JScrollPane(doctorTable), BorderLayout.CENTER);

        add(formPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);

        // Bottom Panel for Refresh button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton refreshButton = new JButton("Refresh List");
        bottomPanel.add(refreshButton);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> loadDoctors());
        
        loadDoctors();

        submitButton.addActionListener(e -> addDoctor());
    }

    private void loadDoctors() {
        try {
            tableModel.setRowCount(0); // Clear existing data
            List<Doctor> doctors = dataAccess.getAllDoctors();
            for (Doctor d : doctors) {
                Object[] row = {d.getDoctorId(), d.getName(), d.getSpecialization(), d.getPhone(), d.getEmail()};
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading doctors: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addDoctor() {
        try {
            // Basic validation
            if (nameField.getText().trim().isEmpty() || specializationField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Doctor Name and Specialization cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Doctor doctor = new Doctor();
            doctor.setName(nameField.getText().trim());
            doctor.setSpecialization(specializationField.getText().trim());
            doctor.setPhone(phoneField.getText().trim());
            doctor.setEmail(emailField.getText().trim());

            if (dataAccess.addDoctor(doctor)) {
                JOptionPane.showMessageDialog(this, "Doctor added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadDoctors();
                patientPanel.loadDoctors(); // Refresh doctor list in PatientPanel
                // Clear fields
                nameField.setText("");
                specializationField.setText("");
                phoneField.setText("");
                emailField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add doctor.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error adding doctor: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}