package ui;

import dal.DataAccess;
import model.Doctor;
import model.Patient;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
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
    private JLabel countLabel;

    public PatientPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 245));

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main Content Panel
        JSplitPane splitPane = createSplitPane();
        add(splitPane, BorderLayout.CENTER);

        loadDoctors();
        loadPatients();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(60, 179, 113));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel titleLabel = new JLabel("PATIENT MANAGEMENT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        countLabel = new JLabel("Admitted Patients: 0");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        countLabel.setForeground(Color.WHITE);
        headerPanel.add(countLabel, BorderLayout.EAST);
        
        return headerPanel;
    }

    private JSplitPane createSplitPane() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        // Form Panel
        JPanel formPanel = createFormPanel();
        splitPane.setTopComponent(formPanel);

        // Table Panel
        JPanel tablePanel = createTablePanel();
        splitPane.setBottomComponent(tablePanel);

        return splitPane;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(60, 179, 113), 2),
            "Patient Registration",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(60, 179, 113)
        ));
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Name Field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createStyledLabel("Name:*"), gbc);
        gbc.gridx = 1;
        nameField = createStyledTextField();
        formPanel.add(nameField, gbc);

        // Age Field
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createStyledLabel("Age:*"), gbc);
        gbc.gridx = 1;
        ageField = createStyledTextField();
        formPanel.add(ageField, gbc);

        // Gender Field
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createStyledLabel("Gender:*"), gbc);
        gbc.gridx = 1;
        genderComboBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        genderComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        genderComboBox.setBackground(Color.WHITE);
        formPanel.add(genderComboBox, gbc);

        // Illness Field
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createStyledLabel("Illness:*"), gbc);
        gbc.gridx = 1;
        illnessField = createStyledTextField();
        formPanel.add(illnessField, gbc);
        
        // Doctor Field
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(createStyledLabel("Assign Doctor:*"), gbc);
        gbc.gridx = 1;
        doctorComboBox = new JComboBox<>();
        doctorComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        doctorComboBox.setBackground(Color.WHITE);
        formPanel.add(doctorComboBox, gbc);

        // Submit Button
        gbc.gridx = 1; gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        submitButton = createStyledButton("REGISTER PATIENT", new Color(60, 179, 113));
        submitButton.addActionListener(e -> registerPatient());
        formPanel.add(submitButton, gbc);

        return formPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Admitted Patients"));
        tablePanel.setBackground(Color.WHITE);

        String[] columnNames = {"ID", "Name", "Age", "Gender", "Illness", "Admitted Date", "Assigned Doctor", "Bed ID"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        patientTable = new JTable(tableModel);
        patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientTable.setRowHeight(25);
        patientTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Style table header
        JTableHeader header = patientTable.getTableHeader();
        header.setBackground(new Color(60, 179, 113));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(patientTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Refresh button
        JButton refreshButton = createStyledButton("REFRESH LIST", new Color(60, 179, 113));
        refreshButton.addActionListener(e -> {
            loadDoctors();
            loadPatients();
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(refreshButton);
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);

        return tablePanel;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return field;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        return button;
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
            tableModel.setRowCount(0);
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
            countLabel.setText("Admitted Patients: " + patients.size());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading patients: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerPatient() {
        try {
            if (nameField.getText().trim().isEmpty() || ageField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Patient Name and Age cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Patient patient = new Patient();
            patient.setName(nameField.getText().trim());
            patient.setAge(Integer.parseInt(ageField.getText().trim()));
            patient.setGender((String) genderComboBox.getSelectedItem());
            patient.setIllness(illnessField.getText().trim());
            patient.setAdmittedDate(new Date());
            
            Doctor selectedDoctor = (Doctor) doctorComboBox.getSelectedItem();
            if (selectedDoctor == null) {
                JOptionPane.showMessageDialog(this, "Please add a doctor first before registering a patient.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            patient.setDoctorId(selectedDoctor.getDoctorId());

            if (dataAccess.addPatient(patient)) {
                JOptionPane.showMessageDialog(this, "Patient registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPatients();
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