package org.example;

import javax.swing.*;
import java.awt.*;

public class CustomerLoginFrame extends JFrame {

    public CustomerLoginFrame() {
        setTitle(LanguageManager.get("custlogin.window.title"));
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

        JLabel titleLabel = new JLabel(LanguageManager.get("custlogin.heading"), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Nirmala UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(56, 110, 54));
        formPanel.add(titleLabel, gbc);

        JPanel fieldsPanel = new JPanel(new GridLayout(2, 2, 10, 15));
        fieldsPanel.setBackground(new Color(240, 250, 240));
        fieldsPanel.setPreferredSize(new Dimension(360, 90));

        JLabel userLabel = new JLabel(LanguageManager.get("custlogin.label.username"));
        userLabel.setFont(new Font("Nirmala UI", Font.PLAIN, 15));
        JTextField usernameField = new JTextField();
        usernameField.setFont(new Font("Nirmala UI", Font.PLAIN, 15));

        JLabel passLabel = new JLabel(LanguageManager.get("custlogin.label.password"));
        passLabel.setFont(new Font("Nirmala UI", Font.PLAIN, 15));
        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(new Font("Nirmala UI", Font.PLAIN, 15));

        fieldsPanel.add(userLabel);
        fieldsPanel.add(usernameField);
        fieldsPanel.add(passLabel);
        fieldsPanel.add(passwordField);

        gbc.gridwidth = 1;
        GridBagConstraints centerGbc = new GridBagConstraints();
        centerGbc.gridwidth = GridBagConstraints.REMAINDER;
        centerGbc.insets = new Insets(10, 10, 10, 10);
        formPanel.add(fieldsPanel, centerGbc);

        JButton loginButton = new JButton(LanguageManager.get("custlogin.button.login"));
        loginButton.setFont(new Font("Nirmala UI", Font.BOLD, 16));
        loginButton.setBackground(new Color(76, 140, 74));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new Dimension(360, 45));
        formPanel.add(loginButton, centerGbc);

        JButton registerButton = new JButton(LanguageManager.get("custlogin.link.register"));
        registerButton.setFont(new Font("Nirmala UI", Font.PLAIN, 13));
        registerButton.setBorderPainted(false);
        registerButton.setContentAreaFilled(false);
        registerButton.setForeground(new Color(56, 110, 54));
        formPanel.add(registerButton, centerGbc);

        JButton backButton = new JButton(LanguageManager.get("custlogin.link.back"));
        backButton.setFont(new Font("Nirmala UI", Font.PLAIN, 13));
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
                JOptionPane.showMessageDialog(this, LanguageManager.get("custlogin.msg.welcome") + customer.getCustomerName() + "!");
                dispose();
                new CustomerDashboardFrame(customer);
            } else {
                JOptionPane.showMessageDialog(this, LanguageManager.get("custlogin.msg.invalid"));
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