package ui;

import model.Employee;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainFrame extends JFrame implements LoginPanel.LoginListener {
    private JPanel currentPanel;
    private Employee currentEmployee;
    private JLabel userInfoLabel;
    private JLabel dateTimeLabel;

    public MainFrame() {
        setTitle("Hospital Management System - Login");
        setSize(1200, 800);
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
        
        // Remove header bar if it exists
        setJMenuBar(null);
        
        setTitle("Hospital Management System - Login");
        revalidate();
        repaint();
    }

    /**
     * Shows the main tabbed application for Admin and other staff roles.
     * @param employee The employee who logged in.
     */
    private void showMainApplication(Employee employee) {
        if (currentPanel != null) {
            remove(currentPanel);
        }
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Pass employee to tabbed pane creator
        JTabbedPane tabbedPane = createMainTabbedPane(employee);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        JPanel statusBar = createStatusBar();
        mainPanel.add(statusBar, BorderLayout.SOUTH);
        
        currentPanel = mainPanel;
        add(currentPanel, BorderLayout.CENTER);
        
        setTitle("Hospital Management System - Welcome, " + currentEmployee.getName());
        updateUserInfo();
        revalidate();
        repaint();
    }

    /**
     * Shows the dedicated portal for Doctors.
     */
    private void showDoctorPortal() {
        if (currentPanel != null) {
            remove(currentPanel);
        }

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Shared header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Doctor portal panel
        DoctorPortalPanel doctorPanel = new DoctorPortalPanel(currentEmployee);
        mainPanel.add(doctorPanel, BorderLayout.CENTER);

        // Shared status bar
        JPanel statusBar = createStatusBar();
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        currentPanel = mainPanel;
        add(currentPanel, BorderLayout.CENTER);

        setTitle("Doctor Portal - Welcome, " + currentEmployee.getName());
        updateUserInfo();
        revalidate();
        repaint();
    }

    /**
     * Creates the shared header panel with user info and logout button.
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 60));

        JLabel titleLabel = new JLabel("HOSPITAL MANAGEMENT SYSTEM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

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

    /**
     * Dynamically builds the JTabbedPane based on the employee's role.
     */
    private JTabbedPane createMainTabbedPane(Employee employee) {
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        String role = employee.getRole();

        // Create panels
        DashboardPanel dashboardPanel = new DashboardPanel();
        PatientPanel patientPanel = new PatientPanel();
        DoctorPanel doctorPanel = new DoctorPanel(patientPanel);
        BedManagementPanel bedPanel = new BedManagementPanel();
        
        // [FIX] Pass the patientPanel to the BillingPanel constructor
        BillingPanel billingPanel = new BillingPanel(patientPanel);
        
        ConfigPanel configPanel = new ConfigPanel();
        AppointmentPanel appointmentPanel = new AppointmentPanel();
        

        // Load initial dashboard stats
        dashboardPanel.refreshStats();

        // --- ROLE-BASED TAB LOGIC ---
        switch (role) {
            case "Administrator":
            case "IT Admin":
                tabbedPane.addTab("  Dashboard  ", dashboardPanel);
                tabbedPane.addTab("  Patient Management  ", patientPanel);
                tabbedPane.addTab("  Appointments  ", appointmentPanel);
                tabbedPane.addTab("  Doctor Management  ", doctorPanel);
                tabbedPane.addTab("  Bed Management  ", bedPanel);
                tabbedPane.addTab("  Billing & Discharge  ", billingPanel);
                tabbedPane.addTab("  Configuration  ", configPanel);
                break;

            case "Receptionist":
                tabbedPane.addTab("  Patient Management  ", patientPanel);
                tabbedPane.addTab("  Appointments  ", appointmentPanel);
                break;

            case "Nurse":
            case "Head Nurse":
                tabbedPane.addTab("  Patient Management  ", patientPanel);
                tabbedPane.addTab("  Bed Management  ", bedPanel);
                break;

            case "Billing Staff":
                tabbedPane.addTab("  Patient Management  ", patientPanel);
                tabbedPane.addTab("  Billing & Discharge  ", billingPanel);
                break;

            default:
                JPanel lockedPanel = new JPanel(new GridBagLayout());
                JLabel lockedLabel = new JLabel("No assigned role. Please contact an administrator.");
                lockedLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                lockedLabel.setForeground(Color.RED);
                lockedPanel.add(lockedLabel);
                tabbedPane.addTab("  Access Denied  ", lockedPanel);
                break;
        }

        // Refresh data dynamically on tab switch
        tabbedPane.addChangeListener(e -> {
            Component selectedComponent = tabbedPane.getSelectedComponent();

            if (selectedComponent instanceof PatientPanel) {
                ((PatientPanel) selectedComponent).loadDoctors();
                ((PatientPanel) selectedComponent).loadBedTypes();
                ((PatientPanel) selectedComponent).loadIllnesses();
                ((PatientPanel) selectedComponent).loadPatients();
            } else if (selectedComponent instanceof DoctorPanel) {
                ((DoctorPanel) selectedComponent).loadDoctors();
            } else if (selectedComponent instanceof BedManagementPanel) {
                ((BedManagementPanel) selectedComponent).refreshBedLayout();
            } else if (selectedComponent instanceof BillingPanel) {
                ((BillingPanel) selectedComponent).loadAdmittedPatients();
                ((BillingPanel) selectedComponent).loadBillingHistory();
            } else if (selectedComponent instanceof DashboardPanel) {
                ((DashboardPanel) selectedComponent).refreshStats();
            } else if (selectedComponent instanceof AppointmentPanel) {
                ((AppointmentPanel) selectedComponent).loadAllDoctors();
                ((AppointmentPanel) selectedComponent).loadAllPatients();
            }
            // ConfigPanel handles its own refreshes internally
        });

        return tabbedPane;
    }

    /**
     * Creates the shared status bar with a live clock.
     */
    private JPanel createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.setBackground(new Color(240, 240, 240));
        statusPanel.setPreferredSize(new Dimension(getWidth(), 25));

        JLabel statusLabel = new JLabel(" System Ready - Hospital Management System v3.0");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusPanel.add(statusLabel, BorderLayout.WEST);

        dateTimeLabel = new JLabel();
        dateTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusPanel.add(dateTimeLabel, BorderLayout.EAST);

        Timer timer = new Timer(1000, e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateTimeLabel.setText(sdf.format(new Date()) + " ");
        });
        timer.start();

        return statusPanel;
    }

    /**
     * Called after a successful login — routes based on employee role.
     */
    @Override
    public void onLoginSuccess(Employee employee) {
        this.currentEmployee = employee;
        String role = employee.getRole();

        if ("Doctor".equals(role) || "Senior Doctor".equals(role)) {
            showDoctorPortal();
        } else {
            showMainApplication(employee);
        }
    }

    /**
     * Updates user info in the header.
     */
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