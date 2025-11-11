package ui;

import dal.DataAccess;
import model.Bed;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class BedManagementPanel extends JPanel {
    private final DataAccess dataAccess = new DataAccess();
    private JLabel countLabel;
    private int totalBeds = 0;
    
    private JPanel mainContentPanel;
    private JScrollPane scrollPane;

    public BedManagementPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 245));

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setBackground(Color.WHITE);
        
        scrollPane = new JScrollPane(mainContentPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // This makes the scroll speed reasonable
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);

        refreshBedLayout();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(165, 42, 42));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JLabel titleLabel = new JLabel("BED MANAGEMENT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        countLabel = new JLabel("Total Beds: 0 (Available: 0 | Occupied: 0)");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        countLabel.setForeground(Color.WHITE);
        headerPanel.add(countLabel, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(Color.WHITE);
        
        JButton refreshButton = createStyledButton("REFRESH STATUS", new Color(165, 42, 42));
        refreshButton.addActionListener(e -> refreshBedLayout());
        bottomPanel.add(refreshButton);
        
        return bottomPanel;
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

    public void refreshBedLayout() {
        mainContentPanel.removeAll();
        totalBeds = 0;
        int availableBeds = 0;
        int occupiedBeds = 0;

        try {
            Map<Integer, List<Bed>> floorMap = dataAccess.getBedsGroupedByFloor();

            for (Map.Entry<Integer, List<Bed>> entry : floorMap.entrySet()) {
                int floorNumber = entry.getKey();
                List<Bed> bedsOnFloor = entry.getValue();

                // [START] THE FIX
                // Use WrapLayout instead of FlowLayout
                JPanel floorPanel = new JPanel(new WrapLayout(WrapLayout.LEFT, 10, 10));
                // [END] THE FIX
                
                floorPanel.setBackground(Color.WHITE);
                floorPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    "Floor " + floorNumber,
                    javax.swing.border.TitledBorder.LEFT,
                    javax.swing.border.TitledBorder.TOP,
                    new Font("Segoe UI", Font.BOLD, 16),
                    Color.DARK_GRAY
                ));
                
                for (Bed bed : bedsOnFloor) {
                    floorPanel.add(new BedBlock(bed));
                    totalBeds++;
                    if ("Available".equals(bed.getStatus())) {
                        availableBeds++;
                    } else {
                        occupiedBeds++;
                    }
                }
                
                mainContentPanel.add(floorPanel);
                mainContentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
            
            countLabel.setText(String.format(
                "Total Beds: %d (Available: %d | Occupied: %d)",
                totalBeds, availableBeds, occupiedBeds
            ));

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading bed data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }
}