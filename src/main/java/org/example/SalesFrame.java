package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class SalesFrame extends JFrame {

    private Farmer currentFarmer;

    private JComboBox<String> productDropdown;
    private JComboBox<String> customerDropdown;
    private JTextField quantityField;
    private JLabel totalLabel;
    private JTable salesTable;
    private DefaultTableModel tableModel;

    private ProductDAO productDAO = new ProductDAO();
    private CustomerDAO customerDAO = new CustomerDAO();
    private SalesDAO salesDAO = new SalesDAO();

    private List<Product> productList;
    private List<Customer> customerList;

    private static final Color PRIMARY_GREEN = new Color(76, 145, 65);
    private static final Color BACKGROUND = new Color(245, 250, 240);
    private static final Color CARD_BG = Color.WHITE;

    public SalesFrame(Farmer farmer) {
        this.currentFarmer = farmer;

        setTitle("Sales Management");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout(10, 10));

        // ---- Top banner ----
        JPanel topBanner = new JPanel(new BorderLayout());
        topBanner.setBackground(PRIMARY_GREEN);
        topBanner.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel titleLabel = new JLabel("Sales Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topBanner.add(titleLabel, BorderLayout.WEST);
        add(topBanner, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBackground(BACKGROUND);
        mainContent.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        add(mainContent, BorderLayout.CENTER);

        // ---- New sale form ----
        JPanel formPanel = new JPanel(new GridLayout(2, 4, 10, 8));
        formPanel.setBackground(CARD_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(230, 230, 230)), "Record New Sale",
                        0, 0, new Font("Segoe UI", Font.BOLD, 13), new Color(80, 80, 80)),
                BorderFactory.createEmptyBorder(10, 12, 12, 12)));

        productDropdown = new JComboBox<>();
        customerDropdown = new JComboBox<>();
        quantityField = new JTextField();
        styleField(quantityField);
        styleCombo(productDropdown);
        styleCombo(customerDropdown);

        totalLabel = new JLabel("Total: Rs. 0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        totalLabel.setForeground(new Color(60, 120, 50));

        formPanel.add(formLabel("Product:"));
        formPanel.add(formLabel("Customer:"));
        formPanel.add(formLabel("Quantity:"));
        formPanel.add(new JLabel(""));

        formPanel.add(productDropdown);
        formPanel.add(customerDropdown);
        formPanel.add(quantityField);
        formPanel.add(totalLabel);

        mainContent.add(formPanel, BorderLayout.NORTH);

        // ---- Sales history table ----
        String[] columns = {"Sale ID", "Product ID", "Customer ID", "Qty", "Total Amount", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        salesTable = new JTable(tableModel);
        salesTable.setRowHeight(28);
        salesTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        salesTable.setSelectionBackground(new Color(210, 235, 200));
        salesTable.setGridColor(new Color(235, 235, 235));

        JTableHeader header = salesTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(PRIMARY_GREEN);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 32));

        JScrollPane scrollPane = new JScrollPane(salesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(BACKGROUND);
        tableContainer.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        mainContent.add(tableContainer, BorderLayout.CENTER);

        // ---- Bottom buttons ----
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        buttonPanel.setBackground(BACKGROUND);
        JButton recordSaleBtn = createActionButton("Record Sale", PRIMARY_GREEN);
        JButton addCustomerBtn = createActionButton("Add New Customer", new Color(70, 130, 180));
        JButton refreshBtn = createActionButton("Refresh History", new Color(140, 140, 140));
        buttonPanel.add(recordSaleBtn);
        buttonPanel.add(addCustomerBtn);
        buttonPanel.add(refreshBtn);
        mainContent.add(buttonPanel, BorderLayout.SOUTH);

        // ---- Event handling ----
        recordSaleBtn.addActionListener(e -> handleRecordSale());
        addCustomerBtn.addActionListener(e -> handleAddCustomer());
        refreshBtn.addActionListener(e -> loadSalesHistory());

        loadProducts();
        loadCustomers();
        loadSalesHistory();

        setVisible(true);
    }

    private JLabel formLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(90, 90, 90));
        return label;
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadProducts() {
        productDropdown.removeAllItems();
        productList = productDAO.getProductsByFarmer(currentFarmer.getFarmerId());
        for (Product p : productList) {
            productDropdown.addItem(p.getProductId() + " - " + p.getProductName() + " (Rs." + p.getPricePerUnit() + "/" + p.getUnit() + ")");
        }
    }

    private void loadCustomers() {
        customerDropdown.removeAllItems();
        customerList = customerDAO.getAllCustomers();
        for (Customer c : customerList) {
            customerDropdown.addItem(c.getCustomerId() + " - " + c.getCustomerName());
        }
    }

    private void loadSalesHistory() {
        tableModel.setRowCount(0);
        List<Sale> sales = salesDAO.getAllSales(currentFarmer.getFarmerId());
        for (Sale s : sales) {
            tableModel.addRow(new Object[]{
                    s.getSaleId(), s.getProductId(), s.getCustomerId(),
                    s.getQuantitySold(), s.getTotalAmount(), s.getSaleDate()
            });
        }
    }

    private void handleRecordSale() {
        if (productList.isEmpty() || customerList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You need at least one product and one customer first.");
            return;
        }

        int productIndex = productDropdown.getSelectedIndex();
        int customerIndex = customerDropdown.getSelectedIndex();

        if (productIndex < 0 || customerIndex < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product and a customer.");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.");
                return;
            }

            Product selectedProduct = productList.get(productIndex);
            Customer selectedCustomer = customerList.get(customerIndex);

            double total = quantity * selectedProduct.getPricePerUnit();

            Sale sale = new Sale();
            sale.setProductId(selectedProduct.getProductId());
            sale.setCustomerId(selectedCustomer.getCustomerId());
            sale.setFarmerId(currentFarmer.getFarmerId());
            sale.setQuantitySold(quantity);
            sale.setTotalAmount(total);

            boolean success = salesDAO.recordSale(sale);

            if (success) {
                JOptionPane.showMessageDialog(this, "Sale recorded successfully!\nTotal: Rs. " + total);
                quantityField.setText("");
                totalLabel.setText("Total: Rs. 0.00");
                loadProducts();
                loadSalesHistory();
            } else {
                JOptionPane.showMessageDialog(this, "Sale failed. Not enough stock available.");
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity must be a valid number.");
        }
    }

    private void handleAddCustomer() {
        JTextField nameField = new JTextField();
        JTextField contactField = new JTextField();
        JTextField addressField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Contact:"));
        panel.add(contactField);
        panel.add(new JLabel("Address:"));
        panel.add(addressField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Customer", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Customer name is required.");
                return;
            }

            Customer customer = new Customer();
            customer.setCustomerName(name);
            customer.setContactNumber(contactField.getText().trim());
            customer.setAddress(addressField.getText().trim());

            boolean success = customerDAO.addCustomer(customer);
            if (success) {
                JOptionPane.showMessageDialog(this, "Customer added!");
                loadCustomers();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add customer.");
            }
        }
    }
}