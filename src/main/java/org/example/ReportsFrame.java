package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ReportsFrame extends JFrame {

    private Farmer currentFarmer;
    private JTabbedPane tabbedPane;

    private DefaultTableModel inventoryModel;
    private DefaultTableModel salesModel;

    private ProductDAO productDAO = new ProductDAO();
    private SalesDAO salesDAO = new SalesDAO();

    private static final Color PRIMARY_GREEN = new Color(76, 145, 65);
    private static final Color BACKGROUND = new Color(245, 250, 240);
    private static final Color CARD_BG = Color.WHITE;

    public ReportsFrame(Farmer farmer) {
        this.currentFarmer = farmer;

        setTitle("Reports");
        setSize(900, 620);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout(10, 10));

        // ---- Top banner ----
        JPanel topBanner = new JPanel(new BorderLayout());
        topBanner.setBackground(PRIMARY_GREEN);
        topBanner.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel titleLabel = new JLabel("Reports");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topBanner.add(titleLabel, BorderLayout.WEST);

        JLabel summaryLabel = new JLabel();
        summaryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        summaryLabel.setForeground(Color.WHITE);
        topBanner.add(summaryLabel, BorderLayout.EAST);

        add(topBanner, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBackground(BACKGROUND);
        mainContent.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        add(mainContent, BorderLayout.CENTER);

        // ---- Tabs ----
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(CARD_BG);

        String[] invColumns = {"ID", "Name", "Category", "Stock", "Unit", "Price/Unit", "Stock Value (Rs.)"};
        inventoryModel = new DefaultTableModel(invColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable inventoryTable = new JTable(inventoryModel);
        styleTable(inventoryTable);
        JScrollPane invScroll = new JScrollPane(inventoryTable);
        invScroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        tabbedPane.addTab("Inventory Report", invScroll);

        String[] salesColumns = {"Sale ID", "Product ID", "Customer ID", "Qty", "Total Amount", "Date"};
        salesModel = new DefaultTableModel(salesColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable salesTable = new JTable(salesModel);
        styleTable(salesTable);
        JScrollPane salesScroll = new JScrollPane(salesTable);
        salesScroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        tabbedPane.addTab("Sales Report", salesScroll);

        mainContent.add(tabbedPane, BorderLayout.CENTER);

        // ---- Bottom buttons ----
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(BACKGROUND);
        JButton refreshBtn = createActionButton("Refresh", new Color(140, 140, 140));
        JButton exportBtn = createActionButton("Export Current Tab to Text File", PRIMARY_GREEN);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(exportBtn);
        mainContent.add(buttonPanel, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> {
            loadInventoryReport();
            loadSalesReport(summaryLabel);
        });

        exportBtn.addActionListener(e -> handleExport());

        loadInventoryReport();
        loadSalesReport(summaryLabel);

        setVisible(true);
    }

    private void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(210, 235, 200));
        table.setGridColor(new Color(235, 235, 235));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(PRIMARY_GREEN);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 32));
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

    private void loadInventoryReport() {
        inventoryModel.setRowCount(0);
        List<Product> products = productDAO.getProductsByFarmer(currentFarmer.getFarmerId());

        for (Product p : products) {
            double stockValue = p.getStockQuantity() * p.getPricePerUnit();
            inventoryModel.addRow(new Object[]{
                    p.getProductId(), p.getProductName(), p.getCategory(),
                    p.getStockQuantity(), p.getUnit(), p.getPricePerUnit(),
                    String.format("%.2f", stockValue)
            });
        }
    }

    private void loadSalesReport(JLabel summaryLabel) {
        salesModel.setRowCount(0);
        List<Sale> sales = salesDAO.getAllSales(currentFarmer.getFarmerId());
        for (Sale s : sales) {
            salesModel.addRow(new Object[]{
                    s.getSaleId(), s.getProductId(), s.getCustomerId(),
                    s.getQuantitySold(), s.getTotalAmount(), s.getSaleDate()
            });
        }

        int totalSales = salesDAO.getTotalSalesCount(currentFarmer.getFarmerId());
        double totalRevenue = salesDAO.getTotalRevenue(currentFarmer.getFarmerId());
        summaryLabel.setText("Transactions: " + totalSales + "   |   Revenue: Rs. " + String.format("%.2f", totalRevenue));
    }

    private void handleExport() {
        int selectedTab = tabbedPane.getSelectedIndex();
        DefaultTableModel modelToExport = (selectedTab == 0) ? inventoryModel : salesModel;
        String reportName = (selectedTab == 0) ? "Inventory_Report" : "Sales_Report";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File(reportName + ".txt"));
        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();

            try (FileWriter writer = new FileWriter(file)) {
                for (int col = 0; col < modelToExport.getColumnCount(); col++) {
                    writer.write(modelToExport.getColumnName(col) + "\t");
                }
                writer.write("\n");

                for (int row = 0; row < modelToExport.getRowCount(); row++) {
                    for (int col = 0; col < modelToExport.getColumnCount(); col++) {
                        writer.write(String.valueOf(modelToExport.getValueAt(row, col)) + "\t");
                    }
                    writer.write("\n");
                }

                JOptionPane.showMessageDialog(this, "Report exported successfully!");

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error exporting report: " + ex.getMessage());
            }
        }
    }
}