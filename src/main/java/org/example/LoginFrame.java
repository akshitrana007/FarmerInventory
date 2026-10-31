package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel messageLabel;

    private static final Color PRIMARY_GREEN = new Color(76, 145, 65);
    private static final Color PRIMARY_GREEN_DARK = new Color(60, 120, 50);
    private static final Color BACKGROUND = new Color(245, 250, 240);
    private static final Font LABEL_FONT = new Font("Nirmala UI", Font.PLAIN, 14);
    private static final Font FIELD_FONT = new Font("Nirmala UI", Font.PLAIN, 14);

    public LoginFrame() {

        setTitle(LanguageManager.get("login.window.title"));
        setSize(420, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(new HandshakePanel(), BorderLayout.NORTH);

        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(BACKGROUND);
        formContainer.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        add(formContainer, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel(LanguageManager.get("login.heading"));
        titleLabel.setFont(new Font("Nirmala UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_GREEN_DARK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 8, 20, 8);
        formContainer.add(titleLabel, gbc);

        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridwidth = 1;

        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel userLabel = new JLabel(LanguageManager.get("login.label.username"));
        userLabel.setFont(LABEL_FONT);
        formContainer.add(userLabel, gbc);

        usernameField = new JTextField(15);
        usernameField.setFont(FIELD_FONT);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        gbc.gridx = 1;
        formContainer.add(usernameField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel passLabel = new JLabel(LanguageManager.get("login.label.password"));
        passLabel.setFont(LABEL_FONT);
        formContainer.add(passLabel, gbc);

        passwordField = new JPasswordField(15);
        passwordField.setFont(FIELD_FONT);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        gbc.gridx = 1;
        formContainer.add(passwordField, gbc);

        loginButton = new JButton(LanguageManager.get("login.button.login"));
        loginButton.setFont(new Font("Nirmala UI", Font.BOLD, 15));
        loginButton.setBackground(PRIMARY_GREEN);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 8, 8, 8);
        formContainer.add(loginButton, gbc);

        messageLabel = new JLabel(" ");
        messageLabel.setForeground(new Color(200, 50, 50));
        messageLabel.setFont(new Font("Nirmala UI", Font.PLAIN, 13));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 4;
        gbc.insets = new Insets(8, 8, 8, 8);
        formContainer.add(messageLabel, gbc);

        JButton registerButton = new JButton(LanguageManager.get("login.link.register"));
        registerButton.setBorderPainted(false);
        registerButton.setContentAreaFilled(false);
        registerButton.setForeground(PRIMARY_GREEN_DARK);
        registerButton.setFont(new Font("Nirmala UI", Font.BOLD, 13));
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        formContainer.add(registerButton, gbc);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RegisterFrame();
            }
        });

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        getRootPane().setDefaultButton(loginButton); // pressing Enter also triggers login

        setVisible(true);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setForeground(new Color(200, 50, 50));
            messageLabel.setText(LanguageManager.get("login.error.empty"));
            return;
        }

        FarmerDAO farmerDAO = new FarmerDAO();
        Farmer farmer = farmerDAO.login(username, password);

        if (farmer != null) {
            new DashboardFrame(farmer);
            dispose();
        } else {
            messageLabel.setForeground(new Color(200, 50, 50));
            messageLabel.setText(LanguageManager.get("login.error.invalid"));
        }
    }
}