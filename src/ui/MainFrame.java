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

    // [START] UPDATED METHOD
    // Now accepts the Employee object to pass to the tab builder
    private void showMainApplication(Employee employee) {
        if (currentPanel != null) {
            remove(currentPanel);
        }

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Header panel is now shared
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Pass the employee to the tabbed pane creator
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
    // [END] UPDATED METHOD

    private void showDoctorPortal() {
        if (currentPanel != null) {
            remove(currentPanel);
        }

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Add the same shared header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Add the new DoctorPortalPanel
        DoctorPortalPanel doctorPanel = new DoctorPortalPanel(currentEmployee);
        mainPanel.add(doctorPanel, BorderLayout.CENTER);

        // Add the same shared status bar
        JPanel statusBar = createStatusBar();
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        currentPanel = mainPanel;
        add(currentPanel, BorderLayout.CENTER);

        setTitle("Doctor Portal - Welcome, " + currentEmployee.getName());
        updateUserInfo();
        revalidate();
        repaint();
    }

    private JPanel createHeaderPanel() {
        // ... (This method is unchanged) ...
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

    // [START] UPDATED METHOD
    // This method now dynamically builds the tabs based on the employee's role
    private JTabbedPane createMainTabbedPane(Employee employee) {
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        String role = employee.getRole();

        // Create all panels. We only add the ones the user has access to.
        // We must create PatientPanel first, as DoctorPanel depends on it.
        PatientPanel patientPanel = new PatientPanel();
        DoctorPanel doctorPanel = new DoctorPanel(patientPanel);
        BedManagementPanel bedPanel = new BedManagementPanel();
        BillingPanel billingPanel = new BillingPanel();

        // --- ROLE-BASED TAB LOGIC ---
        switch (role) {
            case "Administrator":
            case "IT Admin":
                // Admins see all tabs
                tabbedPane.addTab("  Patient Management  ", patientPanel);
                tabbedPane.addTab("  Doctor Management  ", doctorPanel);
                tabbedPane.addTab("  Bed Management  ", bedPanel);
                tabbedPane.addTab("  Billing & Discharge  ", billingPanel);
                break;
            
            case "Receptionist":
                // Reception only sees patient registration
                tabbedPane.addTab("  Patient Management  ", patientPanel);
                break;
                
            case "Nurse":
            case "Head Nurse":
                // Nurses see patients and beds
                tabbedPane.addTab("  Patient Management  ", patientPanel);
                tabbedPane.addTab("  Bed Management  ", bedPanel);
                break;
                
            case "Billing Staff":
                // Billing sees patients (to find them) and billing
                tabbedPane.addTab("  Patient Management  ", patientPanel);
                tabbedPane.addTab("  Billing & Discharge  ", billingPanel);
                break;
                
            default:
                // Fallback for any other roles (if any)
                // Show a restricted view
                JPanel lockedPanel = new JPanel(new GridBagLayout());
                JLabel lockedLabel = new JLabel("No assigned role. Please contact an administrator.");
                lockedLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                lockedLabel.setForeground(Color.RED);
                lockedPanel.add(lockedLabel);
                tabbedPane.addTab("  Access Denied  ", lockedPanel);
                break;
        }
        
        // This listener will refresh whichever tabs are visible
        tabbedPane.addChangeListener(e -> {
            Component selectedComponent = tabbedPane.getSelectedComponent();
            if (selectedComponent instanceof PatientPanel) {
                ((PatientPanel) selectedComponent).loadDoctors();
                ((PatientPanel) selectedComponent).loadPatients();
            } else if (selectedComponent instanceof DoctorPanel) {
                ((DoctorPanel) selectedComponent).loadDoctors();
            } else if (selectedComponent instanceof BedManagementPanel) {
                ((BedManagementPanel) selectedComponent).refreshBedLayout();
            } else if (selectedComponent instanceof BillingPanel) {
                ((BillingPanel) selectedComponent).loadAdmittedPatients();
                ((BillingPanel) selectedComponent).loadBillingHistory();
            }
        });

        return tabbedPane;
    }
    // [END] UPDATED METHOD

    private JPanel createStatusBar() {
        // ... (This method is unchanged) ...
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.setBackground(new Color(240, 240, 240));
        statusPanel.setPreferredSize(new Dimension(getWidth(), 25));
        JLabel statusLabel = new JLabel(" System Ready - Hospital Management System v2.0");
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

    // [START] UPDATED METHOD
    // This is the main router
    @Override
    public void onLoginSuccess(Employee employee) {
        this.currentEmployee = employee;
        String role = employee.getRole();

        // Role-based routing
        if ("Doctor".equals(role) || "Senior Doctor".equals(role)) {
            showDoctorPortal();
        } else {
            // All other roles (Admin, Nurse, Billing, etc.)
            // go to the main application, which will build
            // the tabs based on their specific role.
            showMainApplication(employee);
        }
    }
    // [END] UPDATED METHOD

    private void updateUserInfo() {
        // ... (This method is unchanged) ...
        if (currentEmployee != null && userInfoLabel != null) {
            String userInfo = String.format("Logged in as: %s (%s) - %s",
                    currentEmployee.getName(),
                    currentEmployee.getEmployeeNumber(),
                    currentEmployee.getRole());
            userInfoLabel.setText(userInfo);
        }
    }
}