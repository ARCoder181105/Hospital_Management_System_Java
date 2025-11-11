package ui;

import dal.DataAccess;
import model.Employee;
import model.Patient;
import model.Bed;
import model.Appointment;
import javax.swing.*;
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

    // Components for appointments
    private JList<String> appointmentList;
    private DefaultListModel<String> appointmentListModel;

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
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(700);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel tablePanel = createTablePanel();
        
        // Right panel is now a split pane itself
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplitPane.setDividerLocation(350);
        rightSplitPane.setBorder(BorderFactory.createEmptyBorder());
        
        detailsPanel = createDetailsPanel();
        rightSplitPane.setTopComponent(detailsPanel);
        rightSplitPane.setBottomComponent(createAppointmentListPanel());
        
        splitPane.setLeftComponent(tablePanel);
        splitPane.setRightComponent(rightSplitPane); // Set the new split pane

        return splitPane;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(BorderFactory.createTitledBorder("My Assigned Patients"));
        tablePanel.setBackground(Color.WHITE);

        // Search Bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(new JLabel("Search Patient:"));
        searchField = new JTextField(30);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchPanel.add(searchField);
        tablePanel.add(searchPanel, BorderLayout.NORTH);

        // Table
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

        // Search functionality
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }

            private void filter() {
                String text = searchField.getText();
                sorter.setRowFilter(text.trim().isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
            }
        });

        // Selection listener
        patientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && patientTable.getSelectedRow() != -1) {
                populateDetailsPanel();
            }
        });

        return tablePanel;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Patient Details"));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(350, 0));

        // Labels
        detailNameLabel = createDetailLabel(" ", 18, Font.BOLD);
        detailAgeGenderLabel = createDetailLabel(" ", 14, Font.PLAIN);
        detailAdmittedLabel = createDetailLabel(" ", 14, Font.PLAIN);
        detailIllnessLabel = createDetailLabel(" ", 14, Font.PLAIN);
        detailSeverityLabel = createDetailLabel(" ", 14, Font.BOLD);
        detailBedLabel = createDetailLabel(" ", 14, Font.PLAIN);

        // Layout
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
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", style, size));
        label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        return label;
    }

    private JPanel createDetailSection(String title, JLabel contentLabel) {
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

    private void clearDetailsPanel() {
        detailNameLabel.setText("No patient selected");
        detailAgeGenderLabel.setText(" ");
        detailIllnessLabel.setText(" ");
        detailSeverityLabel.setText(" ");
        detailBedLabel.setText(" ");
        detailAdmittedLabel.setText(" ");
        detailSeverityLabel.setForeground(Color.BLACK);
    }

    private void populateDetailsPanel() {
        int selectedRow = patientTable.convertRowIndexToModel(patientTable.getSelectedRow());
        if (selectedRow == -1) {
            clearDetailsPanel();
            return;
        }

        try {
            int patientId = (int) tableModel.getValueAt(selectedRow, 0);
            Patient patient = dataAccess.getPatientById(patientId);
            Bed bed = dataAccess.getBedByPatientId(patientId);

            detailNameLabel.setText(patient.getName());
            detailAgeGenderLabel.setText(patient.getAge() + " years / " + patient.getGender());
            detailAdmittedLabel.setText(new SimpleDateFormat("yyyy-MM-dd").format(patient.getAdmittedDate()));

            // Show correct illness name
            String illness = (patient.getOtherIllnessText() != null && !patient.getOtherIllnessText().isEmpty())
                    ? patient.getOtherIllnessText()
                    : patient.getIllnessName();
            detailIllnessLabel.setText(illness);

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

    private void loadMyPatients() {
        try {
            tableModel.setRowCount(0);
            // This line correctly uses getEmployeeId()
            List<Patient> patients = dataAccess.getPatientsByDoctorId(currentDoctor.getEmployeeId());
            for (Patient p : patients) {
                Object[] row = {
                        p.getPatientId(),
                        p.getName(),
                        p.getAge(),
                        (p.getOtherIllnessText() != null && !p.getOtherIllnessText().isEmpty())
                                ? p.getOtherIllnessText()
                                : p.getIllnessName(),
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
    
    private JPanel createAppointmentListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Today's Appointments"));
        panel.setBackground(Color.WHITE);
        panel.setMinimumSize(new Dimension(0, 150)); // Ensure it has some height

        appointmentListModel = new DefaultListModel<>();
        appointmentList = new JList<>(appointmentListModel);
        appointmentList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        panel.add(new JScrollPane(appointmentList), BorderLayout.CENTER);
        return panel;
    }
    
    private void loadAppointmentsForToday() {
        try {
            appointmentListModel.clear();
            
            // [START] THE FIX
            // Changed currentDoctor.getDoctorId() to currentDoctor.getEmployeeId()
            // The Employee ID is the Doctor ID.
            List<Appointment> appointments = dataAccess.getAppointmentsByDoctorAndDate(currentDoctor.getEmployeeId(), new Date());
            // [END] THE FIX
            
            if (appointments.isEmpty()) {
                appointmentListModel.addElement("No scheduled appointments for today.");
            } else {
                for (Appointment appt : appointments) {
                    appointmentListModel.addElement(
                        String.format("%s - %s", appt.getAppointmentTime(), appt.getPatientName())
                    );
                }
            }
        } catch (SQLException e) {
            appointmentListModel.addElement("Error loading appointments.");
            e.printStackTrace();
        }
    }
}