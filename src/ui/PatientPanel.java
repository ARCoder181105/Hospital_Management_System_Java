package ui;

import dal.DataAccess;
import model.Bed;
import model.Doctor;
import model.Patient;
import javax.swing.*;
// import javax.swing.event.ListSelectionEvent;
// import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class PatientPanel extends JPanel {
    private final DataAccess dataAccess = new DataAccess();
    private JTextField nameField, ageField;
    private JComboBox<String> genderComboBox;
    private JComboBox<Doctor> doctorComboBox;
    
    // UI Components for Illness and Severity
    private JComboBox<String> illnessComboBox;
    private JTextField otherIllnessField;
    private JComboBox<String> severityComboBox;
    private JLabel otherIllnessLabel;
    
    private JComboBox<Bed> bedTypeComboBox; // Bed type dropdown
    private JLabel bedTypeLabel;          // Label for bed type
    
    // CRUD Buttons
    private JButton registerButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    
    private JTable patientTable;
    private DefaultTableModel tableModel;
    private JLabel countLabel;
    
    private int selectedPatientId = -1;
    
    private List<Patient> patientList; // Holds the full list of patient objects

    private final String[] COMMON_ILLNESSES = {
        "Fever", "Flu (Influenza)", "Pneumonia", "Bronchitis",
        "Appendicitis", "Gallstones", "Kidney Stones",
        "Heart Attack", "Stroke", "Diabetes Management", "Other..."
    };

    public PatientPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 245));

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        JSplitPane splitPane = createSplitPane();
        add(splitPane, BorderLayout.CENTER);

        loadDoctors();
        loadBedTypes(); // Load bed types on init
        loadPatients();
        clearForm();
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
        splitPane.setDividerLocation(380); // Increased form height
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setTopComponent(createFormPanel());
        splitPane.setBottomComponent(createTablePanel());
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

        // --- Row 0: Name ---
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createStyledLabel("Name:*"), gbc);
        gbc.gridx = 1;
        nameField = createStyledTextField();
        formPanel.add(nameField, gbc);

        // --- Row 1: Age ---
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createStyledLabel("Age:*"), gbc);
        gbc.gridx = 1;
        ageField = createStyledTextField();
        formPanel.add(ageField, gbc);

        // --- Row 2: Gender ---
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createStyledLabel("Gender:*"), gbc);
        gbc.gridx = 1;
        genderComboBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        genderComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        genderComboBox.setBackground(Color.WHITE);
        formPanel.add(genderComboBox, gbc);

        // --- Row 3: Illness Dropdown ---
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createStyledLabel("Illness:*"), gbc);
        gbc.gridx = 1;
        illnessComboBox = new JComboBox<>(COMMON_ILLNESSES);
        illnessComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        illnessComboBox.setBackground(Color.WHITE);
        formPanel.add(illnessComboBox, gbc);
        
        // --- Row 4: Other Illness Field (Initially hidden) ---
        gbc.gridx = 0; gbc.gridy = 4;
        otherIllnessLabel = createStyledLabel("Specify Illness:");
        formPanel.add(otherIllnessLabel, gbc);
        gbc.gridx = 1;
        otherIllnessField = createStyledTextField();
        formPanel.add(otherIllnessField, gbc);
        
        illnessComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                boolean isOther = "Other...".equals(e.getItem());
                otherIllnessLabel.setVisible(isOther);
                otherIllnessField.setVisible(isOther);
            }
        });

        // --- Row 5: Severity Dropdown ---
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(createStyledLabel("Severity:*"), gbc);
        gbc.gridx = 1;
        severityComboBox = new JComboBox<>(new String[]{"Mild", "Moderate", "Severe"});
        severityComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        severityComboBox.setBackground(Color.WHITE);
        formPanel.add(severityComboBox, gbc);
        
        // --- Row 6: Bed Type Dropdown (NEW) ---
        gbc.gridx = 0; gbc.gridy = 6;
        bedTypeLabel = createStyledLabel("Requested Bed Type:*");
        formPanel.add(bedTypeLabel, gbc);
        gbc.gridx = 1;
        bedTypeComboBox = new JComboBox<>();
        bedTypeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bedTypeComboBox.setBackground(Color.WHITE);
        // Add custom renderer to show price
        bedTypeComboBox.setRenderer(new BedTypeRenderer());
        formPanel.add(bedTypeComboBox, gbc);
        
        // Add item listener to enable/disable bed type based on severity
        severityComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                boolean needsBed = !"Mild".equals(e.getItem());
                bedTypeLabel.setVisible(needsBed);
                bedTypeComboBox.setVisible(needsBed);
            }
        });
        
        // --- Row 7: Doctor Field ---
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(createStyledLabel("Assign Doctor:*"), gbc);
        gbc.gridx = 1;
        doctorComboBox = new JComboBox<>();
        doctorComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        doctorComboBox.setBackground(Color.WHITE);
        formPanel.add(doctorComboBox, gbc);

        // --- Row 8: Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        registerButton = createStyledButton("REGISTER NEW", new Color(60, 179, 113));
        registerButton.addActionListener(e -> registerPatient());
        buttonPanel.add(registerButton);
        updateButton = createStyledButton("UPDATE SELECTED", new Color(255, 140, 0));
        updateButton.addActionListener(e -> updatePatient());
        buttonPanel.add(updateButton);
        deleteButton = createStyledButton("DELETE SELECTED", new Color(220, 53, 69));
        deleteButton.addActionListener(e -> deletePatient());
        buttonPanel.add(deleteButton);
        clearButton = createStyledButton("CLEAR FORM", new Color(108, 117, 125));
        clearButton.addActionListener(e -> clearForm());
        buttonPanel.add(clearButton);
        
        gbc.gridx = 0; gbc.gridy = 8; // Updated gridy
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(buttonPanel, gbc);

        return formPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Admitted Patients (Double-click for details)")); // Updated title
        tablePanel.setBackground(Color.WHITE);

        String[] columnNames = {"ID", "Name", "Age", "Gender", "Illness", "Severity", "Bed Request", "Admitted Date", "Assigned Doctor", "Bed ID"};
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
        
        JTableHeader header = patientTable.getTableHeader();
        header.setBackground(new Color(60, 179, 113));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(patientTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Single-click listener to populate form for editing
        patientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && patientTable.getSelectedRow() != -1) {
                populateFormFromTable();
            }
        });
        
        // Double-click listener to open read-only details
        patientTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openPatientDetails();
                }
            }
        });

        // Refresh button
        JButton refreshButton = createStyledButton("REFRESH LIST", new Color(60, 179, 113));
        refreshButton.addActionListener(e -> {
            loadDoctors();
            loadBedTypes(); // Refresh bed types
            loadPatients();
            clearForm();
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(refreshButton);
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);

        return tablePanel;
    }
    
    private void populateFormFromTable() {
        int selectedRow = patientTable.convertRowIndexToModel(patientTable.getSelectedRow());
        if (selectedRow == -1) return;

        // Get the full patient object from our list
        Patient patient = patientList.get(selectedRow);
        
        selectedPatientId = patient.getPatientId();

        // Populate simple fields
        nameField.setText(patient.getName());
        ageField.setText(String.valueOf(patient.getAge()));
        genderComboBox.setSelectedItem(patient.getGender());
        severityComboBox.setSelectedItem(patient.getDiseaseSeverity());
        
        // Show/hide bed fields based on severity
        boolean needsBed = !"Mild".equals(patient.getDiseaseSeverity());
        bedTypeLabel.setVisible(needsBed);
        bedTypeComboBox.setVisible(needsBed);
        
        // Find and select the correct bed type
        for (int i = 0; i < bedTypeComboBox.getItemCount(); i++) {
            Bed bed = bedTypeComboBox.getItemAt(i);
            if (bed.getBedType().equals(patient.getRequestedBedType())) {
                bedTypeComboBox.setSelectedIndex(i);
                break;
            }
        }

        // Find and select the correct doctor
        for (int i = 0; i < doctorComboBox.getItemCount(); i++) {
            Doctor doctor = doctorComboBox.getItemAt(i);
            if (doctor.getDoctorId() == patient.getDoctorId()) {
                doctorComboBox.setSelectedIndex(i);
                break;
            }
        }
        
        // Set illness combo box (or "Other" field)
        String illness = patient.getIllness();
        boolean foundInDropdown = false;
        for (int i = 0; i < illnessComboBox.getItemCount(); i++) {
            if (illnessComboBox.getItemAt(i).equals(illness)) {
                illnessComboBox.setSelectedIndex(i);
                foundInDropdown = true;
                break;
            }
        }
        
        if (!foundInDropdown) {
            illnessComboBox.setSelectedItem("Other...");
            otherIllnessField.setText(illness);
            otherIllnessLabel.setVisible(true);
            otherIllnessField.setVisible(true);
        } else {
            otherIllnessField.setText("");
            otherIllnessLabel.setVisible(false);
            otherIllnessField.setVisible(false);
        }

        registerButton.setEnabled(false);
        updateButton.setEnabled(true);
        deleteButton.setEnabled(true);
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
            Doctor selectedDoctor = (Doctor) doctorComboBox.getSelectedItem();
            doctorComboBox.removeAllItems();
            List<Doctor> doctors = dataAccess.getAllDoctors();
            for (Doctor doctor : doctors) {
                doctorComboBox.addItem(doctor);
            }
            if (selectedDoctor != null) {
                for (int i = 0; i < doctorComboBox.getItemCount(); i++) {
                    if (doctorComboBox.getItemAt(i).getDoctorId() == selectedDoctor.getDoctorId()) {
                        doctorComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading doctors: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void loadBedTypes() {
        try {
            bedTypeComboBox.removeAllItems();
            List<Bed> bedTypes = dataAccess.getBedTypes();
            for (Bed bed : bedTypes) {
                bedTypeComboBox.addItem(bed);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading bed types: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void loadPatients() {
        try {
            tableModel.setRowCount(0);
            // Store the list
            this.patientList = dataAccess.getAllPatients();
            
            for (Patient p : patientList) {
                Object[] row = {
                    p.getPatientId(),
                    p.getName(),
                    p.getAge(),
                    p.getGender(),
                    p.getIllness(),
                    p.getDiseaseSeverity(),
                    p.getRequestedBedType() != null ? p.getRequestedBedType() : "N/A",
                    p.getAdmittedDate().toString(),
                    p.getAssignedDoctorName() != null ? p.getAssignedDoctorName() : "N/A",
                    p.getBedId() == 0 ? "Unassigned" : p.getBedId()
                };
                tableModel.addRow(row);
            }
            countLabel.setText("Admitted Patients: " + patientList.size());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading patients: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openPatientDetails() {
        int selectedViewRow = patientTable.getSelectedRow();
        if (selectedViewRow == -1) return;
        
        int modelRow = patientTable.convertRowIndexToModel(selectedViewRow);
        
        // Get the full patient object
        Patient selectedPatient = patientList.get(modelRow);
        
        // Open the dialog
        PatientDetailDialog dialog = new PatientDetailDialog((Frame) SwingUtilities.getWindowAncestor(this), selectedPatient);
        dialog.setVisible(true);
    }

    private void clearForm() {
        nameField.setText("");
        ageField.setText("");
        genderComboBox.setSelectedIndex(0);
        illnessComboBox.setSelectedIndex(0);
        severityComboBox.setSelectedIndex(0);
        otherIllnessField.setText("");
        otherIllnessField.setVisible(false);
        otherIllnessLabel.setVisible(false);
        
        if (doctorComboBox.getItemCount() > 0) {
            doctorComboBox.setSelectedIndex(0);
        }
        if (bedTypeComboBox.getItemCount() > 0) {
            bedTypeComboBox.setSelectedIndex(0);
        }
        
        // "Mild" severity hides bed fields by default
        bedTypeLabel.setVisible(false);
        bedTypeComboBox.setVisible(false);
        
        selectedPatientId = -1;
        patientTable.clearSelection();
        
        registerButton.setEnabled(true);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
    
    private boolean validateFields() {
        if (nameField.getText().trim().isEmpty() || ageField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Patient Name and Age cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        try {
            Integer.parseInt(ageField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for age.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if ("Other...".equals(illnessComboBox.getSelectedItem()) && otherIllnessField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify the illness.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (doctorComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please add a doctor first.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Check bed type only if a bed is needed
        if (!"Mild".equals(severityComboBox.getSelectedItem()) && bedTypeComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please add bed types to the database.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }

    private Patient getPatientFromForm() {
        Patient patient = new Patient();
        patient.setName(nameField.getText().trim());
        patient.setAge(Integer.parseInt(ageField.getText().trim()));
        patient.setGender((String) genderComboBox.getSelectedItem());
        patient.setDoctorId(((Doctor) doctorComboBox.getSelectedItem()).getDoctorId());
        
        String illness;
        String selectedIllness = (String) illnessComboBox.getSelectedItem();
        if ("Other...".equals(selectedIllness)) {
            illness = otherIllnessField.getText().trim();
        } else {
            illness = selectedIllness;
        }
        patient.setIllness(illness);
        
        String severity = (String) severityComboBox.getSelectedItem();
        patient.setDiseaseSeverity(severity);
        
        // Get bed request
        if (!"Mild".equals(severity) && bedTypeComboBox.getSelectedItem() != null) {
            patient.setRequestedBedType(((Bed) bedTypeComboBox.getSelectedItem()).getBedType());
        } else {
            patient.setRequestedBedType(null); // No bed requested
        }
        
        return patient;
    }

    private void registerPatient() {
        if (!validateFields()) {
            return;
        }
        
        try {
            Patient patient = getPatientFromForm();
            patient.setAdmittedDate(new Date()); // Set admission date on registration

            if (dataAccess.addPatient(patient)) {
                JOptionPane.showMessageDialog(this, "Patient registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPatients();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to register patient. A bed of the requested type may not be available.", "Registration Error", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error on registration: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updatePatient() {
        if (selectedPatientId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient from the table to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!validateFields()) {
            return;
        }
        
        try {
            Patient patient = getPatientFromForm();
            patient.setPatientId(selectedPatientId); // Set the ID for the WHERE clause

            if (dataAccess.updatePatient(patient)) {
                JOptionPane.showMessageDialog(this, "Patient updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPatients();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update patient.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error updating patient: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deletePatient() {
        if (selectedPatientId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient from the table to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete " + nameField.getText() + "?\n" +
                "This will also free their bed. This action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                if (dataAccess.deletePatient(selectedPatientId)) {
                    JOptionPane.showMessageDialog(this, "Patient deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadPatients();
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete patient.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Could not delete patient. They may have billing records.\n" +
                        "Please discharge the patient via the Billing panel instead.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // Custom renderer for the Bed Type JComboBox
    class BedTypeRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Bed) {
                Bed bed = (Bed) value;
                setText(String.format("%s (₹%.2f/day)", bed.getBedType(), bed.getPricePerDay()));
            } else {
                setText("Select Bed Type");
            }
            return this;
        }
    }
}