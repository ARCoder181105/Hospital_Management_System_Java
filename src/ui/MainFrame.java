package ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Hospital Management System");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Use JTabbedPane with better styling
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Create panels
        PatientPanel patientPanel = new PatientPanel();
        DoctorPanel doctorPanel = new DoctorPanel(patientPanel);
        BedManagementPanel bedPanel = new BedManagementPanel();
        BillingPanel billingPanel = new BillingPanel();

        // Add tabs with clear text labels
        tabbedPane.addTab("Patient Management", patientPanel);
        tabbedPane.addTab("Doctor Management", doctorPanel);
        tabbedPane.addTab("Bed Management", bedPanel);
        tabbedPane.addTab("Billing & Discharge", billingPanel);
        
        // Set tooltips for better UX
        tabbedPane.setToolTipTextAt(0, "Manage patient registrations and information");
        tabbedPane.setToolTipTextAt(1, "Manage doctor information and assignments");
        tabbedPane.setToolTipTextAt(2, "View and manage bed allocations");
        tabbedPane.setToolTipTextAt(3, "Generate bills and discharge patients");
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Add status bar
        add(createStatusBar(), BorderLayout.SOUTH);
    }
    
    private JPanel createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.setBackground(new Color(240, 240, 240));
        statusPanel.setPreferredSize(new Dimension(getWidth(), 25));
        
        JLabel statusLabel = new JLabel(" System Ready - Hospital Management System v1.0");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        JLabel timeLabel = new JLabel(new java.util.Date().toString());
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusPanel.add(timeLabel, BorderLayout.EAST);
        
        return statusPanel;
    }
}