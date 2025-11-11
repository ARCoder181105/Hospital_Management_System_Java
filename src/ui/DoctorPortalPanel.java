package ui;

import dal.DataAccess;
import model.Employee;
import model.Patient;
import model.Bed;
import model.Appointment;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent; // NEW IMPORT
import javax.swing.event.ListSelectionListener; // NEW IMPORT
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DoctorPortalPanel extends JPanel {
    private final DataAccess dataAccess = new DataAccess();
    private final Employee currentDoctor;

    private JTable patientTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private JPanel detailsPanel;
    private JLabel patientCountLabel;

    // Labels for patient details
    private JLabel detailNameLabel;
    private JLabel detailAgeGenderLabel;
    private JLabel detailIllnessLabel;
    private JLabel detailSeverityLabel;
    private JLabel detailBedLabel;
    private JLabel detailAdmittedLabel;

    // [START] UPDATED: Changed from String to Appointment
    private JList<Appointment> appointmentList;
    private DefaultListModel<Appointment> appointmentListModel;
    // [END] UPDATED

    public DoctorPortalPanel(Employee doctor) {
        this.currentDoctor = doctor;
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 245));

        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Main Split Pane
        add(createMainSplitPane(), BorderLayout.CENTER);

        // Load data
        loadMyPatients();
        loadAppointmentsForToday();
    }

    private JPanel createHeaderPanel() {
        // ... (This method is unchanged)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JLabel titleLabel = new JLabel("DOCTOR'S PORTAL");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        patientCountLabel = new JLabel("My Patients: 0");
        patientCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        patientCountLabel.setForeground(Color.WHITE);
        headerPanel.add(patientCountLabel, BorderLayout.EAST);
        return headerPanel;
    }

    private JSplitPane createMainSplitPane() {
        // ... (This method is unchanged)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(700);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        JPanel tablePanel = createTablePanel();
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplitPane.setDividerLocation(350);
        rightSplitPane.setBorder(BorderFactory.createEmptyBorder());
        detailsPanel = createDetailsPanel();
        rightSplitPane.setTopComponent(detailsPanel);
        rightSplitPane.setBottomComponent(createAppointmentListPanel());
        splitPane.setLeftComponent(tablePanel);
        splitPane.setRightComponent(rightSplitPane);
        return splitPane;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        // [UPDATED] Title change
        tablePanel.setBorder(BorderFactory.createTitledBorder("My Assigned Patients (In-Patients)"));
        tablePanel.setBackground(Color.WHITE);
        
        // ... (Search Bar and Table setup are unchanged) ...
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(new JLabel("Search Patient:"));
        searchField = new JTextField(30);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchPanel.add(searchField);
        tablePanel.add(searchPanel, BorderLayout.NORTH);
        String[] columnNames = {"ID", "Name", "Age", "Illness", "Severity", "Bed ID"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        patientTable = new JTable(tableModel);
        patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientTable.setRowHeight(25);
        patientTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JTableHeader header = patientTable.getTableHeader();
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sorter = new TableRowSorter<>(tableModel);
        patientTable.setRowSorter(sorter);
        JScrollPane scrollPane = new JScrollPane(patientTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // ... (Search functionality is unchanged) ...
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText();
                sorter.setRowFilter(text.trim().isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
            }
        });

        // [START] UPDATED Selection listener for the TABLE
        patientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && patientTable.getSelectedRow() != -1) {
                // Get patient ID from the table
                int selectedRow = patientTable.convertRowIndexToModel(patientTable.getSelectedRow());
                int patientId = (int) tableModel.getValueAt(selectedRow, 0);
                
                // Populate details
                populateDetailsPanel(patientId);
                
                // Clear the *other* list's selection
                appointmentList.clearSelection();
            }
        });
        // [END] UPDATED Selection listener

        return tablePanel;
    }

    private JPanel createDetailsPanel() {
        // ... (This method is unchanged)
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Patient Details"));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(350, 0));
        detailNameLabel = createDetailLabel(" ", 18, Font.BOLD);
        detailAgeGenderLabel = createDetailLabel(" ", 14, Font.PLAIN);
        detailAdmittedLabel = createDetailLabel(" ", 14, Font.PLAIN);
        detailIllnessLabel = createDetailLabel(" ", 14, Font.PLAIN);
        detailSeverityLabel = createDetailLabel(" ", 14, Font.BOLD);
        detailBedLabel = createDetailLabel(" ", 14, Font.PLAIN);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createDetailSection("Name:", detailNameLabel));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createDetailSection("Age/Gender:", detailAgeGenderLabel));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createDetailSection("Admitted On:", detailAdmittedLabel));
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(new JSeparator());
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createDetailSection("Illness:", detailIllnessLabel));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createDetailSection("Severity:", detailSeverityLabel));
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(new JSeparator());
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createDetailSection("Location:", detailBedLabel));
        panel.add(Box.createVerticalGlue());
        clearDetailsPanel();
        return panel;
    }

    private JLabel createDetailLabel(String text, int size, int style) {
        // ... (This method is unchanged)
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", style, size));
        label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        return label;
    }

    private JPanel createDetailSection(String title, JLabel contentLabel) {
        // ... (This method is unchanged)
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(Color.WHITE);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(Color.GRAY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        section.add(titleLabel, BorderLayout.NORTH);
        section.add(contentLabel, BorderLayout.CENTER);
        return section;
    }

    // [START] UPDATED METHOD: clearDetailsPanel
    private void clearDetailsPanel() {
        detailNameLabel.setText("No patient selected");
        detailAgeGenderLabel.setText(" ");
        detailIllnessLabel.setText(" ");
        detailSeverityLabel.setText(" ");
        detailBedLabel.setText(" ");
        detailAdmittedLabel.setText(" ");
        detailSeverityLabel.setForeground(Color.BLACK);
        
        // [NEW] Clear both selections
        patientTable.clearSelection();
        if (appointmentList != null) {
            appointmentList.clearSelection();
        }
    }
    // [END] UPDATED METHOD

    // [START] UPDATED METHOD: Now takes patientId as a parameter
    private void populateDetailsPanel(int patientId) {
        try {
            // 1. Get the full patient object
            Patient patient = dataAccess.getPatientById(patientId);
            if (patient == null) {
                clearDetailsPanel();
                return;
            }
            
            // 2. Get their bed (if any)
            Bed bed = dataAccess.getBedByPatientId(patientId);

            // 3. Populate all labels
            detailNameLabel.setText(patient.getName());
            detailAgeGenderLabel.setText(patient.getAge() + " years / " + patient.getGender());
            
            if (patient.getAdmittedDate() != null) {
                 detailAdmittedLabel.setText(new SimpleDateFormat("yyyy-MM-dd").format(patient.getAdmittedDate()));
            } else {
                detailAdmittedLabel.setText("N/A (Outpatient)");
            }

            // Show correct illness name
            String illness = (patient.getOtherIllnessText() != null && !patient.getOtherIllnessText().isEmpty())
                    ? patient.getOtherIllnessText()
                    : patient.getIllnessName();
            detailIllnessLabel.setText(illness);

            // Set severity color
            if (patient.getDiseaseSeverity() != null) {
                detailSeverityLabel.setText(patient.getDiseaseSeverity());
                switch (patient.getDiseaseSeverity()) {
                    case "Severe":
                        detailSeverityLabel.setForeground(new Color(220, 53, 69));
                        break;
                    case "Moderate":
                        detailSeverityLabel.setForeground(new Color(255, 140, 0));
                        break;
                    default:
                        detailSeverityLabel.setForeground(new Color(34, 139, 34));
                        break;
                }
            } else {
                detailSeverityLabel.setText("N/A");
                detailSeverityLabel.setForeground(Color.BLACK);
            }


            if (bed != null) {
                detailBedLabel.setText("Bed " + bed.getBedId() + " (Floor " + bed.getFloor() + ", " + bed.getBedTypeName() + ")");
            } else {
                detailBedLabel.setText("Unassigned / Outpatient");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching patient details: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    // [END] UPDATED METHOD

    private void loadMyPatients() {
        // ... (This method is unchanged)
        try {
            tableModel.setRowCount(0);
            List<Patient> patients = dataAccess.getPatientsByDoctorId(currentDoctor.getEmployeeId());
            for (Patient p : patients) {
                Object[] row = {
                        p.getPatientId(), p.getName(), p.getAge(),
                        (p.getOtherIllnessText() != null && !p.getOtherIllnessText().isEmpty())
                                ? p.getOtherIllnessText() : p.getIllnessName(),
                        p.getDiseaseSeverity(),
                        p.getBedId() == 0 ? "N/A" : p.getBedId()
                };
                tableModel.addRow(row);
            }
            patientCountLabel.setText("My Patients: " + patients.size());
            clearDetailsPanel();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading patients: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // [START] UPDATED METHOD: createAppointmentListPanel
    private JPanel createAppointmentListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Today's Appointments (Outpatients)"));
        panel.setBackground(Color.WHITE);
        panel.setMinimumSize(new Dimension(0, 150));

        // Use Appointment object, not String
        appointmentListModel = new DefaultListModel<Appointment>();
        appointmentList = new JList<>(appointmentListModel);
        appointmentList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        appointmentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // [NEW] Add a custom renderer
        appointmentList.setCellRenderer(new AppointmentListRenderer());
        
        // [NEW] Add a selection listener to the list
        appointmentList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && appointmentList.getSelectedValue() != null) {
                // Get the patient ID from the selected appointment
                Appointment selectedAppt = appointmentList.getSelectedValue();
                
                // Do nothing if it's the "empty" message
                if (selectedAppt.getAppointmentTime() == null) {
                    return;
                }
                
                int patientId = selectedAppt.getPatientId();
                
                // Populate the details panel
                populateDetailsPanel(patientId);
                
                // Clear the *other* list's selection
                patientTable.clearSelection();
            }
        });
        
        panel.add(new JScrollPane(appointmentList), BorderLayout.CENTER);
        return panel;
    }
    // [END] UPDATED METHOD
    
    // [START] UPDATED METHOD: loadAppointmentsForToday
    private void loadAppointmentsForToday() {
        try {
            appointmentListModel.clear();
            List<Appointment> appointments = dataAccess.getAppointmentsByDoctorAndDate(currentDoctor.getEmployeeId(), new Date());
            
            if (appointments.isEmpty()) {
                // To show a message, we add a dummy Appointment object
                Appointment empty = new Appointment();
                empty.setPatientName("No scheduled appointments for today.");
                appointmentListModel.addElement(empty);
            } else {
                for (Appointment appt : appointments) {
                    // [FIX] Add the full object, not the string
                    appointmentListModel.addElement(appt);
                }
            }
        } catch (SQLException e) {
            Appointment error = new Appointment();
            error.setPatientName("Error loading appointments.");
            appointmentListModel.addElement(error);
            e.printStackTrace();
        }
    }
    // [END] UPDATED METHOD
    
    // [START] NEW INNER CLASS: AppointmentListRenderer
    /**
     * Custom renderer to display the Appointment object in the JList.
     * This allows us to store the full object but display a friendly string.
     */
    class AppointmentListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // Let the default renderer do its work
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Appointment) {
                Appointment appt = (Appointment) value;
                // If appointmentTime is null, it's our "empty" or "error" message
                if (appt.getAppointmentTime() != null) {
                    setText(String.format("%s - %s", appt.getAppointmentTime(), appt.getPatientName()));
                    setFont(getFont().deriveFont(Font.PLAIN));
                    setForeground(Color.BLACK);
                } else {
                    setText(appt.getPatientName()); // Show "No appointments..."
                    setFont(getFont().deriveFont(Font.ITALIC));
                    setForeground(Color.GRAY);
                }
            }
            return c;
        }
    }
    // [END] NEW INNER CLASS
}