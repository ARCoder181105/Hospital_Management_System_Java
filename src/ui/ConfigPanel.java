package ui;

import javax.swing.*;
import java.awt.*;

public class ConfigPanel extends JPanel {

    public ConfigPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JTabbedPane configTabs = new JTabbedPane();
        configTabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Add the two new management panels
        ConfigBedPanel bedConfigPanel = new ConfigBedPanel();
        ConfigIllnessPanel illnessConfigPanel = new ConfigIllnessPanel();
        
        configTabs.addTab("  Manage Bed Types  ", bedConfigPanel);
        configTabs.addTab("  Manage Illness List  ", illnessConfigPanel);
        
        add(configTabs, BorderLayout.CENTER);
    }
}