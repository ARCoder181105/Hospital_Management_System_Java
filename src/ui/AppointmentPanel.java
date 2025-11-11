package ui;

import dal.DataAccess;
import model.Appointment;
import model.Doctor;
import model.Patient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AppointmentPanel extends JPanel {
    private final DataAccess dataAccess = new DataAccess();

    // Components for the form
    private JComboBox<Patient> patientComboBox;
    private JComboBox<Doctor> doctorComboBox;
    private JSpinner dateSpinner;
    private JComboBox<String> timeComboBox;
    private JButton saveButton;
    private JButton clearButton;
    private JLabel formTitleLabel;

    // Components for the table
    private JTable appointmentTable;
    private DefaultTableModel tableModel;
    private JSpinner viewDateSpinner;
    private JButton viewButton;
    private JButton rescheduleButton;
    private JButton cancelButton;

    // State
    private int selectedAppointmentId = -1;

    private final String[] TIME_SLOTS = {
            "09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM",
            "01:00 PM", "01:30 PM", "02:00 PM", "02:30 PM", "03:00 PM", "03:30 PM", "04:00 PM"
    };

    public AppointmentPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 245));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(250);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        splitPane.setTopComponent(createFormPanel());
        splitPane.setBottomComponent(createTablePanel());

        add(splitPane, BorderLayout.CENTER);

        loadAllPatients();
        loadAllDoctors();
        loadAppointmentsForDate(new Date());
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new BorderLayout(10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Manage Appointment"));
        formPanel.setBackground(Color.WHITE);

        formTitleLabel = new JLabel("Book New Appointment", SwingConstants.LEFT);
        formTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
        formPanel.add(formTitleLabel, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Patient
        gbc.gridx = 0; gbc.gridy = 0;
        fieldsPanel.add(new JLabel("Select Patient:"), gbc);
        gbc.gridx = 1;
        patientComboBox = new JComboBox<>();
        patientComboBox.setBackground(Color.WHITE);
        fieldsPanel.add(patientComboBox, gbc);

        // Doctor
        gbc.gridx = 0; gbc.gridy = 1;
        fieldsPanel.add(new JLabel("Select Doctor:"), gbc);
        gbc.gridx = 1;
        doctorComboBox = new JComboBox<>();
        doctorComboBox.setBackground(Color.WHITE);
        fieldsPanel.add(doctorComboBox, gbc);

        // Date
        gbc.gridx = 0; gbc.gridy = 2;
        fieldsPanel.add(new JLabel("Select Date:"), gbc);
        gbc.gridx = 1;
        dateSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        fieldsPanel.add(dateSpinner, gbc);

        // Time
        gbc.gridx = 0; gbc.gridy = 3;
        fieldsPanel.add(new JLabel("Select Time:"), gbc);
        gbc.gridx = 1;
        timeComboBox = new JComboBox<>(TIME_SLOTS);
        timeComboBox.setBackground(Color.WHITE);
        fieldsPanel.add(timeComboBox, gbc);

        // Buttons
        gbc.gridx = 1; gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setBackground(Color.WHITE);

        saveButton = new JButton("Save Appointment");
        saveButton.setBackground(new Color(37, 99, 235));
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> saveAppointment());
        buttonPanel.add(saveButton);

        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearForm());
        buttonPanel.add(clearButton);
        fieldsPanel.add(buttonPanel, gbc);

        formPanel.add(fieldsPanel, BorderLayout.CENTER);
        return formPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Scheduled Appointments"));
        tablePanel.setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(new JLabel("View Appointments for Date:"));

        viewDateSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(viewDateSpinner, "yyyy-MM-dd");
        viewDateSpinner.setEditor(dateEditor);
        topPanel.add(viewDateSpinner);

        viewButton = new JButton("View");
        viewButton.addActionListener(e -> loadAppointmentsForDate((Date) viewDateSpinner.getValue()));
        topPanel.add(viewButton);

        rescheduleButton = new JButton("Reschedule Selected");
        rescheduleButton.setBackground(new Color(255, 140, 0));
        rescheduleButton.setForeground(Color.WHITE);
        rescheduleButton.addActionListener(e -> populateFormForReschedule());
        rescheduleButton.setEnabled(false);
        topPanel.add(rescheduleButton);

        cancelButton = new JButton("Cancel Selected");
        cancelButton.setBackground(new Color(220, 53, 69));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.addActionListener(e -> cancelAppointment());
        cancelButton.setEnabled(false);
        topPanel.add(cancelButton);

        tablePanel.add(topPanel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Time", "Patient Name", "Doctor Name", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        appointmentTable = new JTable(tableModel);
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentTable.setRowHeight(25);
        appointmentTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        appointmentTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = appointmentTable.getSelectedRow() != -1;
            cancelButton.setEnabled(rowSelected);
            rescheduleButton.setEnabled(rowSelected);
        });

        JTableHeader header = appointmentTable.getTableHeader();
        header.setBackground(new Color(108, 117, 125));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));

        tablePanel.add(new JScrollPane(appointmentTable), BorderLayout.CENTER);
        return tablePanel;
    }

    // --- Data Loading Methods ---

    public void loadAllPatients() {
        try {
            patientComboBox.removeAllItems();
            List<Patient> patients = dataAccess.getAllPatients();
            for (Patient p : patients) {
                patientComboBox.addItem(p);
            }
        } catch (SQLException e) {
            showError("Could not load patients: " + e.getMessage());
        }
    }

    public void loadAllDoctors() {
        try {
            doctorComboBox.removeAllItems();
            List<Doctor> doctors = dataAccess.getAllDoctors();
            for (Doctor d : doctors) {
                doctorComboBox.addItem(d);
            }
        } catch (SQLException e) {
            showError("Could not load doctors: " + e.getMessage());
        }
    }

    private void loadAppointmentsForDate(Date date) {
        try {
            tableModel.setRowCount(0);
            List<Appointment> appointments = dataAccess.getScheduledAppointments(date);
            for (Appointment appt : appointments) {
                tableModel.addRow(new Object[]{
                        appt.getAppointmentId(),
                        appt.getAppointmentTime(),
                        appt.getPatientName(),
                        appt.getDoctorName(),
                        appt.getStatus()
                });
            }
        } catch (SQLException e) {
            showError("Could not load appointments: " + e.getMessage());
        }
    }

    // --- Save / Update / Cancel / Reschedule Logic ---

    private void saveAppointment() {
        Patient selectedPatient = (Patient) patientComboBox.getSelectedItem();
        Doctor selectedDoctor = (Doctor) doctorComboBox.getSelectedItem();
        Date selectedDate = (Date) dateSpinner.getValue();
        String selectedTime = (String) timeComboBox.getSelectedItem();

        if (selectedPatient == null || selectedDoctor == null) {
            showError("Please select a patient and a doctor.");
            return;
        }

        try {
            String dayAbbr = new SimpleDateFormat("EEE").format(selectedDate);
            String availableDays = selectedDoctor.getAvailableDays();
            if (availableDays == null || !availableDays.contains(dayAbbr)) {
                showError("Dr. " + selectedDoctor.getName() + " is not available on " + dayAbbr + "s.");
                return;
            }

            List<String> bookedSlots = dataAccess.getBookedSlots(selectedDoctor.getDoctorId(), selectedDate);
            if (bookedSlots.contains(selectedTime)) {
                int conflictingApptId = dataAccess.getAppointmentBySlot(selectedDoctor.getDoctorId(), selectedDate, selectedTime);
                if (conflictingApptId != selectedAppointmentId) {
                    showError("This time slot is already booked for Dr. " + selectedDoctor.getName() + ".");
                    return;
                }
            }

            Appointment appt = new Appointment();
            appt.setPatientId(selectedPatient.getPatientId());
            appt.setDoctorId(selectedDoctor.getDoctorId());
            appt.setAppointmentDate(selectedDate);
            appt.setAppointmentTime(selectedTime);

            boolean success;
            if (selectedAppointmentId == -1) {
                success = dataAccess.addAppointment(appt);
            } else {
                appt.setAppointmentId(selectedAppointmentId);
                success = dataAccess.updateAppointment(appt);
            }

            if (success) {
                String message = (selectedAppointmentId == -1)
                        ? "Appointment booked successfully!"
                        : "Appointment rescheduled successfully!";
                JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAppointmentsForDate((Date) viewDateSpinner.getValue());
                clearForm();
            } else {
                showError("Failed to save appointment.");
            }

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    private void populateFormForReschedule() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an appointment to reschedule.");
            return;
        }

        int appointmentId = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            Appointment appt = dataAccess.getAppointmentById(appointmentId);
            if (appt == null) {
                showError("Could not find appointment details.");
                return;
            }

            selectedAppointmentId = appt.getAppointmentId();
            formTitleLabel.setText("Reschedule Appointment (ID: " + appointmentId + ")");
            saveButton.setText("Save Changes");
            patientComboBox.setEnabled(false);
            doctorComboBox.setEnabled(false);

            for (int i = 0; i < patientComboBox.getItemCount(); i++) {
                if (patientComboBox.getItemAt(i).getPatientId() == appt.getPatientId()) {
                    patientComboBox.setSelectedIndex(i);
                    break;
                }
            }

            for (int i = 0; i < doctorComboBox.getItemCount(); i++) {
                if (doctorComboBox.getItemAt(i).getDoctorId() == appt.getDoctorId()) {
                    doctorComboBox.setSelectedIndex(i);
                    break;
                }
            }

            dateSpinner.setValue(appt.getAppointmentDate());
            timeComboBox.setSelectedItem(appt.getAppointmentTime());

        } catch (SQLException e) {
            showError("Error fetching appointment details: " + e.getMessage());
        }
    }

    private void cancelAppointment() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an appointment to cancel.");
            return;
        }

        int appointmentId = (int) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 4);

        if ("Cancelled".equals(status)) {
            showError("This appointment is already cancelled.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel this appointment?",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                if (dataAccess.cancelAppointment(appointmentId)) {
                    JOptionPane.showMessageDialog(this, "Appointment cancelled successfully.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadAppointmentsForDate((Date) viewDateSpinner.getValue());
                } else {
                    showError("Failed to cancel appointment.");
                }
            } catch (SQLException e) {
                showError("Database error: " + e.getMessage());
            }
        }
    }

    private void clearForm() {
        selectedAppointmentId = -1;
        formTitleLabel.setText("Book New Appointment");
        saveButton.setText("Save Appointment");
        patientComboBox.setEnabled(true);
        doctorComboBox.setEnabled(true);

        if (patientComboBox.getItemCount() > 0) patientComboBox.setSelectedIndex(0);
        if (doctorComboBox.getItemCount() > 0) doctorComboBox.setSelectedIndex(0);
        if (timeComboBox.getItemCount() > 0) timeComboBox.setSelectedIndex(0);
        dateSpinner.setValue(new Date());
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
