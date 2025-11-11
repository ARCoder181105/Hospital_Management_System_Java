package ui;

import dal.DataAccess;
import model.Doctor;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DoctorPanel extends JPanel {
    private final DataAccess dataAccess = new DataAccess();
    private JTextField nameField, specializationField, phoneField, emailField;
    
    // [START] NEW UI FIELDS
    private JTextField feeField;
    private JCheckBox monCheck, tueCheck, wedCheck, thuCheck, friCheck, satCheck, sunCheck;
    private List<JCheckBox> dayCheckBoxes;
    // [END] NEW UI FIELDS
    
    private JButton addButton, updateButton, deleteButton, clearButton;
    private JTable doctorTable;
    private DefaultTableModel tableModel;
    private PatientPanel patientPanel;
    private JLabel countLabel;
    private int selectedDoctorId = -1;
    
    private List<Doctor> doctorList;

    public DoctorPanel(PatientPanel patientPanel) {
        this.patientPanel = patientPanel;
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 245));
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createSplitPane(), BorderLayout.CENTER);
        loadDoctors();
        clearForm();
    }

    private JPanel createHeaderPanel() {
        // ... (unchanged) ...
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 60));
        JLabel titleLabel = new JLabel("DOCTOR MANAGEMENT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        countLabel = new JLabel("Total Doctors: 0");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        countLabel.setForeground(Color.WHITE);
        headerPanel.add(countLabel, BorderLayout.EAST);
        return headerPanel;
    }

    private JSplitPane createSplitPane() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300); // Increased form height
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setTopComponent(createFormPanel());
        splitPane.setBottomComponent(createTablePanel());
        return splitPane;
    }

    // [START] UPDATED METHOD: createFormPanel
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
            "Doctor Details",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(70, 130, 180)
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

        // --- Row 1: Specialization ---
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createStyledLabel("Specialization:*"), gbc);
        gbc.gridx = 1;
        specializationField = createStyledTextField();
        formPanel.add(specializationField, gbc);

        // --- Row 2: Phone ---
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createStyledLabel("Phone:"), gbc);
        gbc.gridx = 1;
        phoneField = createStyledTextField();
        formPanel.add(phoneField, gbc);

        // --- Row 3: Email ---
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createStyledLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailField = createStyledTextField();
        formPanel.add(emailField, gbc);

        // --- Row 4: Consultation Fee (NEW) ---
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(createStyledLabel("Consultation Fee (Rs.):"), gbc);
        gbc.gridx = 1;
        feeField = createStyledTextField();
        formPanel.add(feeField, gbc);
        
        // --- Row 5: Available Days (NEW) ---
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(createStyledLabel("Available Days:"), gbc);
        gbc.gridx = 1;
        formPanel.add(createDayCheckboxPanel(), gbc); // Add the checkbox panel

        // --- Row 6: Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        addButton = createStyledButton("ADD NEW", new Color(34, 139, 34));
        addButton.addActionListener(e -> addDoctor());
        buttonPanel.add(addButton);
        updateButton = createStyledButton("UPDATE SELECTED", new Color(255, 140, 0));
        updateButton.addActionListener(e -> updateDoctor());
        buttonPanel.add(updateButton);
        deleteButton = createStyledButton("DELETE SELECTED", new Color(220, 53, 69));
        deleteButton.addActionListener(e -> deleteDoctor());
        buttonPanel.add(deleteButton);
        clearButton = createStyledButton("CLEAR FORM", new Color(108, 117, 125));
        clearButton.addActionListener(e -> clearForm());
        buttonPanel.add(clearButton);

        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(buttonPanel, gbc);

        return formPanel;
    }
    // [END] UPDATED METHOD: createFormPanel

    // [START] NEW METHOD: createDayCheckboxPanel
    /**
     * Helper method to create the panel of checkboxes for available days.
     */
    private JPanel createDayCheckboxPanel() {
        JPanel dayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        dayPanel.setBackground(Color.WHITE);
        
        dayCheckBoxes = new ArrayList<>();
        
        monCheck = new JCheckBox("Mon");
        dayCheckBoxes.add(monCheck);
        tueCheck = new JCheckBox("Tue");
        dayCheckBoxes.add(tueCheck);
        wedCheck = new JCheckBox("Wed");
        dayCheckBoxes.add(wedCheck);
        thuCheck = new JCheckBox("Thu");
        dayCheckBoxes.add(thuCheck);
        friCheck = new JCheckBox("Fri");
        dayCheckBoxes.add(friCheck);
        satCheck = new JCheckBox("Sat");
        dayCheckBoxes.add(satCheck);
        sunCheck = new JCheckBox("Sun");
        dayCheckBoxes.add(sunCheck);
        
        for (JCheckBox cb : dayCheckBoxes) {
            cb.setBackground(Color.WHITE);
            cb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            dayPanel.add(cb);
        }
        
        return dayPanel;
    }
    // [END] NEW METHOD: createDayCheckboxPanel

    // [START] UPDATED METHOD: createTablePanel
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Available Doctors (Double-click for details)"));
        tablePanel.setBackground(Color.WHITE);

        // --- NEW COLUMNS ---
        String[] columnNames = {"ID", "Name", "Specialization", "Fee (Rs.)", "Available Days"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        doctorTable = new JTable(tableModel);
        doctorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        doctorTable.setRowHeight(25);
        doctorTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JTableHeader header = doctorTable.getTableHeader();
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(doctorTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        doctorTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && doctorTable.getSelectedRow() != -1) {
                populateFormFromTable();
            }
        });
        
        doctorTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openDoctorDetails();
                }
            }
        });

        JButton refreshButton = createStyledButton("REFRESH LIST", new Color(70, 130, 180));
        refreshButton.addActionListener(e -> { loadDoctors(); clearForm(); });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(refreshButton);
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);

        return tablePanel;
    }
    // [END] UPDATED METHOD: createTablePanel
    
    // [START] UPDATED METHOD: populateFormFromTable
    private void populateFormFromTable() {
        int selectedRow = doctorTable.convertRowIndexToModel(doctorTable.getSelectedRow());
        if (selectedRow == -1) return;

        // Get the full Doctor object from our list
        Doctor doctor = doctorList.get(selectedRow);

        selectedDoctorId = doctor.getDoctorId();

        // Populate form fields
        nameField.setText(doctor.getName());
        specializationField.setText(doctor.getSpecialization());
        phoneField.setText(doctor.getPhone());
        emailField.setText(doctor.getEmail());
        feeField.setText(String.valueOf(doctor.getConsultationFee()));
        setCheckboxesFromAvailableDays(doctor.getAvailableDays());

        addButton.setEnabled(false);
        updateButton.setEnabled(true);
        deleteButton.setEnabled(true);
    }
    // [END] UPDATED METHOD: populateFormFromTable

    private JLabel createStyledLabel(String text) { /* ... (unchanged) ... */ return new JLabel(text); }
    private JTextField createStyledTextField() { /* ... (unchanged) ... */ return new JTextField(); }
    private JButton createStyledButton(String text, Color color) { /* ... (unchanged) ... */ return new JButton(text); }

    // [START] UPDATED METHOD: loadDoctors
    public void loadDoctors() {
        try {
            tableModel.setRowCount(0);
            this.doctorList = dataAccess.getAllDoctors();
            
            for (Doctor d : doctorList) {
                Object[] row = {
                    d.getDoctorId(),
                    d.getName(),
                    d.getSpecialization(),
                    String.format("%.2f", d.getConsultationFee()),
                    d.getAvailableDays() != null ? d.getAvailableDays() : ""
                };
                tableModel.addRow(row);
            }
            countLabel.setText("Total Doctors: " + doctorList.size());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading doctors: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    // [END] UPDATED METHOD: loadDoctors
    
    private void openDoctorDetails() {
        int selectedViewRow = doctorTable.getSelectedRow();
        if (selectedViewRow == -1) return;
        
        int modelRow = doctorTable.convertRowIndexToModel(selectedViewRow);
        Doctor selectedDoctor = doctorList.get(modelRow);
        
        DoctorDetailDialog dialog = new DoctorDetailDialog((Frame) SwingUtilities.getWindowAncestor(this), selectedDoctor, dataAccess);
        dialog.setVisible(true);
    }

    private String getPrefixedDoctorName() {
        String name = nameField.getText().trim();
        if (!name.isEmpty() && !name.toLowerCase().startsWith("dr.")) {
            name = "Dr. " + name;
        }
        return name;
    }
    
    // [START] NEW METHOD: getAvailableDaysFromCheckboxes
    private String getAvailableDaysFromCheckboxes() {
        List<String> days = new ArrayList<>();
        if (monCheck.isSelected()) days.add("Mon");
        if (tueCheck.isSelected()) days.add("Tue");
        if (wedCheck.isSelected()) days.add("Wed");
        if (thuCheck.isSelected()) days.add("Thu");
        if (friCheck.isSelected()) days.add("Fri");
        if (satCheck.isSelected()) days.add("Sat");
        if (sunCheck.isSelected()) days.add("Sun");
        return String.join(",", days);
    }
    // [END] NEW METHOD: getAvailableDaysFromCheckboxes

    // [START] NEW METHOD: setCheckboxesFromAvailableDays
    private void setCheckboxesFromAvailableDays(String daysString) {
        // First, clear all
        for (JCheckBox cb : dayCheckBoxes) {
            cb.setSelected(false);
        }
        
        if (daysString == null || daysString.trim().isEmpty()) {
            return;
        }
        
        String[] days = daysString.split(",");
        for (String day : days) {
            if ("Mon".equals(day)) monCheck.setSelected(true);
            else if ("Tue".equals(day)) tueCheck.setSelected(true);
            else if ("Wed".equals(day)) wedCheck.setSelected(true);
            else if ("Thu".equals(day)) thuCheck.setSelected(true);
            else if ("Fri".equals(day)) friCheck.setSelected(true);
            else if ("Sat".equals(day)) satCheck.setSelected(true);
            else if ("Sun".equals(day)) sunCheck.setSelected(true);
        }
    }
    // [END] NEW METHOD: setCheckboxesFromAvailableDays

    // [START] UPDATED METHOD: addDoctor
    private void addDoctor() {
        try {
            if (nameField.getText().trim().isEmpty() || specializationField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Doctor Name and Specialization cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            double fee = 0.0;
            if (!feeField.getText().trim().isEmpty()) {
                try {
                    fee = Double.parseDouble(feeField.getText().trim());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid number for the fee.", "Input Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            Doctor doctor = new Doctor();
            doctor.setName(getPrefixedDoctorName());
            doctor.setSpecialization(specializationField.getText().trim());
            doctor.setPhone(phoneField.getText().trim());
            doctor.setEmail(emailField.getText().trim());
            doctor.setConsultationFee(fee);
            doctor.setAvailableDays(getAvailableDaysFromCheckboxes());

            if (dataAccess.addDoctor(doctor)) {
                JOptionPane.showMessageDialog(this, "Doctor added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadDoctors();
                patientPanel.loadDoctors();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add doctor.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error adding doctor: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    // [END] UPDATED METHOD: addDoctor
    
    // [START] UPDATED METHOD: updateDoctor
    private void updateDoctor() {
        if (selectedDoctorId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a doctor from the table to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            if (nameField.getText().trim().isEmpty() || specializationField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Doctor Name and Specialization cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            double fee = 0.0;
            if (!feeField.getText().trim().isEmpty()) {
                try {
                    fee = Double.parseDouble(feeField.getText().trim());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid number for the fee.", "Input Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            Doctor doctor = new Doctor();
            doctor.setDoctorId(selectedDoctorId);
            doctor.setName(getPrefixedDoctorName());
            doctor.setSpecialization(specializationField.getText().trim());
            doctor.setPhone(phoneField.getText().trim());
            doctor.setEmail(emailField.getText().trim());
            doctor.setConsultationFee(fee);
            doctor.setAvailableDays(getAvailableDaysFromCheckboxes());

            if (dataAccess.updateDoctor(doctor)) {
                JOptionPane.showMessageDialog(this, "Doctor updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadDoctors();
                patientPanel.loadDoctors();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update doctor.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error updating doctor: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    // [END] UPDATED METHOD: updateDoctor
    
    private void deleteDoctor() {
        // ... (unchanged) ...
    }
    
    // [START] UPDATED METHOD: clearForm
    private void clearForm() {
        nameField.setText("");
        specializationField.setText("");
        phoneField.setText("");
        emailField.setText("");
        feeField.setText(""); // Clear new field
        setCheckboxesFromAvailableDays(null); // Clear checkboxes
        
        selectedDoctorId = -1;
        doctorTable.clearSelection();
        
        addButton.setEnabled(true);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
    // [END] UPDATED METHOD: clearForm
}