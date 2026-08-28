package org.example;

import javax.swing.*;
import java.awt.*;

public class CustomerLoginFrame extends JFrame {

    public CustomerLoginFrame() {
        setTitle("Farmer Inventory System - Customer Login");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(240, 250, 240));

        // Top illustration (reusing your existing handshake artwork)
        HandshakePanel handshakePanel = new HandshakePanel();
        handshakePanel.setPreferredSize(new Dimension(1000, 220));
        add(handshakePanel, BorderLayout.NORTH);

        // Center form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(240, 250, 240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Customer Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(56, 110, 54));
        formPanel.add(titleLabel, gbc);

        JPanel fieldsPanel = new JPanel(new GridLayout(2, 2, 10, 15));
        fieldsPanel.setBackground(new Color(240, 250, 240));
        fieldsPanel.setPreferredSize(new Dimension(360, 90));

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        JTextField usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        fieldsPanel.add(userLabel);
        fieldsPanel.add(usernameField);
        fieldsPanel.add(passLabel);
        fieldsPanel.add(passwordField);

        gbc.gridwidth = 1;
        GridBagConstraints centerGbc = new GridBagConstraints();
        centerGbc.gridwidth = GridBagConstraints.REMAINDER;
        centerGbc.insets = new Insets(10, 10, 10, 10);
        formPanel.add(fieldsPanel, centerGbc);

        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginButton.setBackground(new Color(76, 140, 74));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new Dimension(360, 45));
        formPanel.add(loginButton, centerGbc);

        JButton registerButton = new JButton("New here? Register");
        registerButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        registerButton.setBorderPainted(false);
        registerButton.setContentAreaFilled(false);
        registerButton.setForeground(new Color(56, 110, 54));
        formPanel.add(registerButton, centerGbc);

        JButton backButton = new JButton("Back to Role Selection");
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setForeground(Color.GRAY);
        formPanel.add(backButton, centerGbc);

        add(formPanel, BorderLayout.CENTER);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            CustomerDAO dao = new CustomerDAO();
            Customer customer = dao.login(username, password);

            if (customer != null) {
                JOptionPane.showMessageDialog(this, "Welcome, " + customer.getCustomerName() + "!");
                dispose();
                new CustomerDashboardFrame(customer);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.");
            }
        });

        registerButton.addActionListener(e -> {
            dispose();
            new CustomerRegisterFrame();
        });

        backButton.addActionListener(e -> {
            dispose();
            new RoleSelectionFrame();
        });

        setVisible(true);
    }
}