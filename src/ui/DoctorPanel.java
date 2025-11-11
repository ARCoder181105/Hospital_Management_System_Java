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
import java.util.List;

public class DoctorPanel extends JPanel {
    private final DataAccess dataAccess = new DataAccess();
    private JTextField nameField, specializationField, phoneField, emailField;
    private JButton addButton, updateButton, deleteButton, clearButton;
    private JTable doctorTable;
    private DefaultTableModel tableModel;
    private PatientPanel patientPanel;
    private JLabel countLabel;
    private int selectedDoctorId = -1;
    
    private List<Doctor> doctorList; // Holds the full list of doctor objects

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
        splitPane.setDividerLocation(250);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setTopComponent(createFormPanel());
        splitPane.setBottomComponent(createTablePanel());
        return splitPane;
    }

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

        // Name Field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createStyledLabel("Name:*"), gbc);
        gbc.gridx = 1;
        nameField = createStyledTextField();
        formPanel.add(nameField, gbc);

        // Specialization Field
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createStyledLabel("Specialization:*"), gbc);
        gbc.gridx = 1;
        specializationField = createStyledTextField();
        formPanel.add(specializationField, gbc);

        // Phone Field
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createStyledLabel("Phone:"), gbc);
        gbc.gridx = 1;
        phoneField = createStyledTextField();
        formPanel.add(phoneField, gbc);

        // Email Field
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createStyledLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailField = createStyledTextField();
        formPanel.add(emailField, gbc);

        // Button Panel
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

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(buttonPanel, gbc);

        return formPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Available Doctors (Double-click for details)"));
        tablePanel.setBackground(Color.WHITE);

        String[] columnNames = {"ID", "Name", "Specialization", "Phone", "Email"};
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
    
    private void populateFormFromTable() {
        int selectedRow = doctorTable.convertRowIndexToModel(doctorTable.getSelectedRow());
        if (selectedRow == -1) return;

        selectedDoctorId = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        String specialization = (String) tableModel.getValueAt(selectedRow, 2);
        String phone = (String) tableModel.getValueAt(selectedRow, 3);
        String email = (String) tableModel.getValueAt(selectedRow, 4);

        nameField.setText(name);
        specializationField.setText(specialization);
        phoneField.setText(phone);
        emailField.setText(email);

        addButton.setEnabled(false);
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

    // [START] CORRECTION
    // Changed from 'private' to 'public'
    public void loadDoctors() {
    // [END] CORRECTION
        try {
            tableModel.setRowCount(0);
            this.doctorList = dataAccess.getAllDoctors();
            
            for (Doctor d : doctorList) {
                Object[] row = {d.getDoctorId(), d.getName(), d.getSpecialization(), d.getPhone(), d.getEmail()};
                tableModel.addRow(row);
            }
            countLabel.setText("Total Doctors: " + doctorList.size());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading doctors: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
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

    private void addDoctor() {
        try {
            if (nameField.getText().trim().isEmpty() || specializationField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Doctor Name and Specialization cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Doctor doctor = new Doctor();
            doctor.setName(getPrefixedDoctorName());
            doctor.setSpecialization(specializationField.getText().trim());
            doctor.setPhone(phoneField.getText().trim());
            doctor.setEmail(emailField.getText().trim());

            if (dataAccess.addDoctor(doctor)) {
                JOptionPane.showMessageDialog(this, "Doctor added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadDoctors();
                patientPanel.loadDoctors(); // Refresh patient panel's doctor list
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add doctor.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error adding doctor: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
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

            Doctor doctor = new Doctor();
            doctor.setDoctorId(selectedDoctorId);
            doctor.setName(getPrefixedDoctorName());
            doctor.setSpecialization(specializationField.getText().trim());
            doctor.setPhone(phoneField.getText().trim());
            doctor.setEmail(emailField.getText().trim());

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
    
    private void deleteDoctor() {
        if (selectedDoctorId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a doctor from the table to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete " + nameField.getText() + "?\nThis action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                if (dataAccess.deleteDoctor(selectedDoctorId)) {
                    JOptionPane.showMessageDialog(this, "Doctor deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadDoctors();
                    patientPanel.loadDoctors();
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete doctor.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Could not delete doctor. They may still be assigned to active patients.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void clearForm() {
        nameField.setText("");
        specializationField.setText("");
        phoneField.setText("");
        emailField.setText("");
        
        selectedDoctorId = -1;
        doctorTable.clearSelection();
        
        addButton.setEnabled(true);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
}