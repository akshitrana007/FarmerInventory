package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class InventoryFrame extends JFrame {

    private Farmer currentFarmer;
    private JTable productTable;
    private DefaultTableModel tableModel;

    private JTextField nameField, categoryField, stockField, unitField, priceField, thresholdField, searchField;

    private ProductDAO productDAO = new ProductDAO();

    private static final Color PRIMARY_GREEN = new Color(76, 145, 65);
    private static final Color PRIMARY_GREEN_DARK = new Color(60, 120, 50);
    private static final Color BACKGROUND = new Color(245, 250, 240);
    private static final Color CARD_BG = Color.WHITE;

    public InventoryFrame(Farmer farmer) {
        this.currentFarmer = farmer;

        setTitle("Inventory Management");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout(10, 10));

        // ---- Top banner ----
        JPanel topBanner = new JPanel(new BorderLayout());
        topBanner.setBackground(PRIMARY_GREEN);
        topBanner.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel titleLabel = new JLabel("Inventory Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topBanner.add(titleLabel, BorderLayout.WEST);
        add(topBanner, BorderLayout.NORTH);

        // ---- Main content container ----
        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBackground(BACKGROUND);
        mainContent.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        add(mainContent, BorderLayout.CENTER);

        // ---- Search bar ----
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        searchPanel.setBackground(CARD_BG);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JLabel searchLbl = new JLabel("Search Product:");
        searchLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField = new JTextField(18);
        styleField(searchField);

        JButton searchBtn = createSmallButton("Search");
        JButton showAllBtn = createSmallButton("Show All");

        searchPanel.add(searchLbl);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(showAllBtn);
        mainContent.add(searchPanel, BorderLayout.NORTH);

        // ---- Table ----
        String[] columns = {"ID", "Name", "Category", "Stock", "Unit", "Price/Unit", "Low Threshold"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(tableModel);
        productTable.setRowHeight(28);
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        productTable.setSelectionBackground(new Color(210, 235, 200));
        productTable.setSelectionForeground(Color.BLACK);
        productTable.setGridColor(new Color(235, 235, 235));

        JTableHeader header = productTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(PRIMARY_GREEN);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 32));

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        mainContent.add(scrollPane, BorderLayout.CENTER);

        // ---- Form + buttons ----
        JPanel southContainer = new JPanel(new BorderLayout(0, 10));
        southContainer.setBackground(BACKGROUND);
        southContainer.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel formPanel = new JPanel(new GridLayout(2, 7, 8, 8));
        formPanel.setBackground(CARD_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        nameField = new JTextField();
        categoryField = new JTextField();
        stockField = new JTextField();
        unitField = new JTextField();
        priceField = new JTextField();
        thresholdField = new JTextField();

        styleField(nameField);
        styleField(categoryField);
        styleField(stockField);
        styleField(unitField);
        styleField(priceField);
        styleField(thresholdField);

        formPanel.add(formLabel("Name:"));
        formPanel.add(formLabel("Category:"));
        formPanel.add(formLabel("Stock:"));
        formPanel.add(formLabel("Unit:"));
        formPanel.add(formLabel("Price/Unit:"));
        formPanel.add(formLabel("Low Threshold:"));
        formPanel.add(new JLabel(""));

        formPanel.add(nameField);
        formPanel.add(categoryField);
        formPanel.add(stockField);
        formPanel.add(unitField);
        formPanel.add(priceField);
        formPanel.add(thresholdField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        buttonPanel.setBackground(BACKGROUND);
        JButton addBtn = createActionButton("Add Product", PRIMARY_GREEN);
        JButton updateStockBtn = createActionButton("Update Stock", new Color(70, 130, 180));
        JButton deleteBtn = createActionButton("Delete", new Color(190, 70, 70));
        JButton clearBtn = createActionButton("Clear", new Color(140, 140, 140));
        buttonPanel.add(addBtn);
        buttonPanel.add(updateStockBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(clearBtn);

        southContainer.add(formPanel, BorderLayout.CENTER);
        southContainer.add(buttonPanel, BorderLayout.SOUTH);
        mainContent.add(southContainer, BorderLayout.SOUTH);

        // ---- Event handling ----
        addBtn.addActionListener(e -> handleAddProduct());
        updateStockBtn.addActionListener(e -> handleUpdateStock());
        deleteBtn.addActionListener(e -> handleDeleteProduct());
        clearBtn.addActionListener(e -> clearForm());
        searchBtn.addActionListener(e -> handleSearch());
        showAllBtn.addActionListener(e -> loadAllProducts());

        productTable.getSelectionModel().addListSelectionListener(e -> {
            int row = productTable.getSelectedRow();
            if (row >= 0) {
                nameField.setText(tableModel.getValueAt(row, 1).toString());
                categoryField.setText(tableModel.getValueAt(row, 2).toString());
                stockField.setText(tableModel.getValueAt(row, 3).toString());
                unitField.setText(tableModel.getValueAt(row, 4).toString());
                priceField.setText(tableModel.getValueAt(row, 5).toString());
                thresholdField.setText(tableModel.getValueAt(row, 6).toString());
            }
        });

        loadAllProducts();
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

    private JButton createSmallButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(PRIMARY_GREEN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
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

    private void loadAllProducts() {
        tableModel.setRowCount(0);
        List<Product> products = productDAO.getProductsByFarmer(currentFarmer.getFarmerId());
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                    p.getProductId(), p.getProductName(), p.getCategory(),
                    p.getStockQuantity(), p.getUnit(), p.getPricePerUnit(), p.getLowStockThreshold()
            });
        }
    }

    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadAllProducts();
            return;
        }
        tableModel.setRowCount(0);
        List<Product> results = productDAO.searchProduct(keyword, currentFarmer.getFarmerId());
        for (Product p : results) {
            tableModel.addRow(new Object[]{
                    p.getProductId(), p.getProductName(), p.getCategory(),
                    p.getStockQuantity(), p.getUnit(), p.getPricePerUnit(), p.getLowStockThreshold()
            });
        }
    }

    private void handleAddProduct() {
        try {
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            int stock = Integer.parseInt(stockField.getText().trim());
            String unit = unitField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());
            int threshold = Integer.parseInt(thresholdField.getText().trim());

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Product name is required.");
                return;
            }

            Product product = new Product();
            product.setFarmerId(currentFarmer.getFarmerId());
            product.setProductName(name);
            product.setCategory(category);
            product.setStockQuantity(stock);
            product.setUnit(unit.isEmpty() ? "kg" : unit);
            product.setPricePerUnit(price);
            product.setLowStockThreshold(threshold);

            boolean success = productDAO.addProduct(product);
            if (success) {
                JOptionPane.showMessageDialog(this, "Product added successfully!");
                clearForm();
                loadAllProducts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add product.");
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Stock, Price, and Threshold must be valid numbers.");
        }
    }

    private void handleUpdateStock() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product from the table first.");
            return;
        }

        try {
            int productId = (int) tableModel.getValueAt(row, 0);
            int newStock = Integer.parseInt(stockField.getText().trim());

            boolean success = productDAO.updateStock(productId, newStock);
            if (success) {
                JOptionPane.showMessageDialog(this, "Stock updated successfully!");
                clearForm();
                loadAllProducts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update stock.");
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Stock must be a valid number.");
        }
    }

    private void handleDeleteProduct() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product from the table first.");
            return;
        }

        int productId = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this product?", "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = productDAO.deleteProduct(productId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Product deleted.");
                clearForm();
                loadAllProducts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete product.");
            }
        }
    }

    private void clearForm() {
        nameField.setText("");
        categoryField.setText("");
        stockField.setText("");
        unitField.setText("");
        priceField.setText("");
        thresholdField.setText("");
        productTable.clearSelection();
    }
}