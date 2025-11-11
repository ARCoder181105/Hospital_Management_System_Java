package ui;

import dal.DataAccess;
import model.Bill;
import model.Patient;
import util.PdfGenerator;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
// import java.util.concurrent.TimeUnit;

public class BillDialog extends JDialog {
    private Bill bill;
    private Patient patient;
    private DataAccess dataAccess;
    private JButton downloadPdfButton;
    private JButton closeButton;

    public BillDialog(Frame owner, Bill bill, DataAccess dataAccess) {
        super(owner, "Bill Details", true);
        this.bill = bill;
        this.dataAccess = dataAccess;
        
        setSize(500, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // Fetch full patient details
        try {
            // This method now fetches all details needed for the PDF
            this.patient = dataAccess.getPatientById(bill.getPatientId());
            
            if (this.patient == null) {
                // If patient was deleted, create a placeholder
                this.patient = new Patient();
                this.patient.setName(bill.getPatientName());
                this.patient.setPatientId(bill.getPatientId());
                // We don't have illness/admitted date, but that's ok
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            this.patient = new Patient(); // Failsafe
            this.patient.setName(bill.getPatientName());
            this.patient.setPatientId(bill.getPatientId());
        }

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createDetailsPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(148, 0, 211));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel titleLabel = new JLabel("Patient Invoice");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        return headerPanel;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Helper to add rows
        addDetailRow(panel, gbc, 0, "Patient Name:", patient.getName());
        addDetailRow(panel, gbc, 1, "Patient ID:", String.valueOf(patient.getPatientId()));
        addDetailRow(panel, gbc, 2, "Bill ID:", String.valueOf(bill.getBillId()));
        addDetailRow(panel, gbc, 3, "Bill Date:", new SimpleDateFormat("yyyy-MM-dd").format(bill.getBillDate()));
        
        // Separator
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 10, 0);
        panel.add(new JSeparator(), gbc);
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = 1;

        // Itemized list
        addDetailRow(panel, gbc, 5, "Bed Charges:", String.format("Rs. %.2f", bill.getBedCharge()));
        addDetailRow(panel, gbc, 6, "Service Charges:", String.format("Rs. %.2f", bill.getServiceCharge()));
        addDetailRow(panel, gbc, 7, "Doctor's Fee:", String.format("Rs. %.2f", bill.getDoctorFee()));

        // Separator
        gbc.gridy = 8;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 10, 0);
        panel.add(new JSeparator(), gbc);
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = 1;

        // Total
        gbc.gridy = 9;
        gbc.gridx = 0;
        JLabel totalLabel = new JLabel("TOTAL AMOUNT");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(totalLabel, gbc);
        
        gbc.gridx = 1;
        JLabel totalValue = new JLabel(String.format("Rs. %.2f", bill.getTotal()));
        totalValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalValue.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(totalValue, gbc);

        return panel;
    }
    
    private void addDetailRow(JPanel panel, GridBagConstraints gbc, int y, String label, String value) {
        gbc.gridy = y;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lbl, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0.7;
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        val.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(val, gbc);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));
        
        downloadPdfButton = new JButton("Download PDF");
        downloadPdfButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        downloadPdfButton.setBackground(new Color(34, 139, 34)); // Green
        downloadPdfButton.setForeground(Color.WHITE);
        downloadPdfButton.addActionListener(e -> downloadPdf());
        panel.add(downloadPdfButton);
        
        closeButton = new JButton("Close");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeButton.setBackground(new Color(108, 117, 125)); // Gray
        closeButton.setForeground(Color.WHITE);
        closeButton.addActionListener(e -> setVisible(false));
        panel.add(closeButton);
        
        return panel;
    }
    
    // [START] UPDATED METHOD: downloadPdf
    private void downloadPdf() {
        try {
            // We use the patient object we fetched in the constructor
            File pdfFile = PdfGenerator.generateBillPdf(bill, this.patient);
            
            // --- UPDATED MESSAGE ---
            JOptionPane.showMessageDialog(this, 
                "PDF saved successfully to:\n" + pdfFile.getAbsolutePath(), 
                "Download Complete", 
                JOptionPane.INFORMATION_MESSAGE);
            // --- END UPDATED MESSAGE ---
                
            PdfGenerator.openPdf(pdfFile); // Try to open the file
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    // [END] UPDATED METHOD: downloadPdf
}