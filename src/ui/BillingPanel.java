package ui;

import dal.DataAccess;
import model.Bill;
import model.Doctor;
import model.Patient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BillingPanel extends JPanel {
    private final DataAccess dataAccess = new DataAccess();
    private static final double BASE_SERVICE_CHARGE = 500.00;

    private JComboBox<Patient> patientComboBox;
    private JTextField bedChargeField, serviceChargeField, doctorFeeField, totalField, daysStayedField;
    private JButton previewBillButton, generateBillButton;

    // Components for Billing History
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel totalRevenueLabel;

    private List<Bill> billingHistory; // Stores list of Bill objects

    // [NEW] Reference to PatientPanel for refreshing after discharge
    private PatientPanel patientPanel;

    // Updated constructor to accept PatientPanel
    public BillingPanel(PatientPanel patientPanel) {
        this.patientPanel = patientPanel;
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 245));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createSplitPane(), BorderLayout.CENTER);

        loadAdmittedPatients();
        loadBillingHistory();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(148, 0, 211));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel("BILLING & DISCHARGE");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        totalRevenueLabel = new JLabel("Total Revenue: ₹0.00");
        totalRevenueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        totalRevenueLabel.setForeground(Color.WHITE);
        headerPanel.add(totalRevenueLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JSplitPane createSplitPane() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(350);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setTopComponent(createGenerationPanel());
        splitPane.setBottomComponent(createHistoryPanel());
        return splitPane;
    }

    private JPanel createGenerationPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(148, 0, 211), 2),
                "Generate Bill and Discharge Patient",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(148, 0, 211)
        ));
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Patient Selection
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createStyledLabel("Select Admitted Patient:*"), gbc);
        gbc.gridx = 1;
        patientComboBox = new JComboBox<>();
        patientComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        patientComboBox.setBackground(Color.WHITE);

        // Custom renderer for patients
        patientComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Patient) {
                    setText(((Patient) value).getName() + " (ID: " + ((Patient) value).getPatientId() + ")");
                } else if (value == null) {
                    setText("Select a patient");
                }
                return this;
            }
        });
        formPanel.add(patientComboBox, gbc);

        // Days Stayed
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createStyledLabel("Days Stayed:"), gbc);
        gbc.gridx = 1;
        daysStayedField = createStyledTextField();
        daysStayedField.setEditable(false);
        formPanel.add(daysStayedField, gbc);

        // Bed Charges
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createStyledLabel("Bed Charges (₹):"), gbc);
        gbc.gridx = 1;
        bedChargeField = createStyledTextField();
        bedChargeField.setEditable(false);
        formPanel.add(bedChargeField, gbc);

        // Service Charges
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createStyledLabel("Service Charges (₹):"), gbc);
        gbc.gridx = 1;
        serviceChargeField = createStyledTextField();
        serviceChargeField.setEditable(false);
        formPanel.add(serviceChargeField, gbc);

        // Doctor's Fee
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(createStyledLabel("Doctor's Fee (₹):"), gbc);
        gbc.gridx = 1;
        doctorFeeField = createStyledTextField();
        doctorFeeField.setEditable(false);
        formPanel.add(doctorFeeField, gbc);

        // Total
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(createStyledLabel("Total (₹):"), gbc);
        gbc.gridx = 1;
        totalField = createStyledTextField();
        totalField.setEditable(false);
        totalField.setFont(new Font("Segoe UI", Font.BOLD, 12));
        totalField.setBackground(new Color(255, 255, 200));
        formPanel.add(totalField, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 6;
        previewBillButton = createStyledButton("PREVIEW BILL", new Color(255, 140, 0));
        previewBillButton.addActionListener(e -> previewBill());
        formPanel.add(previewBillButton, gbc);

        gbc.gridx = 1;
        generateBillButton = createStyledButton("GENERATE BILL & DISCHARGE", new Color(34, 139, 34));
        generateBillButton.addActionListener(e -> generateBill());
        formPanel.add(generateBillButton, gbc);

        // Refresh Button
        gbc.gridx = 1; gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.EAST;
        JButton refreshButton = createStyledButton("REFRESH LISTS", new Color(148, 0, 211));
        refreshButton.addActionListener(e -> {
            loadAdmittedPatients();
            loadBillingHistory();
        });
        formPanel.add(refreshButton, gbc);

        return formPanel;
    }

    private JPanel createHistoryPanel() {
        JPanel historyPanel = new JPanel(new BorderLayout(10, 10));
        historyPanel.setBorder(BorderFactory.createTitledBorder("Billing History (Double-click to view details)"));
        historyPanel.setBackground(Color.WHITE);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(createStyledLabel("Search by Patient Name:"));
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchPanel.add(searchField);
        historyPanel.add(searchPanel, BorderLayout.NORTH);

        String[] columnNames = {"Bill ID", "Patient Name", "Total (₹)", "Bill Date"};
        historyTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        historyTable = new JTable(historyTableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setRowHeight(25);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JTableHeader header = historyTable.getTableHeader();
        header.setBackground(new Color(148, 0, 211));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));

        sorter = new TableRowSorter<>(historyTableModel);
        historyTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        historyPanel.add(scrollPane, BorderLayout.CENTER);

        historyTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openBillDetails();
            }
        });

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText();
                sorter.setRowFilter(text.trim().isEmpty() ? null : RowFilter.regexFilter("(?i)" + text, 1));
            }
        });

        return historyPanel;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return field;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { button.setBackground(color.darker()); }
            public void mouseExited(java.awt.event.MouseEvent evt) { button.setBackground(color); }
        });
        return button;
    }

    public void loadBillingHistory() {
        try {
            historyTableModel.setRowCount(0);
            billingHistory = dataAccess.getBillingHistory();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            double totalRevenue = 0;
            for (Bill bill : billingHistory) {
                historyTableModel.addRow(new Object[]{
                        bill.getBillId(),
                        bill.getPatientName(),
                        String.format("₹%.2f", bill.getTotal()),
                        df.format(bill.getBillDate())
                });
                totalRevenue += bill.getTotal();
            }
            totalRevenueLabel.setText(String.format("Total Revenue: ₹%.2f", totalRevenue));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading billing history: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadAdmittedPatients() {
        try {
            Object selectedItem = patientComboBox.getSelectedItem();
            patientComboBox.removeAllItems();
            List<Patient> patients = dataAccess.getAdmittedPatients();

            if (patients.isEmpty()) {
                patientComboBox.addItem(null);
            } else {
                for (Patient p : patients) patientComboBox.addItem(p);
            }

            if (selectedItem instanceof Patient) {
                for (int i = 0; i < patientComboBox.getItemCount(); i++) {
                    if (patientComboBox.getItemAt(i) != null &&
                        ((Patient) patientComboBox.getItemAt(i)).getPatientId() == ((Patient) selectedItem).getPatientId()) {
                        patientComboBox.setSelectedItem(patientComboBox.getItemAt(i));
                        break;
                    }
                }
            }

            if (patientComboBox.getSelectedIndex() == -1 && patientComboBox.getItemCount() > 0) {
                patientComboBox.setSelectedIndex(0);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading patients: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openBillDetails() {
        int row = historyTable.getSelectedRow();
        if (row == -1) return;
        int modelRow = historyTable.convertRowIndexToModel(row);
        Bill selected = billingHistory.get(modelRow);
        BillDialog dialog = new BillDialog((Frame) SwingUtilities.getWindowAncestor(this), selected, dataAccess);
        dialog.setVisible(true);
    }

    private void previewBill() {
        Patient selected = (Patient) patientComboBox.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Patient details = dataAccess.getPatientById(selected.getPatientId());
            if (details == null || details.getAdmittedDate() == null) {
                JOptionPane.showMessageDialog(this, "Could not fetch patient's admission details.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            long diff = new Date().getTime() - details.getAdmittedDate().getTime();
            long days = Math.max(1, TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));

            double pricePerDay = details.getPricePerDay();
            double bedCharge = Math.max(0, days * pricePerDay);

            double doctorFee = 0.0;
            if (details.getDoctorId() != 0) {
                Doctor doc = dataAccess.getDoctorById(details.getDoctorId());
                if (doc != null) doctorFee = doc.getConsultationFee();
            }

            daysStayedField.setText(String.valueOf(days));
            bedChargeField.setText(String.format("%.2f", bedCharge));
            serviceChargeField.setText(String.format("%.2f", BASE_SERVICE_CHARGE));
            doctorFeeField.setText(String.format("%.2f", doctorFee));
            totalField.setText(String.format("%.2f", bedCharge + BASE_SERVICE_CHARGE + doctorFee));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching patient details: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateBill() {
        if (totalField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please preview the bill first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Patient selected = (Patient) patientComboBox.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "No patient selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Bill bill = new Bill();
        bill.setPatientId(selected.getPatientId());
        bill.setBedCharge(Double.parseDouble(bedChargeField.getText()));
        bill.setServiceCharge(Double.parseDouble(serviceChargeField.getText()));
        bill.setDoctorFee(Double.parseDouble(doctorFeeField.getText()));
        bill.setTotal(Double.parseDouble(totalField.getText()));
        bill.setBillDate(new Date());

        int choice = JOptionPane.showConfirmDialog(this,
                "This will discharge the patient " + selected.getName() + " and free their bed.\nAre you sure?",
                "Confirm Discharge", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                if (dataAccess.dischargePatient(selected.getPatientId(), bill)) {
                    JOptionPane.showMessageDialog(this, "Bill generated and patient discharged successfully!");
                    clearFields();
                    loadAdmittedPatients();
                    loadBillingHistory();

                    // --- NEW: Refresh the patient list on main panel ---
                    if (patientPanel != null) {
                        patientPanel.loadPatients();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Operation failed.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database Error during discharge: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearFields() {
        daysStayedField.setText("");
        bedChargeField.setText("");
        serviceChargeField.setText("");
        doctorFeeField.setText("");
        totalField.setText("");
        if (patientComboBox.getItemCount() > 0) {
            patientComboBox.setSelectedIndex(0);
        }
    }
}
