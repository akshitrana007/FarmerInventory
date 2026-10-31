package org.example;

import javax.swing.*;
import java.awt.*;

public class RoleSelectionFrame extends JFrame {

    public RoleSelectionFrame() {
        setTitle(LanguageManager.get("role.window.title"));
        setSize(400, 330);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(240, 250, 240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Language dropdown panel
        JPanel langPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        langPanel.setBackground(new Color(240, 250, 240));
        JLabel langLabel = new JLabel(LanguageManager.get("role.language.label") + ":");
        langLabel.setFont(new Font("Nirmala UI", Font.PLAIN, 13));

        String[] languageOptions = {"English", "हिंदी", "भोजपुरी"};
        JComboBox<String> languageDropdown = new JComboBox<>(languageOptions);
        languageDropdown.setFont(new Font("Nirmala UI", Font.PLAIN, 13));

        // Pre-select dropdown based on current language
        String currentLang = LanguageManager.getCurrentLocale().getLanguage();
        if (currentLang.equals("hi")) {
            languageDropdown.setSelectedIndex(1);
        } else if (currentLang.equals("bho")) {
            languageDropdown.setSelectedIndex(2);
        } else {
            languageDropdown.setSelectedIndex(0);
        }

        langPanel.add(langLabel);
        langPanel.add(languageDropdown);
        add(langPanel, gbc);

        JLabel titleLabel = new JLabel(LanguageManager.get("role.heading"), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Nirmala UI", Font.BOLD, 18));
        add(titleLabel, gbc);

        JButton farmerButton = new JButton(LanguageManager.get("role.button.farmer"));
        farmerButton.setFont(new Font("Nirmala UI", Font.PLAIN, 16));
        farmerButton.setBackground(new Color(76, 140, 74));
        farmerButton.setForeground(Color.WHITE);
        add(farmerButton, gbc);

        JButton customerButton = new JButton(LanguageManager.get("role.button.customer"));
        customerButton.setFont(new Font("Nirmala UI", Font.PLAIN, 16));
        customerButton.setBackground(new Color(76, 140, 74));
        customerButton.setForeground(Color.WHITE);
        add(customerButton, gbc);

        // When user picks a new language, switch it and reload this screen
        languageDropdown.addActionListener(e -> {
            int selected = languageDropdown.getSelectedIndex();
            String langCode;
            if (selected == 1) {
                langCode = "hi";
            } else if (selected == 2) {
                langCode = "bho";
            } else {
                langCode = "en";
            }

            // Only reload if the language actually changed
            if (!langCode.equals(LanguageManager.getCurrentLocale().getLanguage())) {
                LanguageManager.setLanguage(langCode);
                dispose();
                new RoleSelectionFrame();
            }
        });

        farmerButton.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        customerButton.addActionListener(e -> {
            dispose();
            new CustomerLoginFrame();
        });

        setVisible(true);
    }
}