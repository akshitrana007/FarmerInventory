package org.example;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.Color;
import java.awt.Font;
import java.util.Enumeration;

public class Main {
    public static void main(String[] args) {

        // Step 1: Apply FlatLaf modern look and feel
        FlatLightLaf.setup();

        // Step 2: Tint FlatLaf's accent color to match our green farm theme
        UIManager.put("Component.accentColor", new Color(76, 145, 65));
        UIManager.put("Button.default.background", new Color(76, 145, 65));
        UIManager.put("Button.default.foreground", Color.WHITE);
        UIManager.put("ProgressBar.foreground", new Color(76, 145, 65));
        UIManager.put("TabbedPane.underlineColor", new Color(76, 145, 65));

        // Step 3: Force Nirmala UI as the default font for EVERY Swing component
        // (buttons, labels, tables, popups, etc.) so Hindi/Bhojpuri text renders
        // correctly everywhere. This must run AFTER FlatLaf.setup(), since FlatLaf
        // sets its own fonts — we override them here so our language support keeps working.
        Font defaultFont = new Font("Nirmala UI", Font.PLAIN, 13);
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof Font) {
                UIManager.put(key, defaultFont);
            }
        }

        SwingUtilities.invokeLater(RoleSelectionFrame::new);
    }
}