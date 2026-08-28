package org.example;

import javax.swing.*;
import java.awt.*;

public class RoleSelectionFrame extends JFrame {

    public RoleSelectionFrame() {
        setTitle("Welcome - Choose Your Role");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(240, 250, 240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Are you a Customer or a Farmer?", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(titleLabel, gbc);

        JButton farmerButton = new JButton("I am a Farmer");
        farmerButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        farmerButton.setBackground(new Color(76, 140, 74));
        farmerButton.setForeground(Color.WHITE);
        add(farmerButton, gbc);

        JButton customerButton = new JButton("I am a Customer");
        customerButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        customerButton.setBackground(new Color(76, 140, 74));
        customerButton.setForeground(Color.WHITE);
        add(customerButton, gbc);

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