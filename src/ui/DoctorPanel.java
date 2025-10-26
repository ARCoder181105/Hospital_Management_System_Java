package ui;

import dal.DataAccess;
import model.Doctor;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class DoctorPanel extends JPanel {
    private final DataAccess dataAccess = new DataAccess();
    private JTextField nameField, specializationField, phoneField, emailField;
    private JButton submitButton;
    private JTable doctorTable;
    private DefaultTableModel tableModel;
    private PatientPanel patientPanel;
    private JLabel countLabel;

    public DoctorPanel(PatientPanel patientPanel) {
        this.patientPanel = patientPanel;
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
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
            "Add New Doctor",
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

        // Submit Button
        gbc.gridx = 1; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        submitButton = createStyledButton("ADD DOCTOR", new Color(34, 139, 34));
        submitButton.addActionListener(e -> addDoctor());
        formPanel.add(submitButton, gbc);

        return formPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Available Doctors"));
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
        
        // Style table header
        JTableHeader header = doctorTable.getTableHeader();
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(doctorTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Refresh button
        JButton refreshButton = createStyledButton("REFRESH LIST", new Color(70, 130, 180));
        refreshButton.addActionListener(e -> loadDoctors());
        
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

    private void loadDoctors() {
        try {
            tableModel.setRowCount(0);
            List<Doctor> doctors = dataAccess.getAllDoctors();
            for (Doctor d : doctors) {
                Object[] row = {d.getDoctorId(), d.getName(), d.getSpecialization(), d.getPhone(), d.getEmail()};
                tableModel.addRow(row);
            }
            countLabel.setText("Total Doctors: " + doctors.size());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading doctors: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addDoctor() {
        try {
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
                patientPanel.loadDoctors();
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