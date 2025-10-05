// ui/MainFrame.java (Completely Replaced)
package ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Integrated Hospital Management System");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Use JTabbedPane for a better UI structure
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Panels for each module
        PatientPanel patientPanel = new PatientPanel();
        DoctorPanel doctorPanel = new DoctorPanel(patientPanel); // Still needs patientPanel to refresh doctor list
        BedManagementPanel bedPanel = new BedManagementPanel();
        BillingPanel billingPanel = new BillingPanel();

        // Add tabs to the pane
        tabbedPane.addTab("Patient Management", patientPanel);
        tabbedPane.addTab("Doctor Management", doctorPanel);
        tabbedPane.addTab("Bed Allotment", bedPanel);
        tabbedPane.addTab("Billing & Discharge", billingPanel);
        
        // Add tabbed pane to the frame
        add(tabbedPane, BorderLayout.CENTER);
    }
}