package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BrowseCustomersFrame extends JFrame {

    private final Farmer currentFarmer;
    private final CustomerDAO customerDAO = new CustomerDAO();

    private DefaultTableModel tableModel;
    private JTable customerTable;
    private List<Customer> currentCustomers;

    public BrowseCustomersFrame(Farmer farmer) {
        this.currentFarmer = farmer;

        setTitle(LanguageManager.get("browsecust.title.prefix") + farmer.getFullName());
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 250, 240));

        JLabel titleLabel = new JLabel(LanguageManager.get("browsecust.heading"));
        titleLabel.setFont(new Font("Nirmala UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(56, 110, 54));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {
                LanguageManager.get("browsecust.col.name"),
                LanguageManager.get("browsecust.col.contact"),
                LanguageManager.get("browsecust.col.address")
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        customerTable = new JTable(tableModel);
        customerTable.setRowHeight(24);
        customerTable.setFont(new Font("Nirmala UI", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(customerTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setBackground(new Color(240, 250, 240));

        JButton chatButton = new JButton(LanguageManager.get("browsecust.button.chat"));
        chatButton.setFont(new Font("Nirmala UI", Font.BOLD, 14));
        chatButton.setBackground(new Color(76, 140, 74));
        chatButton.setForeground(Color.WHITE);

        JButton closeButton = new JButton(LanguageManager.get("browsecust.button.close"));

        bottomPanel.add(chatButton);
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);

        chatButton.addActionListener(e -> openChat());
        closeButton.addActionListener(e -> dispose());

        loadAllCustomers();

        setVisible(true);
    }

    private void loadAllCustomers() {
        currentCustomers = customerDAO.getAllCustomers();
        tableModel.setRowCount(0);

        for (Customer c : currentCustomers) {
            tableModel.addRow(new Object[]{
                    c.getCustomerName(),
                    c.getContactNumber(),
                    c.getAddress()
            });
        }
    }

    private void openChat() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("browsecust.msg.selectfirst"));
            return;
        }

        Customer selectedCustomer = currentCustomers.get(selectedRow);

        new ChatFrame(
                currentFarmer.getFarmerId(), "FARMER", currentFarmer.getFullName(),
                selectedCustomer.getCustomerId(), "CUSTOMER", selectedCustomer.getCustomerName()
        );
    }
}