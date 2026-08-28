package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterFrame extends JFrame {

    private JTextField fullNameField, usernameField, contactField, locationField;
    private JPasswordField passwordField;
    private JLabel messageLabel;

    private static final Color PRIMARY_GREEN = new Color(76, 145, 65);
    private static final Color PRIMARY_GREEN_DARK = new Color(60, 120, 50);
    private static final Color BACKGROUND = new Color(245, 250, 240);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public RegisterFrame() {

        setTitle("Farmer Inventory System - Register");
        setSize(440, 520);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("New Farmer Registration");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_GREEN_DARK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 8, 20, 8);
        add(titleLabel, gbc);

        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridwidth = 1;

        gbc.gridy = 1;
        gbc.gridx = 0;
        add(styledLabel("Full Name:"), gbc);
        fullNameField = new JTextField(15);
        styleField(fullNameField);
        gbc.gridx = 1;
        add(fullNameField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        add(styledLabel("Username:"), gbc);
        usernameField = new JTextField(15);
        styleField(usernameField);
        gbc.gridx = 1;
        add(usernameField, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        add(styledLabel("Password:"), gbc);
        passwordField = new JPasswordField(15);
        styleField(passwordField);
        gbc.gridx = 1;
        add(passwordField, gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        add(styledLabel("Contact Number:"), gbc);
        contactField = new JTextField(15);
        styleField(contactField);
        gbc.gridx = 1;
        add(contactField, gbc);

        gbc.gridy = 5;
        gbc.gridx = 0;
        add(styledLabel("Farm Location:"), gbc);
        locationField = new JTextField(15);
        styleField(locationField);
        gbc.gridx = 1;
        add(locationField, gbc);

        JButton registerButton = new JButton("Register");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        registerButton.setBackground(PRIMARY_GREEN);
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 8, 8, 8);
        add(registerButton, gbc);

        messageLabel = new JLabel(" ");
        messageLabel.setForeground(new Color(200, 50, 50));
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 7;
        gbc.insets = new Insets(8, 8, 8, 8);
        add(messageLabel, gbc);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegister();
            }
        });

        getRootPane().setDefaultButton(registerButton);

        setVisible(true);
    }

    private JLabel styledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        return label;
    }

    private void styleField(JTextField field) {
        field.setFont(FIELD_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
    }

    private void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String contact = contactField.getText().trim();
        String location = locationField.getText().trim();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            messageLabel.setForeground(new Color(200, 50, 50));
            messageLabel.setText("Full name, username, and password are required.");
            return;
        }

        Farmer newFarmer = new Farmer();
        newFarmer.setFullName(fullName);
        newFarmer.setUsername(username);
        newFarmer.setPassword(password);
        newFarmer.setContactNumber(contact);
        newFarmer.setFarmLocation(location);

        FarmerDAO farmerDAO = new FarmerDAO();
        boolean success = farmerDAO.registerFarmer(newFarmer);

        if (success) {
            messageLabel.setForeground(PRIMARY_GREEN_DARK);
            messageLabel.setText("Registration successful! You can now log in.");
        } else {
            messageLabel.setForeground(new Color(200, 50, 50));
            messageLabel.setText("Registration failed. Username may already exist.");
        }
    }
}