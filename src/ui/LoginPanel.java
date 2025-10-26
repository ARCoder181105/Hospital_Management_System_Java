package ui;

import dal.DataAccess;
import model.Employee;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class LoginPanel extends JPanel {
    private final DataAccess dataAccess = new DataAccess();
    private JTextField employeeNumberField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    private LoginListener loginListener;

    public interface LoginListener {
        void onLoginSuccess(Employee employee);
    }

    public LoginPanel(LoginListener listener) {
        this.loginListener = listener;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        // Main content panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(245, 245, 245));

        // Login form panel
        JPanel loginPanel = createLoginPanel();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 50, 20, 50);

        mainPanel.add(createHeaderPanel(), gbc);
        mainPanel.add(loginPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(245, 245, 245));
        headerPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("HOSPITAL MANAGEMENT SYSTEM", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(70, 130, 180));

        JLabel subtitleLabel = new JLabel("Staff Login Portal", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(Color.DARK_GRAY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.CENTER);

        return headerPanel;
    }

    private JPanel createLoginPanel() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)));
        loginPanel.setBackground(Color.WHITE);
        loginPanel.setPreferredSize(new Dimension(400, 350));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Icon/Image placeholder
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel iconLabel = new JLabel("HOSPITAL", JLabel.CENTER);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        iconLabel.setForeground(new Color(70, 130, 180));
        loginPanel.add(iconLabel, gbc);

        // Employee Number
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel empNumLabel = new JLabel("Employee Number:");
        empNumLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        loginPanel.add(empNumLabel, gbc);

        gbc.gridx = 1;
        employeeNumberField = new JTextField();
        employeeNumberField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        employeeNumberField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        loginPanel.add(employeeNumberField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        loginPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        loginPanel.add(passwordField, gbc);

        // Login Button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        loginButton = new JButton("LOGIN TO SYSTEM");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));

        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(new Color(50, 110, 160));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(new Color(70, 130, 180));
            }
        });

        loginButton.addActionListener(e -> performLogin());
        loginPanel.add(loginButton, gbc);

        // Status Label
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 10, 5, 10);
        statusLabel = new JLabel(" ", JLabel.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(Color.RED);
        loginPanel.add(statusLabel, gbc);

        // Instructions
        gbc.gridy = 5;
        JLabel instructionLabel = new JLabel(
                "<html><center>Use your employee number and password to access the system</center></html>",
                JLabel.CENTER);
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        instructionLabel.setForeground(Color.GRAY);
        loginPanel.add(instructionLabel, gbc);

        // Enter key listener for login
        passwordField.addActionListener(e -> performLogin());

        return loginPanel;
    }

    private void performLogin() {
        String employeeNumber = employeeNumberField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (employeeNumber.isEmpty() || password.isEmpty()) {
            showError("Please enter both employee number and password.");
            return;
        }

        try {
            Employee employee = dataAccess.authenticateEmployee(employeeNumber, password);
            if (employee != null) {
                showSuccess("Login successful! Welcome, " + employee.getName());
                // Clear fields
                employeeNumberField.setText("");
                passwordField.setText("");
                // Notify listener
                if (loginListener != null) {
                    loginListener.onLoginSuccess(employee);
                }
            } else {
                showError("Invalid employee number or password. Please try again.");
                passwordField.setText("");
            }
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showError(String message) {
        statusLabel.setForeground(Color.RED);
        statusLabel.setText(message);
    }

    private void showSuccess(String message) {
        statusLabel.setForeground(new Color(0, 128, 0));
        statusLabel.setText(message);
    }
}