package ui;

import dal.DataAccess;
import model.Employee;
import model.Patient;
import model.Bed;
import javax.swing.*;
// import javax.swing.event.ListSelectionEvent;
// import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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

    public DoctorPortalPanel(Employee doctor) {
        this.currentDoctor = doctor;
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 245));

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main Content (Split Pane)
        JSplitPane splitPane = createMainSplitPane();
        add(splitPane, BorderLayout.CENTER);

        loadMyPatients();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180)); // Doctor panel color
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

        // Left Panel (Table)
        JPanel tablePanel = createTablePanel();
        splitPane.setLeftComponent(tablePanel);

        // Right Panel (Details)
        detailsPanel = createDetailsPanel();
        splitPane.setRightComponent(detailsPanel);

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
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
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
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        // Table selection listener
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
        panel.setPreferredSize(new Dimension(350, 0)); // Width is respected

        // Initialize labels
        detailNameLabel = createDetailLabel(" ", 18, Font.BOLD);
        detailAgeGenderLabel = createDetailLabel(" ", 14, Font.PLAIN);
        detailAdmittedLabel = createDetailLabel(" ", 14, Font.PLAIN);
        detailIllnessLabel = createDetailLabel(" ", 14, Font.PLAIN);
        detailSeverityLabel = createDetailLabel(" ", 14, Font.BOLD);
        detailBedLabel = createDetailLabel(" ", 14, Font.PLAIN);

        // Add labels to panel with spacing
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
        
        panel.add(Box.createVerticalGlue()); // Pushes content to the top
        
        clearDetailsPanel();
        return panel;
    }

    // Helper to create a detail label
    private JLabel createDetailLabel(String text, int size, int style) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", style, size));
        label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        return label;
    }

    // Helper to create a titled section
    private JPanel createDetailSection(String title, JLabel contentLabel) {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(Color.WHITE);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); // Constrain height
        
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
            // We fetch the full patient and bed details for the panel
            Patient patient = dataAccess.getPatientById(patientId);
            Bed bed = dataAccess.getBedByPatientId(patientId);

            detailNameLabel.setText(patient.getName());
            detailAgeGenderLabel.setText(patient.getAge() + " years old / " + patient.getGender());
            detailAdmittedLabel.setText(new SimpleDateFormat("yyyy-MM-dd").format(patient.getAdmittedDate()));
            detailIllnessLabel.setText(patient.getIllness());
            detailSeverityLabel.setText(patient.getDiseaseSeverity());

            // Set severity color
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
                detailBedLabel.setText("Bed ID: " + bed.getBedId() + " (Floor " + bed.getFloor() + ", " + bed.getBedType() + ")");
            } else {
                detailBedLabel.setText("Unassigned (Outpatient)");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching patient details: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadMyPatients() {
        try {
            tableModel.setRowCount(0);
            List<Patient> patients = dataAccess.getPatientsByDoctorId(currentDoctor.getEmployeeId());
            for (Patient p : patients) {
                Object[] row = {
                    p.getPatientId(),
                    p.getName(),
                    p.getAge(),
                    p.getIllness(),
                    p.getDiseaseSeverity(),
                    p.getBedId() == 0 ? "N/A" : p.getBedId()
                };
                tableModel.addRow(row);
            }
            patientCountLabel.setText("My Patients: " + patients.size());
            clearDetailsPanel();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading patients: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}