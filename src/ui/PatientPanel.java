package ui;

import dal.DataAccess;
import model.ConfigBedType;
import model.ConfigIllness;
import model.Doctor;
import model.Patient;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
    
    private JComboBox<ConfigIllness> illnessComboBox; 
    private JTextField otherIllnessField;
    private JComboBox<String> severityComboBox;
    private JLabel otherIllnessLabel;
    
    private JComboBox<ConfigBedType> bedTypeComboBox; 
    private JLabel bedTypeLabel;
    
    private JButton registerButton, updateButton, deleteButton, clearButton;
    private JTable patientTable;
    private DefaultTableModel tableModel;
    private JLabel countLabel;
    
    private int selectedPatientId = -1;
    private List<Patient> patientList;
    private ConfigIllness otherIllnessOption; // To store the "Other..." object

    public PatientPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 245));

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        JSplitPane splitPane = createSplitPane();
        add(splitPane, BorderLayout.CENTER);

        // Load all dropdown data
        loadDoctors();
        loadBedTypes();
        loadIllnesses(); // This sets otherIllnessOption
        loadPatients();  // This USES otherIllnessOption
        
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
        formPanel.setBorder(BorderFactory.createTitledBorder("Patient Registration"));
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // --- Row 0: Name ---
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createStyledLabel("Name:*"), gbc);
        gbc.gridx = 1; nameField = createStyledTextField(); formPanel.add(nameField, gbc);
        
        // --- Row 1: Age ---
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createStyledLabel("Age:*"), gbc);
        gbc.gridx = 1; ageField = createStyledTextField(); formPanel.add(ageField, gbc);
        
        // --- Row 2: Gender ---
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createStyledLabel("Gender:*"), gbc);
        gbc.gridx = 1; genderComboBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        genderComboBox.setBackground(Color.WHITE); formPanel.add(genderComboBox, gbc);

        // --- Row 3: Illness Dropdown (Now DB-driven) ---
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createStyledLabel("Illness:*"), gbc);
        gbc.gridx = 1;
        illnessComboBox = new JComboBox<>();
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
            if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() != null) {
                // Check the name of the object
                boolean isOther = "Other...".equals(((ConfigIllness)e.getItem()).getIllnessName());
                otherIllnessLabel.setVisible(isOther);
                otherIllnessField.setVisible(isOther);
            }
        });

        // --- Row 5: Severity Dropdown ---
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(createStyledLabel("Severity:*"), gbc);
        gbc.gridx = 1;
        severityComboBox = new JComboBox<>(new String[]{"Mild", "Moderate", "Severe"});
        severityComboBox.setBackground(Color.WHITE);
        formPanel.add(severityComboBox, gbc);
        
        // --- Row 6: Bed Type Dropdown (Now DB-driven) ---
        gbc.gridx = 0; gbc.gridy = 6;
        bedTypeLabel = createStyledLabel("Requested Bed Type:*");
        formPanel.add(bedTypeLabel, gbc);
        gbc.gridx = 1;
        bedTypeComboBox = new JComboBox<>();
        bedTypeComboBox.setBackground(Color.WHITE);
        formPanel.add(bedTypeComboBox, gbc);
        
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
        doctorComboBox.setBackground(Color.WHITE);
        formPanel.add(doctorComboBox, gbc);

        // --- Row 8: Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        registerButton = createStyledButton("REGISTER NEW", new Color(60, 179, 113));
        registerButton.addActionListener(e -> registerPatient());
        updateButton = createStyledButton("UPDATE SELECTED", new Color(255, 140, 0));
        updateButton.addActionListener(e -> updatePatient());
        deleteButton = createStyledButton("DELETE SELECTED", new Color(220, 53, 69));
        deleteButton.addActionListener(e -> deletePatient());
        clearButton = createStyledButton("CLEAR FORM", new Color(108, 117, 125));
        clearButton.addActionListener(e -> clearForm());
        buttonPanel.add(registerButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        
        gbc.gridx = 0; gbc.gridy = 8;
        gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(buttonPanel, gbc);

        return formPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Admitted Patients (Double-click for details)"));
        tablePanel.setBackground(Color.WHITE);

        String[] columnNames = {"ID", "Name", "Age", "Gender", "Illness", "Severity", "Bed Request", "Admitted Date", "Assigned Doctor", "Bed ID"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
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
        
        patientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && patientTable.getSelectedRow() != -1) {
                populateFormFromTable();
            }
        });
        
        patientTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openPatientDetails();
            }
        });

        JButton refreshButton = createStyledButton("REFRESH LIST", new Color(60, 179, 113));
        refreshButton.addActionListener(e -> {
            loadDoctors();
            loadBedTypes();
            loadIllnesses();
            loadPatients();
            clearForm();
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(refreshButton);
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);

        return tablePanel;
    }
    
    // [START] UPDATED METHOD
    private void populateFormFromTable() {
        int selectedRow = patientTable.convertRowIndexToModel(patientTable.getSelectedRow());
        if (selectedRow == -1) return;

        Patient patient = patientList.get(selectedRow);
        selectedPatientId = patient.getPatientId();

        nameField.setText(patient.getName());
        ageField.setText(String.valueOf(patient.getAge()));
        genderComboBox.setSelectedItem(patient.getGender());
        severityComboBox.setSelectedItem(patient.getDiseaseSeverity());
        
        boolean needsBed = !"Mild".equals(patient.getDiseaseSeverity());
        bedTypeLabel.setVisible(needsBed);
        bedTypeComboBox.setVisible(needsBed);
        
        selectComboBoxItem(bedTypeComboBox, patient.getRequestedBedTypeId());
        selectComboBoxItem(doctorComboBox, patient.getDoctorId());
        
        // [FIX #1] Added a null check for otherIllnessOption
        if (otherIllnessOption != null && patient.getIllnessId() == otherIllnessOption.getIllnessId()) {
            illnessComboBox.setSelectedItem(otherIllnessOption);
            otherIllnessField.setText(patient.getOtherIllnessText());
            otherIllnessLabel.setVisible(true);
            otherIllnessField.setVisible(true);
        } else {
            selectComboBoxItem(illnessComboBox, patient.getIllnessId());
            otherIllnessField.setText("");
            otherIllnessLabel.setVisible(false);
            otherIllnessField.setVisible(false);
        }

        registerButton.setEnabled(false);
        updateButton.setEnabled(true);
        deleteButton.setEnabled(true);
    }
    // [END] UPDATED METHOD
    
    private void selectComboBoxItem(JComboBox<?> comboBox, int id) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            Object item = comboBox.getItemAt(i);
            int itemId = -1;
            if (item instanceof Doctor) itemId = ((Doctor) item).getDoctorId();
            else if (item instanceof ConfigBedType) itemId = ((ConfigBedType) item).getBedTypeId();
            else if (item instanceof ConfigIllness) itemId = ((ConfigIllness) item).getIllnessId();
            
            if (itemId == id) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
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
            Object selected = doctorComboBox.getSelectedItem();
            doctorComboBox.removeAllItems();
            dataAccess.getAllDoctors().forEach(doctorComboBox::addItem);
            if (selected != null) doctorComboBox.setSelectedItem(selected);
        } catch (SQLException e) {
            showError("Could not load Doctors: " + e.getMessage());
        }
    }
    
    public void loadBedTypes() {
        try {
            Object selected = bedTypeComboBox.getSelectedItem();
            bedTypeComboBox.removeAllItems();
            dataAccess.getAllBedTypes().forEach(bedTypeComboBox::addItem);
            if (selected != null) bedTypeComboBox.setSelectedItem(selected);
        } catch (SQLException e) {
            showError("Could not load Bed Types: " + e.getMessage());
        }
    }
    
    public void loadIllnesses() {
        try {
            Object selected = illnessComboBox.getSelectedItem();
            illnessComboBox.removeAllItems();
            List<ConfigIllness> illnesses = dataAccess.getAllIllnesses();
            for (ConfigIllness illness : illnesses) {
                illnessComboBox.addItem(illness);
                if ("Other...".equals(illness.getIllnessName())) {
                    otherIllnessOption = illness; // Store this special object
                }
            }
            if (selected != null) illnessComboBox.setSelectedItem(selected);
        } catch (SQLException e) {
            showError("Could not load Illnesses: " + e.getMessage());
        }
    }
    
    // [START] UPDATED METHOD
    public void loadPatients() {
        try {
            tableModel.setRowCount(0);
            this.patientList = dataAccess.getAllPatients();
            
            for (Patient p : patientList) {
                Object[] row = {
                    p.getPatientId(), p.getName(), p.getAge(), p.getGender(),
                    
                    // [FIX #2] Added a null check for otherIllnessOption
                    (otherIllnessOption != null && p.getIllnessId() == otherIllnessOption.getIllnessId()) 
                        ? p.getOtherIllnessText() 
                        : p.getIllnessName(),
                        
                    p.getDiseaseSeverity(),
                    p.getRequestedBedTypeName() != null ? p.getRequestedBedTypeName() : "N/A",
                    p.getAdmittedDate().toString(),
                    p.getAssignedDoctorName() != null ? p.getAssignedDoctorName() : "N/A",
                    p.getBedId() == 0 ? "Unassigned" : p.getBedId()
                };
                tableModel.addRow(row);
            }
            countLabel.setText("Admitted Patients: " + patientList.size());
        } catch (SQLException e) {
            showError("Error loading patients: " + e.getMessage());
        }
    }
    // [END] UPDATED METHOD
    
    private void openPatientDetails() {
        int selectedViewRow = patientTable.getSelectedRow();
        if (selectedViewRow == -1) return;
        int modelRow = patientTable.convertRowIndexToModel(selectedViewRow);
        Patient selectedPatient = patientList.get(modelRow);
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
        if (doctorComboBox.getItemCount() > 0) doctorComboBox.setSelectedIndex(0);
        if (bedTypeComboBox.getItemCount() > 0) bedTypeComboBox.setSelectedIndex(0);
        
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
            showError("Patient Name and Age cannot be empty."); return false;
        }
        try {
            Integer.parseInt(ageField.getText().trim());
        } catch (NumberFormatException ex) {
            showError("Please enter a valid number for age."); return false;
        }
        ConfigIllness selectedIllness = (ConfigIllness) illnessComboBox.getSelectedItem();
        if (selectedIllness == null) {
            showError("Please configure illnesses in the Admin panel."); return false;
        }
        if ("Other...".equals(selectedIllness.getIllnessName()) && otherIllnessField.getText().trim().isEmpty()) {
            showError("Please specify the illness."); return false;
        }
        if (doctorComboBox.getSelectedItem() == null) {
            showError("Please add a doctor first."); return false;
        }
        if (!"Mild".equals(severityComboBox.getSelectedItem()) && bedTypeComboBox.getSelectedItem() == null) {
            showError("Please configure bed types in the Admin panel."); return false;
        }
        return true;
    }

    private Patient getPatientFromForm() {
        Patient patient = new Patient();
        patient.setName(nameField.getText().trim());
        patient.setAge(Integer.parseInt(ageField.getText().trim()));
        patient.setGender((String) genderComboBox.getSelectedItem());
        patient.setDoctorId(((Doctor) doctorComboBox.getSelectedItem()).getDoctorId());
        
        ConfigIllness selectedIllness = (ConfigIllness) illnessComboBox.getSelectedItem();
        patient.setIllnessId(selectedIllness.getIllnessId());
        if ("Other...".equals(selectedIllness.getIllnessName())) {
            patient.setOtherIllnessText(otherIllnessField.getText().trim());
        } else {
            patient.setOtherIllnessText(null);
        }
        
        String severity = (String) severityComboBox.getSelectedItem();
        patient.setDiseaseSeverity(severity);
        
        if (!"Mild".equals(severity) && bedTypeComboBox.getSelectedItem() != null) {
            patient.setRequestedBedTypeId(((ConfigBedType) bedTypeComboBox.getSelectedItem()).getBedTypeId());
        } else {
            patient.setRequestedBedTypeId(0); // 0 or null
        }
        return patient;
    }

    private void registerPatient() {
        if (!validateFields()) return;
        try {
            Patient patient = getPatientFromForm();
            patient.setAdmittedDate(new Date());
            if (dataAccess.addPatient(patient)) {
                JOptionPane.showMessageDialog(this, "Patient registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPatients();
                clearForm();
            } else {
                showError("Failed to register patient. A bed of the requested type may not be available.");
            }
        } catch (SQLException e) {
            showError("Database error on registration: " + e.getMessage());
        }
    }
    
    private void updatePatient() {
        if (selectedPatientId == -1) {
            showError("Please select a patient from the table to update."); return;
        }
        if (!validateFields()) return;
        try {
            Patient patient = getPatientFromForm();
            patient.setPatientId(selectedPatientId);
            if (dataAccess.updatePatient(patient)) {
                JOptionPane.showMessageDialog(this, "Patient updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPatients();
                clearForm();
            } else {
                showError("Failed to update patient.");
            }
        } catch (SQLException e) {
            showError("Database error updating patient: " + e.getMessage());
        }
    }
    
    private void deletePatient() {
        if (selectedPatientId == -1) {
            showError("Please select a patient from the table to delete."); return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete " + nameField.getText() + "?\n" +
                "This will also free their bed. This action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;
        
        try {
            // We must call the version of deletePatient that handles transactions
            dataAccess.deletePatient(selectedPatientId); 
            JOptionPane.showMessageDialog(this, "Patient deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadPatients();
            clearForm();
        } catch (SQLException e) {
            showError("Could not delete patient. They may have billing records.\n" +
                      "Please discharge the patient via the Billing panel instead.");
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}