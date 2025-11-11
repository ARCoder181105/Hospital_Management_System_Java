package ui;

import dal.DataAccess;
import model.ConfigBedType;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ConfigBedPanel extends JPanel {
    private final DataAccess dataAccess = new DataAccess();
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField nameField, priceField;
    private JButton addButton, updateButton, clearButton;
    private JButton deleteButton; // [NEW] Delete button
    private int selectedBedTypeId = -1;
    private String selectedBedTypeName = ""; // [NEW] To store the name for confirmation

    public ConfigBedPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form Panel
        add(createFormPanel(), BorderLayout.NORTH);
        
        // Table Panel
        add(createTablePanel(), BorderLayout.CENTER);

        loadBedTypes();
        clearForm();
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Manage Bed Type"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Bed Type Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        nameField = new JTextField();
        panel.add(nameField, gbc);

        // Price
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Price per Day (Rs.):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        priceField = new JTextField();
        panel.add(priceField, gbc);

        // [UPDATED] Button Panel
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        
        addButton = new JButton("Add New");
        addButton.addActionListener(e -> addBedType());
        
        updateButton = new JButton("Update Selected");
        updateButton.addActionListener(e -> updateBedType());
        
        deleteButton = new JButton("Delete Selected"); // [NEW]
        deleteButton.setBackground(new Color(220, 53, 69)); // Red color
        deleteButton.setForeground(Color.WHITE);
        deleteButton.addActionListener(e -> deleteBedType()); // [NEW]
        
        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearForm());
        
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton); // [NEW]
        buttonPanel.add(clearButton);
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JScrollPane createTablePanel() {
        tableModel = new DefaultTableModel(new String[]{"ID", "Bed Type", "Price per Day"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        
        // [UPDATED] ListSelectionListener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.convertRowIndexToModel(table.getSelectedRow());
                selectedBedTypeId = (int) tableModel.getValueAt(row, 0);
                selectedBedTypeName = (String) tableModel.getValueAt(row, 1); // [NEW]
                
                nameField.setText(selectedBedTypeName);
                priceField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
                
                addButton.setEnabled(false);
                updateButton.setEnabled(true);
                deleteButton.setEnabled(true); // [NEW]
            }
        });

        return new JScrollPane(table);
    }

    private void loadBedTypes() {
        try {
            tableModel.setRowCount(0);
            List<ConfigBedType> bedTypes = dataAccess.getAllBedTypes();
            for (ConfigBedType type : bedTypes) {
                tableModel.addRow(new Object[]{type.getBedTypeId(), type.getBedTypeName(), type.getPricePerDay()});
            }
        } catch (SQLException e) {
            showError("Could not load bed types: " + e.getMessage());
        }
    }

    private void addBedType() {
        ConfigBedType type = getBedTypeFromForm();
        if (type == null) return;
        
        try {
            dataAccess.addBedType(type);
            loadBedTypes();
            clearForm();
        } catch (SQLException e) {
            showError("Error adding bed type: " + e.getMessage());
        }
    }

    private void updateBedType() {
        ConfigBedType type = getBedTypeFromForm();
        if (type == null) return;
        
        type.setBedTypeId(selectedBedTypeId);
        try {
            dataAccess.updateBedType(type);
            loadBedTypes();
            clearForm();
        } catch (SQLException e) {
            showError("Error updating bed type: " + e.getMessage());
        }
    }
    
    // [START] NEW METHOD: deleteBedType
    private void deleteBedType() {
        if (selectedBedTypeId == -1) {
            showError("Please select a bed type from the table to delete.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the bed type '" + selectedBedTypeName + "'?\n" +
                "This will fail if any beds in the hospital are still assigned this type.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                if (dataAccess.deleteBedType(selectedBedTypeId)) {
                    JOptionPane.showMessageDialog(this, "Bed type deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadBedTypes();
                    clearForm();
                } else {
                    showError("Failed to delete bed type.");
                }
            } catch (SQLException e) {
                // This catches the Foreign Key violation
                showError("Could not delete bed type. It is still in use by one or more beds.");
            }
        }
    }
    // [END] NEW METHOD: deleteBedType

    private ConfigBedType getBedTypeFromForm() {
        String name = nameField.getText().trim();
        String priceStr = priceField.getText().trim();
        if (name.isEmpty() || priceStr.isEmpty()) {
            showError("Name and Price fields cannot be empty.");
            return null;
        }
        
        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            showError("Please enter a valid number for price.");
            return null;
        }

        ConfigBedType type = new ConfigBedType();
        type.setBedTypeName(name);
        type.setPricePerDay(price);
        return type;
    }

    // [UPDATED] clearForm
    private void clearForm() {
        selectedBedTypeId = -1;
        selectedBedTypeName = "";
        nameField.setText("");
        priceField.setText("");
        table.clearSelection();
        addButton.setEnabled(true);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false); // [NEW]
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}