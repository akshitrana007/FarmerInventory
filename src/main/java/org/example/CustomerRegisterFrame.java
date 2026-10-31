package org.example;

import javax.swing.*;
import java.awt.*;

public class CustomerRegisterFrame extends JFrame {

    public CustomerRegisterFrame() {
        setTitle(LanguageManager.get("custregister.window.title"));
        setSize(420, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(240, 250, 240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel(LanguageManager.get("custregister.heading"), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Nirmala UI", Font.BOLD, 18));
        add(titleLabel, gbc);

        JTextField nameField = new JTextField();
        JTextField contactField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        add(new JLabel(LanguageManager.get("custregister.label.fullname")), gbc);
        add(nameField, gbc);

        add(new JLabel(LanguageManager.get("custregister.label.contact")), gbc);
        add(contactField, gbc);

        add(new JLabel(LanguageManager.get("custregister.label.address")), gbc);
        add(addressField, gbc);

        add(new JLabel(LanguageManager.get("custregister.label.username")), gbc);
        add(usernameField, gbc);

        add(new JLabel(LanguageManager.get("custregister.label.password")), gbc);
        add(passwordField, gbc);

        JButton registerButton = new JButton(LanguageManager.get("custregister.button.register"));
        registerButton.setBackground(new Color(76, 140, 74));
        registerButton.setForeground(Color.WHITE);
        add(registerButton, gbc);

        JButton backButton = new JButton(LanguageManager.get("custregister.link.back"));
        add(backButton, gbc);

        registerButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String contact = contactField.getText().trim();
            String address = addressField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, LanguageManager.get("custregister.msg.required"));
                return;
            }

            Customer customer = new Customer(0, name, contact, address, username, password);
            CustomerDAO dao = new CustomerDAO();
            boolean success = dao.registerCustomer(customer);

            if (success) {
                JOptionPane.showMessageDialog(this, LanguageManager.get("custregister.msg.success"));
                dispose();
                new CustomerLoginFrame();
            } else {
                JOptionPane.showMessageDialog(this, LanguageManager.get("custregister.msg.fail"));
            }
        });

        backButton.addActionListener(e -> {
            dispose();
            new RoleSelectionFrame();
        });

        setVisible(true);
    }
}