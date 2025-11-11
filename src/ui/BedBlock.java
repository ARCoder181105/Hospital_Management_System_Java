package ui;

import model.Bed;
import javax.swing.*;
import java.awt.*;

public class BedBlock extends JPanel {

    private final Color COLOR_AVAILABLE = new Color(144, 238, 144);
    private final Color COLOR_OCCUPIED = new Color(255, 182, 193);
    
    private JLabel bedIdLabel;
    private JLabel bedTypeLabel;
    private JLabel patientNameLabel;

    public BedBlock(Bed bed) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        bedIdLabel = new JLabel();
        bedIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        bedTypeLabel = new JLabel();
        bedTypeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        patientNameLabel = new JLabel();
        patientNameLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        
        add(bedIdLabel);
        add(bedTypeLabel);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(patientNameLabel);
        
        setPreferredSize(new Dimension(150, 80));
        
        // Set initial data
        updateBed(bed);
    }

    public void updateBed(Bed bed) {
        // Set Bed ID and Type
        bedIdLabel.setText("Bed " + bed.getBedId());
        
        // [FIX] Changed from getBedType() to getBedTypeName()
        bedTypeLabel.setText(bed.getBedTypeName());

        // Set Patient and Color
        if ("Available".equals(bed.getStatus())) {
            patientNameLabel.setText("Available");
            setBackground(COLOR_AVAILABLE);
        } else {
            patientNameLabel.setText(bed.getPatientName() != null ? bed.getPatientName() : "Occupied");
            setBackground(COLOR_OCCUPIED);
        }
        
        // Build tooltip
        String toolTip = String.format(
            "<html><b>Bed ID:</b> %d<br>" +
            "<b>Floor:</b> %d<br>" +
            "<b>Ward:</b> %s<br>" +
            // [FIX] Changed from getBedType() to getBedTypeName()
            "<b>Type:</b> %s (Rs. %.2f/day)<br>" +
            "<b>Status:</b> %s<br>" +
            "<b>Patient:</b> %s</html>",
            bed.getBedId(),
            bed.getFloor(),
            bed.getWard(),
            bed.getBedTypeName(), // This was getBedType()
            bed.getPricePerDay(),
            bed.getStatus(),
            "Available".equals(bed.getStatus()) ? "N/A" : (bed.getPatientName() != null ? bed.getPatientName() : "Unknown") + " (ID: " + bed.getPatientId() + ")"
        );
        setToolTipText(toolTip);
    }
}