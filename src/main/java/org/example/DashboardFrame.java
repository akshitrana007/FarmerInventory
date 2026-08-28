package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DashboardFrame extends JFrame {

    private Farmer currentFarmer;

    private JLabel totalProductsValue;
    private JLabel totalStockValue;
    private JLabel totalSalesValue;
    private JLabel totalRevenueValue;
    private JPanel alertContentPanel;

    private ProductDAO productDAO = new ProductDAO();
    private SalesDAO salesDAO = new SalesDAO();

    private volatile boolean keepMonitoring = true;

    private static final Color PRIMARY_GREEN = new Color(76, 145, 65);
    private static final Color PRIMARY_GREEN_DARK = new Color(60, 120, 50);
    private static final Color BACKGROUND = new Color(245, 250, 240);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color ALERT_RED = new Color(200, 60, 60);

    public DashboardFrame(Farmer farmer) {
        this.currentFarmer = farmer;

        setTitle("Dashboard - " + farmer.getFullName());
        setSize(650, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout(10, 10));

        // ---- Top banner ----
        JPanel topBanner = new JPanel();
        topBanner.setBackground(PRIMARY_GREEN);
        topBanner.setLayout(new BorderLayout());
        topBanner.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel welcomeLabel = new JLabel("Welcome, " + farmer.getFullName() + "  •  " + farmer.getFarmLocation());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        topBanner.add(welcomeLabel, BorderLayout.WEST);

        add(topBanner, BorderLayout.NORTH);

        // ---- Center: stat cards ----
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        statsPanel.setBackground(BACKGROUND);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        totalProductsValue = new JLabel("0");
        totalStockValue = new JLabel("0 units");
        totalSalesValue = new JLabel("0");
        totalRevenueValue = new JLabel("Rs. 0.00");

        statsPanel.add(createStatCard("Total Products", totalProductsValue, new Color(76, 145, 65)));
        statsPanel.add(createStatCard("Total Stock", totalStockValue, new Color(70, 130, 180)));
        statsPanel.add(createStatCard("Total Sales", totalSalesValue, new Color(200, 150, 60)));
        statsPanel.add(createStatCard("Total Revenue", totalRevenueValue, new Color(150, 90, 160)));

        add(statsPanel, BorderLayout.CENTER);

        // ---- Bottom: alerts + nav buttons ----
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(CARD_BG);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 20, 10, 20),
                BorderFactory.createLineBorder(new Color(230, 230, 230))
        ));

        JLabel alertTitle = new JLabel("Stock Alerts (auto-refreshing)");
        alertTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        alertTitle.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));
        bottomPanel.add(alertTitle, BorderLayout.NORTH);

        alertContentPanel = new JPanel();
        alertContentPanel.setBackground(CARD_BG);
        alertContentPanel.setLayout(new BoxLayout(alertContentPanel, BoxLayout.Y_AXIS));
        alertContentPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 8, 10));
        bottomPanel.add(alertContentPanel, BorderLayout.CENTER);

        JPanel navPanel = new JPanel();
        navPanel.setBackground(BACKGROUND);
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton inventoryBtn = createNavButton("Inventory");
        JButton salesBtn = createNavButton("Sales");
        JButton reportsBtn = createNavButton("Reports");
        navPanel.add(inventoryBtn);
        navPanel.add(salesBtn);
        navPanel.add(reportsBtn);

        JPanel southCombined = new JPanel(new BorderLayout());
        southCombined.setBackground(BACKGROUND);
        southCombined.add(bottomPanel, BorderLayout.CENTER);
        southCombined.add(navPanel, BorderLayout.SOUTH);
        add(southCombined, BorderLayout.SOUTH);
        inventoryBtn.addActionListener(e -> new InventoryFrame(currentFarmer));
        salesBtn.addActionListener(e -> new SalesFrame(currentFarmer));
        reportsBtn.addActionListener(e -> new ReportsFrame(currentFarmer));
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                keepMonitoring = false;
            }
        });

        refreshDashboardData();
        startStockMonitoringThread();

        setVisible(true);
    }

    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(PRIMARY_GREEN);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void startStockMonitoringThread() {
        Thread monitorThread = new Thread(() -> {
            while (keepMonitoring) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    break;
                }
                if (keepMonitoring) {
                    SwingUtilities.invokeLater(this::refreshDashboardData);
                }
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    private void refreshDashboardData() {
        int totalProducts = productDAO.getTotalProducts(currentFarmer.getFarmerId());
        int totalStock = productDAO.getTotalStock(currentFarmer.getFarmerId());
        int totalSales = salesDAO.getTotalSalesCount(currentFarmer.getFarmerId());
        double totalRevenue = salesDAO.getTotalRevenue(currentFarmer.getFarmerId());
        totalProductsValue.setText(String.valueOf(totalProducts));
        totalStockValue.setText(totalStock + " units");
        totalSalesValue.setText(String.valueOf(totalSales));
        totalRevenueValue.setText("Rs. " + String.format("%.2f", totalRevenue));

        alertContentPanel.removeAll();
        List<Product> lowStockList = productDAO.getLowStockProducts(currentFarmer.getFarmerId());

        if (!lowStockList.isEmpty()) {
            for (Product p : lowStockList) {
                JLabel item = new JLabel("⚠ " + p.getProductName() + " (" + p.getStockQuantity() + " " + p.getUnit() + " left)");
                item.setForeground(ALERT_RED);
                item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                alertContentPanel.add(item);
            }
        } else {
            JLabel none = new JLabel("No low stock alerts.");
            none.setForeground(new Color(120, 120, 120));
            none.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            alertContentPanel.add(none);
        }

        alertContentPanel.revalidate();
        alertContentPanel.repaint();
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(CARD_BG);
        outer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, accentColor),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        // Soft tinted background using the accent color
        JPanel tintPanel = new JPanel();
        tintPanel.setBackground(new Color(
                accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 18
        ));
        tintPanel.setLayout(new GridBagLayout()); // centers content both ways

        JPanel innerContent = new JPanel();
        innerContent.setOpaque(false);
        innerContent.setLayout(new BoxLayout(innerContent, BoxLayout.Y_AXIS));

        // Small colored circle "icon"
        JPanel dot = new JPanel();
        dot.setPreferredSize(new Dimension(14, 14));
        dot.setMaximumSize(new Dimension(14, 14));
        dot.setBackground(accentColor);
        dot.setAlignmentX(Component.CENTER_ALIGNMENT);
        dot.setBorder(BorderFactory.createEmptyBorder());

        JLabel titleLabel = new JLabel(title);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(new Color(100, 100, 100));

        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(new Color(40, 40, 40));

        innerContent.add(dot);
        innerContent.add(Box.createVerticalStrut(10));
        innerContent.add(titleLabel);
        innerContent.add(Box.createVerticalStrut(6));
        innerContent.add(valueLabel);

        tintPanel.add(innerContent);
        outer.add(tintPanel, BorderLayout.CENTER);

        return outer;
    }
}