package ui;

import dal.DataAccess;
import model.Bed;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class BedManagementPanel extends JPanel {
    private final DataAccess dataAccess = new DataAccess();
    private JTable bedTable;
    private DefaultTableModel tableModel;

    public BedManagementPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table Panel
        String[] columnNames = {"Bed ID", "Ward", "Status", "Patient ID", "Patient Name"};
        tableModel = new DefaultTableModel(columnNames, 0);
        bedTable = new JTable(tableModel);
        
        bedTable.setDefaultRenderer(Object.class, new BedStatusRenderer());

        JScrollPane scrollPane = new JScrollPane(bedTable);
        add(scrollPane, BorderLayout.CENTER);

        // Refresh Button
        JButton refreshButton = new JButton("Refresh Status");
        refreshButton.addActionListener(e -> loadBedData());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(refreshButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadBedData();
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