package ui;

import dal.DataAccess;
import model.ConfigIllness;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ConfigIllnessPanel extends JPanel {
    private final DataAccess dataAccess = new DataAccess();
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField nameField;
    private JButton addButton, updateButton, clearButton, deleteButton;
    private int selectedIllnessId = -1;
    private String selectedIllnessName = "";

    public ConfigIllnessPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form Panel
        add(createFormPanel(), BorderLayout.NORTH);
        
        // Table Panel
        add(createTablePanel(), BorderLayout.CENTER);

        loadIllnesses();
        clearForm();
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Manage Illness"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Illness Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        nameField = new JTextField();
        panel.add(nameField, gbc);

        // Button Panel
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        addButton = new JButton("Add New");
        addButton.addActionListener(e -> addIllness());
        updateButton = new JButton("Update Selected");
        updateButton.addActionListener(e -> updateIllness());
        deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> deleteIllness());
        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearForm());
        
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JScrollPane createTablePanel() {
        tableModel = new DefaultTableModel(new String[]{"ID", "Illness Name"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.convertRowIndexToModel(table.getSelectedRow());
                selectedIllnessId = (int) tableModel.getValueAt(row, 0);
                selectedIllnessName = (String) tableModel.getValueAt(row, 1);
                nameField.setText(selectedIllnessName);
                
                addButton.setEnabled(false);
                updateButton.setEnabled(true);
                deleteButton.setEnabled(true);
                
                // Don't allow deleting/editing the "Other..." option
                if ("Other...".equals(selectedIllnessName)) {
                    updateButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                }
            }
        });

        return new JScrollPane(table);
    }

    private void loadIllnesses() {
        try {
            tableModel.setRowCount(0);
            List<ConfigIllness> illnesses = dataAccess.getAllIllnesses();
            for (ConfigIllness illness : illnesses) {
                tableModel.addRow(new Object[]{illness.getIllnessId(), illness.getIllnessName()});
            }
        } catch (SQLException e) {
            showError("Could not load illness list: " + e.getMessage());
        }
    }

    private void addIllness() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError("Illness name cannot be empty.");
            return;
        }
        
        ConfigIllness illness = new ConfigIllness();
        illness.setIllnessName(name);
        
        try {
            dataAccess.addIllness(illness);
            loadIllnesses();
            clearForm();
        } catch (SQLException e) {
            showError("Error adding illness: " + e.getMessage());
        }
    }

    private void updateIllness() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError("Illness name cannot be empty.");
            return;
        }
        
        ConfigIllness illness = new ConfigIllness();
        illness.setIllnessId(selectedIllnessId);
        illness.setIllnessName(name);
        
        try {
            dataAccess.updateIllness(illness);
            loadIllnesses();
            clearForm();
        } catch (SQLException e) {
            showError("Error updating illness: " + e.getMessage());
        }
    }

    private void deleteIllness() {
        if ("Other...".equals(selectedIllnessName)) {
            showError("Cannot delete the default 'Other...' option.");
            return;
        }
        
        int choice = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete '" + selectedIllnessName + "'?\n" +
            "This may fail if any patient is assigned this illness.", 
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            dataAccess.deleteIllness(selectedIllnessId);
            loadIllnesses();
            clearForm();
        } catch (SQLException e) {
            showError("Could not delete illness. It may be in use by a patient record.");
        }
    }

    private void clearForm() {
        selectedIllnessId = -1;
        selectedIllnessName = "";
        nameField.setText("");
        table.clearSelection();
        addButton.setEnabled(true);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}