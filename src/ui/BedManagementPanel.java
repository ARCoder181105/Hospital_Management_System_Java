package ui;

import dal.DataAccess;
import model.Bed;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class BedManagementPanel extends JPanel {
    private final DataAccess dataAccess = new DataAccess();
    private JTable bedTable;
    private DefaultTableModel tableModel;
    private JLabel countLabel;

    public BedManagementPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 245));

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Table Panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);

        // Bottom Panel
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);

        loadBedData();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(165, 42, 42));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel titleLabel = new JLabel("🛏️ Bed Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        countLabel = new JLabel("Total Beds: 0");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        countLabel.setForeground(Color.WHITE);
        headerPanel.add(countLabel, BorderLayout.EAST);
        
        return headerPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Bed Status Overview"));
        tablePanel.setBackground(Color.WHITE);

        String[] columnNames = {"Bed ID", "Ward", "Status", "Patient ID", "Patient Name"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        bedTable = new JTable(tableModel);
        bedTable.setDefaultRenderer(Object.class, new BedStatusRenderer());
        bedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bedTable.setRowHeight(30);
        bedTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Style table header
        JTableHeader header = bedTable.getTableHeader();
        header.setBackground(new Color(165, 42, 42));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(bedTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(Color.WHITE);
        
        JButton refreshButton = createStyledButton("🔄 Refresh Status", new Color(165, 42, 42));
        refreshButton.addActionListener(e -> loadBedData());
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

    private void loadBedData() {
        try {
            tableModel.setRowCount(0);
            List<Bed> beds = dataAccess.getAllBeds();
            for (Bed bed : beds) {
                Object[] row = {
                    bed.getBedId(),
                    bed.getWard(),
                    bed.getStatus(),
                    bed.getPatientId() == 0 ? "N/A" : bed.getPatientId(),
                    bed.getPatientName() == null ? "N/A" : bed.getPatientName()
                };
                tableModel.addRow(row);
            }
            countLabel.setText("Total Beds: " + beds.size());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading bed data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class BedStatusRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        String status = (String) table.getModel().getValueAt(row, 2); 

        if ("Occupied".equals(status)) {
            c.setBackground(new Color(255, 182, 193)); 
            c.setForeground(Color.BLACK);
        } else if ("Available".equals(status)) {
            c.setBackground(new Color(144, 238, 144));
            c.setForeground(Color.BLACK);
        } else {
            c.setBackground(table.getBackground());
            c.setForeground(table.getForeground());
        }

        if (isSelected) {
            c.setBackground(table.getSelectionBackground());
            c.setForeground(table.getSelectionForeground());
        }
        return c;
    }
}