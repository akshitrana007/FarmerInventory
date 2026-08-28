package org.example;

import javax.swing.*;
import java.awt.*;

public class CustomerRegisterFrame extends JFrame {

    public CustomerRegisterFrame() {
        setTitle("Customer Registration");
        setSize(420, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(240, 250, 240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Create Customer Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(titleLabel, gbc);

        JTextField nameField = new JTextField();
        JTextField contactField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        add(new JLabel("Full Name:"), gbc);
        add(nameField, gbc);

        add(new JLabel("Contact Number:"), gbc);
        add(contactField, gbc);

        add(new JLabel("Address:"), gbc);
        add(addressField, gbc);

        add(new JLabel("Choose Username:"), gbc);
        add(usernameField, gbc);

        add(new JLabel("Choose Password:"), gbc);
        add(passwordField, gbc);

        JButton registerButton = new JButton("Register");
        registerButton.setBackground(new Color(76, 140, 74));
        registerButton.setForeground(Color.WHITE);
        add(registerButton, gbc);

        JButton backButton = new JButton("Back to Role Selection");
        add(backButton, gbc);

        registerButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String contact = contactField.getText().trim();
            String address = addressField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name, username and password are required.");
                return;
            }

            Customer customer = new Customer(0, name, contact, address, username, password);
            CustomerDAO dao = new CustomerDAO();
            boolean success = dao.registerCustomer(customer);

            if (success) {
                JOptionPane.showMessageDialog(this, "Registration successful! Please log in.");
                dispose();
                new CustomerLoginFrame();
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed. Username may already be taken.");
            }
        });

        backButton.addActionListener(e -> {
            dispose();
            new RoleSelectionFrame();
        });

        setVisible(true);
    }
}