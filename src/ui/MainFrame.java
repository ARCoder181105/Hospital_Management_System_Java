package ui;

import model.Employee;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame implements LoginPanel.LoginListener {
    private JPanel currentPanel;
    private Employee currentEmployee;
    private JLabel userInfoLabel;

    public MainFrame() {
        setTitle("Hospital Management System - Login");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        showLoginPanel();
    }

    private void showLoginPanel() {
        if (currentPanel != null) {
            remove(currentPanel);
        }
        
        LoginPanel loginPanel = new LoginPanel(this);
        currentPanel = loginPanel;
        add(currentPanel, BorderLayout.CENTER);
        
        setTitle("Hospital Management System - Login");
        revalidate();
        repaint();
    }

    private void showMainApplication() {
        if (currentPanel != null) {
            remove(currentPanel);
        }

        // Create main application panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create header with user info
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Create tabbed pane for main functionality
        JTabbedPane tabbedPane = createMainTabbedPane();
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Create status bar
        JPanel statusBar = createStatusBar();
        mainPanel.add(statusBar, BorderLayout.SOUTH);
        
        currentPanel = mainPanel;
        add(currentPanel, BorderLayout.CENTER);
        
        setTitle("Hospital Management System - Welcome, " + currentEmployee.getName());
        revalidate();
        repaint();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 60));

        // Application title
        JLabel titleLabel = new JLabel("HOSPITAL MANAGEMENT SYSTEM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // User info and logout
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setBackground(new Color(70, 130, 180));
        
        userInfoLabel = new JLabel();
        userInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userInfoLabel.setForeground(Color.WHITE);
        userPanel.add(userInfoLabel);
        
        userPanel.add(Box.createHorizontalStrut(20));
        
        JButton logoutButton = new JButton("LOGOUT");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        
        logoutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(new Color(200, 35, 51));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(new Color(220, 53, 69));
            }
        });
        
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to logout?", 
                "Confirm Logout", 
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                currentEmployee = null;
                showLoginPanel();
            }
        });
        
        userPanel.add(logoutButton);
        headerPanel.add(userPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JTabbedPane createMainTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Create panels
        PatientPanel patientPanel = new PatientPanel();
        DoctorPanel doctorPanel = new DoctorPanel(patientPanel);
        BedManagementPanel bedPanel = new BedManagementPanel();
        BillingPanel billingPanel = new BillingPanel();

        // Add tabs
        tabbedPane.addTab("Patient Management", patientPanel);
        tabbedPane.addTab("Doctor Management", doctorPanel);
        tabbedPane.addTab("Bed Management", bedPanel);
        tabbedPane.addTab("Billing & Discharge", billingPanel);

        // Set tooltips
        tabbedPane.setToolTipTextAt(0, "Manage patient registrations and information");
        tabbedPane.setToolTipTextAt(1, "Manage doctor information and assignments");
        tabbedPane.setToolTipTextAt(2, "View and manage bed allocations");
        tabbedPane.setToolTipTextAt(3, "Generate bills and discharge patients");

        return tabbedPane;
    }

    private JPanel createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.setBackground(new Color(240, 240, 240));
        statusPanel.setPreferredSize(new Dimension(getWidth(), 25));

        JLabel statusLabel = new JLabel(" System Ready - Hospital Management System v2.0");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusPanel.add(statusLabel, BorderLayout.WEST);

        JLabel timeLabel = new JLabel(new java.util.Date().toString());
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusPanel.add(timeLabel, BorderLayout.EAST);

        return statusPanel;
    }

    @Override
    public void onLoginSuccess(Employee employee) {
        this.currentEmployee = employee;
        updateUserInfo();
        showMainApplication();
    }

    private void updateUserInfo() {
        if (currentEmployee != null && userInfoLabel != null) {
            String userInfo = String.format("Logged in as: %s (%s) - %s", 
                currentEmployee.getName(), 
                currentEmployee.getEmployeeNumber(),
                currentEmployee.getRole());
            userInfoLabel.setText(userInfo);
        }
    }
}