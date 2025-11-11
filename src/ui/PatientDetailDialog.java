package ui;

import model.Patient;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

public class PatientDetailDialog extends JDialog {

    public PatientDetailDialog(Frame owner, Patient patient) {
        super(owner, "Patient Details", true);
        setSize(400, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(new Color(60, 179, 113));
        JLabel titleLabel = new JLabel(patient.getName());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        add(headerPanel, BorderLayout.NORTH);
        
        // Details
        add(createDetailsPanel(patient), BorderLayout.CENTER);
        
        // Close Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeButton.addActionListener(e -> setVisible(false));
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createDetailsPanel(Patient patient) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        
        int y = 0;
        addDetailRow(panel, gbc, y++, "Patient ID:", String.valueOf(patient.getPatientId()));
        addDetailRow(panel, gbc, y++, "Age:", String.valueOf(patient.getAge()));
        addDetailRow(panel, gbc, y++, "Gender:", patient.getGender());
        addDetailRow(panel, gbc, y++, "Admitted On:", new SimpleDateFormat("yyyy-MM-dd").format(patient.getAdmittedDate()));
        
        addSeparator(panel, gbc, y++);

        addDetailRow(panel, gbc, y++, "Illness:", patient.getIllness());
        addDetailRow(panel, gbc, y++, "Severity:", patient.getDiseaseSeverity());

        addSeparator(panel, gbc, y++);
        
        addDetailRow(panel, gbc, y++, "Assigned Doctor:", patient.getAssignedDoctorName());
        addDetailRow(panel, gbc, y++, "Bed ID:", patient.getBedId() == 0 ? "Unassigned (Outpatient)" : String.valueOf(patient.getBedId()));
        addDetailRow(panel, gbc, y++, "Requested Bed:", patient.getRequestedBedType() != null ? patient.getRequestedBedType() : "N/A");

        // Add glue to push everything to the top
        gbc.gridy = y;
        gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);
        
        return panel;
    }

    private void addDetailRow(JPanel panel, GridBagConstraints gbc, int y, String label, String value) {
        gbc.gridy = y;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lbl, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0.7;
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        val.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(val, gbc);
    }
    
    private void addSeparator(JPanel panel, GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 10, 0);
        panel.add(new JSeparator(), gbc);
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.gridwidth = 1;
    }
}