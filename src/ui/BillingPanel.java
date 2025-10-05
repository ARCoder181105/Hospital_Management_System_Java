package ui;

import dal.DataAccess;
import model.Bill;
import model.Patient;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BillingPanel extends JPanel {
    private final DataAccess dataAccess = new DataAccess();

    private static final double BED_RATE_PER_DAY = 1500.00;
    private static final double BASE_SERVICE_CHARGE = 500.00;
    private static final double DOCTOR_CONSULTATION_FEE = 1000.00;

    private JComboBox<Patient> patientComboBox;
    private JTextField bedChargeField, serviceChargeField, doctorFeeField, totalField, daysStayedField;
    private JButton previewBillButton, generateBillButton;

    // --- Components for Billing History ---
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;

    public BillingPanel() {
        setLayout(new BorderLayout());

        // --- Top Panel for Generating New Bills ---
        JPanel generationPanel = createGenerationPanel();

        // --- Bottom Panel for Viewing Billing History ---
        JPanel historyPanel = createHistoryPanel();
        
        // --- Split Pane to combine both panels ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, generationPanel, historyPanel);
        splitPane.setDividerLocation(320); // Initial separation
        add(splitPane, BorderLayout.CENTER);

        // Initial data load
        loadAdmittedPatients();
        loadBillingHistory();
        setupPatientComboBox();
    }
    
    private JPanel createGenerationPanel() {
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Generate Bill and Discharge Patient"));
        formPanel.setMinimumSize(new Dimension(0, 300)); // Set a minimum size

        formPanel.add(new JLabel("Select Admitted Patient:"));
        patientComboBox = new JComboBox<>();
        formPanel.add(patientComboBox);
        
        formPanel.add(new JLabel("Days Stayed:"));
        daysStayedField = new JTextField();
        daysStayedField.setEditable(false);
        formPanel.add(daysStayedField);

        formPanel.add(new JLabel("Bed Charges (₹):"));
        bedChargeField = new JTextField();
        bedChargeField.setEditable(false);
        formPanel.add(bedChargeField);

        formPanel.add(new JLabel("Service Charges (₹):"));
        serviceChargeField = new JTextField();
        serviceChargeField.setEditable(false);
        formPanel.add(serviceChargeField);

        formPanel.add(new JLabel("Doctor's Fee (₹):"));
        doctorFeeField = new JTextField();
        doctorFeeField.setEditable(false);
        formPanel.add(doctorFeeField);

        formPanel.add(new JLabel("Total (₹):"));
        totalField = new JTextField();
        totalField.setEditable(false);
        formPanel.add(totalField);

        previewBillButton = new JButton("Preview Bill");
        formPanel.add(previewBillButton);

        generateBillButton = new JButton("Generate Bill & Discharge");
        formPanel.add(generateBillButton);
        
        JButton refreshButton = new JButton("Refresh Lists");
        formPanel.add(new JLabel()); // Add a placeholder to align the button
        formPanel.add(refreshButton);

        refreshButton.addActionListener(e -> {
            loadAdmittedPatients();
            loadBillingHistory();
        });

        previewBillButton.addActionListener(e -> previewBill());
        generateBillButton.addActionListener(e -> generateBill());

        return formPanel;
    }

    private JPanel createHistoryPanel() {
        JPanel historyPanel = new JPanel(new BorderLayout(10, 10));
        historyPanel.setBorder(BorderFactory.createTitledBorder("Billing History"));

        // Search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search by Patient Name:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);
        historyPanel.add(searchPanel, BorderLayout.NORTH);
        
        // History Table
        String[] columnNames = {"Bill ID", "Patient Name", "Total (₹)", "Bill Date"};
        historyTableModel = new DefaultTableModel(columnNames, 0);
        historyTable = new JTable(historyTableModel);
        sorter = new TableRowSorter<>(historyTableModel);
        historyTable.setRowSorter(sorter);

        historyPanel.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        // Search functionality
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    // Case-insensitive search on column 1 (Patient Name)
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1));
                }
            }
        });
        
        return historyPanel;
    }

    private void loadBillingHistory() {
        try {
            historyTableModel.setRowCount(0); // Clear existing data
            List<Bill> history = dataAccess.getBillingHistory();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            for (Bill bill : history) {
                Object[] row = {
                    bill.getBillId(),
                    bill.getPatientName(),
                    String.format("%.2f", bill.getTotal()),
                    dateFormat.format(bill.getBillDate())
                };
                historyTableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading billing history: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void setupPatientComboBox() {
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
    }

    public void loadAdmittedPatients() {
        patientComboBox.removeAllItems();
        try {
            for (Patient p : dataAccess.getAdmittedPatients()) {
                patientComboBox.addItem(p);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading patients: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void previewBill() {
        Patient selectedPatient = (Patient) patientComboBox.getSelectedItem();
        if (selectedPatient == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Patient fullPatientDetails = dataAccess.getPatientById(selectedPatient.getPatientId());
            long diff = new Date().getTime() - fullPatientDetails.getAdmittedDate().getTime();
            long daysStayed = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            daysStayed = (daysStayed == 0) ? 1 : daysStayed;

            double bedCharge = daysStayed * BED_RATE_PER_DAY;
            
            daysStayedField.setText(String.valueOf(daysStayed));
            bedChargeField.setText(String.format("%.2f", bedCharge));
            serviceChargeField.setText(String.format("%.2f", BASE_SERVICE_CHARGE));
            doctorFeeField.setText(String.format("%.2f", DOCTOR_CONSULTATION_FEE));
            totalField.setText(String.format("%.2f", bedCharge + BASE_SERVICE_CHARGE + DOCTOR_CONSULTATION_FEE));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching patient details: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateBill() {
        if (totalField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please preview the bill first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Patient selectedPatient = (Patient) patientComboBox.getSelectedItem();
        Bill bill = new Bill();
        bill.setPatientId(selectedPatient.getPatientId());
        bill.setBedCharge(Double.parseDouble(bedChargeField.getText()));
        bill.setServiceCharge(Double.parseDouble(serviceChargeField.getText()));
        bill.setDoctorFee(Double.parseDouble(doctorFeeField.getText()));
        bill.setTotal(Double.parseDouble(totalField.getText()));
        bill.setBillDate(new Date());

        int choice = JOptionPane.showConfirmDialog(this, "This will discharge the patient and free their bed. Are you sure?", "Confirm Discharge", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                if (dataAccess.dischargePatient(selectedPatient.getPatientId(), bill)) {
                    JOptionPane.showMessageDialog(this, "Bill generated and patient discharged successfully!");
                    clearFields();
                    loadAdmittedPatients();
                    loadBillingHistory(); // REFRESH THE HISTORY TABLE
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
    }
}