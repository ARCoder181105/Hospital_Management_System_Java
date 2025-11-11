package ui;

import dal.DataAccess;
import model.Doctor;
import model.Patient;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class DoctorDetailDialog extends JDialog {

    private DataAccess dataAccess;

    public DoctorDetailDialog(Frame owner, Doctor doctor, DataAccess dataAccess) {
        super(owner, "Doctor Details", true);
        this.dataAccess = dataAccess;
        setSize(450, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel titleLabel = new JLabel(doctor.getName());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        JLabel subtitleLabel = new JLabel(doctor.getSpecialization());
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.WHITE);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Split pane for details and patient list
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(150);
        splitPane.setTopComponent(createDetailsPanel(doctor));
        splitPane.setBottomComponent(createPatientListPanel(doctor));
        
        add(splitPane, BorderLayout.CENTER);
        
        // Close Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeButton.addActionListener(e -> setVisible(false));
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createDetailsPanel(Doctor doctor) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        
        addDetailRow(panel, gbc, 0, "Doctor ID:", String.valueOf(doctor.getDoctorId()));
        addDetailRow(panel, gbc, 1, "Phone:", doctor.getPhone());
        addDetailRow(panel, gbc, 2, "Email:", doctor.getEmail());
        
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
    
    private JPanel createPatientListPanel(Doctor doctor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Assigned Patients (Active)"));
        
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> patientList = new JList<>(listModel);
        patientList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        try {
            List<Patient> patients = dataAccess.getPatientsByDoctorId(doctor.getDoctorId());
            if (patients.isEmpty()) {
                listModel.addElement("No active patients assigned.");
            } else {
                for (Patient p : patients) {
                    listModel.addElement(p.getName() + " (ID: " + p.getPatientId() + ") - " + p.getIllness());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            listModel.addElement("Error loading patients.");
        }
        
        panel.add(new JScrollPane(patientList), BorderLayout.CENTER);
        return panel;
    }
}