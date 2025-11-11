package ui;

import dal.DataAccess;

import javax.swing.*;
// import javax.swing.border.Border;
import java.awt.*;
import java.sql.SQLException;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private DataAccess dataAccess = new DataAccess();

    // Labels for the stat cards
    private JLabel totalPatientsValue;
    private JLabel availableBedsValue;
    private JLabel totalRevenueValue;
    private JLabel totalDoctorsValue;

    public DashboardPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 245));

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Stats Grid
        JPanel statsPanel = createStatsGridPanel();
        add(statsPanel, BorderLayout.CENTER);

        // Initial data load
        refreshStats();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 245));
        
        JLabel titleLabel = new JLabel("Hospital Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.addActionListener(e -> refreshStats());
        headerPanel.add(refreshButton, BorderLayout.EAST);
        
        return headerPanel;
    }

    private JPanel createStatsGridPanel() {
        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        gridPanel.setBackground(new Color(245, 245, 245));

        // Initialize value labels
        totalPatientsValue = new JLabel("0", SwingConstants.LEFT);
        availableBedsValue = new JLabel("0", SwingConstants.LEFT);
        totalRevenueValue = new JLabel("Rs. 0.00", SwingConstants.LEFT);
        totalDoctorsValue = new JLabel("0", SwingConstants.LEFT);

        // Create and add stat cards
        gridPanel.add(createStatCard("Active In-Patients", totalPatientsValue, new Color(37, 99, 235)));
        gridPanel.add(createStatCard("Available Beds", availableBedsValue, new Color(34, 197, 94)));
        gridPanel.add(createStatCard("Total Doctors", totalDoctorsValue, new Color(255, 140, 0)));
        gridPanel.add(createStatCard("Total Revenue", totalRevenueValue, new Color(148, 0, 211)));

        return gridPanel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(5, 0, 0, 0, accentColor), // Top accent border
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.GRAY);
        card.add(titleLabel, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(accentColor.darker());
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    public void refreshStats() {
        try {
            Map<String, Long> stats = dataAccess.getDashboardStats();
            
            totalPatientsValue.setText(String.valueOf(stats.getOrDefault("active_patients", 0L)));
            availableBedsValue.setText(String.valueOf(stats.getOrDefault("available_beds", 0L)));
            totalDoctorsValue.setText(String.valueOf(stats.getOrDefault("total_doctors", 0L)));
            
            double revenue = stats.getOrDefault("total_revenue", 0L).doubleValue();
            totalRevenueValue.setText(String.format("Rs. %.2f", revenue));

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not load dashboard statistics.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}