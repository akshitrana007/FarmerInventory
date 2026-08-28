package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomerDashboardFrame extends JFrame {

    private final Customer currentCustomer;
    private DefaultTableModel tableModel;
    private JTable productTable;
    private List<Product> currentProducts;

    private final ProductDAO productDAO = new ProductDAO();
    private final FarmerDAO farmerDAO = new FarmerDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    public CustomerDashboardFrame(Customer customer) {
        this.currentCustomer = customer;

        setTitle("Customer Dashboard - Welcome " + customer.getCustomerName());
        setSize(1100, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 250, 240));

        // ---- Top panel: welcome + search boxes ----
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(new Color(240, 250, 240));

        JLabel welcomeLabel = new JLabel("Welcome, " + customer.getCustomerName() + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        welcomeLabel.setForeground(new Color(56, 110, 54));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(new Color(240, 250, 240));

        JTextField productField = new JTextField(12);
        JTextField areaField = new JTextField(12);
        JTextField farmerNameField = new JTextField(12);

        JButton searchButton = new JButton("Search");
        searchButton.setBackground(new Color(76, 140, 74));
        searchButton.setForeground(Color.WHITE);

        JButton showAllButton = new JButton("Show All");

        searchPanel.add(new JLabel("Product:"));
        searchPanel.add(productField);
        searchPanel.add(new JLabel("Area:"));
        searchPanel.add(areaField);
        searchPanel.add(new JLabel("Farmer Name:"));
        searchPanel.add(farmerNameField);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);

        topPanel.add(welcomeLabel);
        topPanel.add(searchPanel);
        add(topPanel, BorderLayout.NORTH);

        // ---- Center: product table ----
        String[] columns = {"Product", "Farmer", "Farmer Contact", "Area", "Price/Unit", "Stock Available", "Unit"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(tableModel);
        productTable.setRowHeight(24);
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);

        // ---- Bottom panel: order button ----
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setBackground(new Color(240, 250, 240));

        JButton orderButton = new JButton("Send Order Request");
        orderButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        orderButton.setBackground(new Color(76, 140, 74));
        orderButton.setForeground(Color.WHITE);

        JButton logoutButton = new JButton("Logout");

        bottomPanel.add(orderButton);
        bottomPanel.add(logoutButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // ---- Load all products initially ----
        loadAllProducts();

        // ---- Button actions ----
        showAllButton.addActionListener(e -> {
            productField.setText("");
            areaField.setText("");
            farmerNameField.setText("");
            loadAllProducts();
        });

        searchButton.addActionListener(e -> {
            String productKeyword = productField.getText().trim();
            String areaKeyword = areaField.getText().trim();
            String farmerNameKeyword = farmerNameField.getText().trim();
            runCombinedSearch(productKeyword, areaKeyword, farmerNameKeyword);
        });

        orderButton.addActionListener(e -> sendOrderRequest());

        logoutButton.addActionListener(e -> {
            dispose();
            new RoleSelectionFrame();
        });

        setVisible(true);
    }

    private void loadAllProducts() {
        runCombinedSearch("", "", "");
    }

    // Combines product name, area, and farmer name filters together.
    // Any box left empty is simply ignored (not used as a filter).
    private void runCombinedSearch(String productKeyword, String areaKeyword, String farmerNameKeyword) {
        List<Farmer> allFarmers = farmerDAO.getAllFarmers();

        // Step 1: figure out which farmers match the area/name filters
        Set<Integer> matchingFarmerIds = new HashSet<>();

        boolean areaFilterUsed = !areaKeyword.isEmpty();
        boolean nameFilterUsed = !farmerNameKeyword.isEmpty();

        if (!areaFilterUsed && !nameFilterUsed) {
            for (Farmer f : allFarmers) {
                matchingFarmerIds.add(f.getFarmerId());
            }
        } else {
            for (Farmer f : allFarmers) {
                boolean areaMatches = !areaFilterUsed ||
                        (f.getFarmLocation() != null && f.getFarmLocation().toLowerCase().contains(areaKeyword.toLowerCase()));
                boolean nameMatches = !nameFilterUsed ||
                        (f.getFullName() != null && f.getFullName().toLowerCase().contains(farmerNameKeyword.toLowerCase()));

                if (areaMatches && nameMatches) {
                    matchingFarmerIds.add(f.getFarmerId());
                }
            }
        }

        // Step 2: collect products from matching farmers, then apply product name filter
        List<Product> results = new ArrayList<>();
        boolean productFilterUsed = !productKeyword.isEmpty();

        for (Integer farmerId : matchingFarmerIds) {
            List<Product> farmerProducts = productDAO.getProductsByFarmer(farmerId);
            for (Product p : farmerProducts) {
                boolean productMatches = !productFilterUsed ||
                        (p.getProductName() != null && p.getProductName().toLowerCase().contains(productKeyword.toLowerCase()));
                if (productMatches) {
                    results.add(p);
                }
            }
        }

        displayProducts(results);
    }

    private void displayProducts(List<Product> products) {
        this.currentProducts = products;
        tableModel.setRowCount(0);

        List<Farmer> allFarmers = farmerDAO.getAllFarmers();

        for (Product p : products) {
            String farmerName = "Unknown";
            String farmerArea = "Unknown";
            String farmerContact = "N/A";

            for (Farmer f : allFarmers) {
                if (f.getFarmerId() == p.getFarmerId()) {
                    farmerName = f.getFullName();
                    farmerArea = f.getFarmLocation();
                    farmerContact = f.getContactNumber();
                    break;
                }
            }

            tableModel.addRow(new Object[]{
                    p.getProductName(),
                    farmerName,
                    farmerContact,
                    farmerArea,
                    p.getPricePerUnit(),
                    p.getStockQuantity(),
                    p.getUnit()
            });
        }

        if (products.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No matching products found.");
        }
    }

    private void sendOrderRequest() {
        int selectedRow = productTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product first.");
            return;
        }

        Product selectedProduct = currentProducts.get(selectedRow);

        String quantityStr = JOptionPane.showInputDialog(this,
                "Enter quantity to order (available: " + selectedProduct.getStockQuantity() + " " + selectedProduct.getUnit() + "):");

        if (quantityStr == null || quantityStr.trim().isEmpty()) {
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.");
            return;
        }

        if (quantity <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.");
            return;
        }

        if (quantity > selectedProduct.getStockQuantity()) {
            JOptionPane.showMessageDialog(this, "Not enough stock available.");
            return;
        }

        boolean success = orderDAO.createOrder(
                currentCustomer.getCustomerId(),
                selectedProduct.getFarmerId(),
                selectedProduct.getProductId(),
                quantity
        );

        if (success) {
            JOptionPane.showMessageDialog(this, "Order request sent! The farmer will review it soon.");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to send order request. Please try again.");
        }
    }
}