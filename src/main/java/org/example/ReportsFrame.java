package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportsFrame extends JFrame {

    private Farmer currentFarmer;
    private JTabbedPane tabbedPane;

    private DefaultTableModel inventoryModel;
    private DefaultTableModel salesModel;

    private ProductDAO productDAO = new ProductDAO();
    private SalesDAO salesDAO = new SalesDAO();

    private static final Color PRIMARY_GREEN = new Color(76, 145, 65);
    private static final Color BROWN = new Color(121, 85, 61);
    private static final Color SKY_TINT = new Color(232, 244, 253);
    private static final Color BACKGROUND = new Color(245, 250, 240);
    private static final Color CARD_BG = Color.WHITE;

    private JLabel invStatProducts;
    private JLabel invStatValue;
    private JLabel salesStatTransactions;
    private JLabel salesStatRevenue;

    private HoverBarChartPanel inventoryChart;
    private HoverBarChartPanel salesChart;

    public ReportsFrame(Farmer farmer) {
        this.currentFarmer = farmer;

        setTitle(LanguageManager.get("reports.title"));
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout());

        add(buildBanner(), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBackground(BACKGROUND);
        mainContent.setBorder(new EmptyBorder(15, 20, 15, 20));
        add(mainContent, BorderLayout.CENTER);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(CARD_BG);

        tabbedPane.addTab(LanguageManager.get("reports.tab.inventory"), buildInventoryTab());
        tabbedPane.addTab(LanguageManager.get("reports.tab.sales"), buildSalesTab());

        mainContent.add(tabbedPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(BACKGROUND);
        AnimatedButton refreshBtn = new AnimatedButton(LanguageManager.get("reports.button.refresh"), new Color(140, 140, 140));
        AnimatedButton exportBtn = new AnimatedButton(LanguageManager.get("reports.button.export"), PRIMARY_GREEN);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(exportBtn);
        mainContent.add(buttonPanel, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> {
            loadInventoryReport();
            loadSalesReport();
        });
        exportBtn.addActionListener(e -> handleExport());

        loadInventoryReport();
        loadSalesReport();

        setVisible(true);
    }

    // ================= Banner =================
    private JPanel buildBanner() {
        PhotoBannerPanel banner = new PhotoBannerPanel("/images/reports_banner.jpg", LanguageManager.get("reports.title"));
        banner.setPreferredSize(new Dimension(1000, 130));
        banner.setLayout(null);

        CircleBackButton backBtn = new CircleBackButton();
        backBtn.setBounds(15, 15, 42, 42);
        backBtn.setToolTipText(LanguageManager.get("common.backtodashboard"));
        backBtn.addActionListener(e -> {
            dispose();
            new DashboardFrame(currentFarmer);
        });
        banner.add(backBtn);

        return banner;
    }

    // ================= Inventory Tab =================
    private JPanel buildInventoryTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel statsRow = new JPanel(new GridLayout(1, 2, 15, 0));
        statsRow.setBackground(BACKGROUND);
        invStatProducts = new JLabel();
        invStatValue = new JLabel();
        statsRow.add(buildStatCard(LanguageManager.get("reports.stat.totalproducts"), invStatProducts));
        statsRow.add(buildStatCard(LanguageManager.get("reports.stat.stockvalue"), invStatValue));
        panel.add(statsRow, BorderLayout.NORTH);

        inventoryChart = new HoverBarChartPanel(
                LanguageManager.get("reports.chart.inventory.title"), PRIMARY_GREEN, LanguageManager.get("reports.chart.nodata"));
        inventoryChart.setPreferredSize(new Dimension(0, 220));
        inventoryChart.setBackground(CARD_BG);
        inventoryChart.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        String[] invColumns = {"ID", "Name", "Category", "Stock", "Unit", "Price/Unit", "Stock Value (Rs.)"};
        inventoryModel = new DefaultTableModel(invColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable inventoryTable = new JTable(inventoryModel);
        styleTable(inventoryTable);
        JScrollPane invScroll = new JScrollPane(inventoryTable);
        invScroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        JPanel centerStack = new JPanel();
        centerStack.setLayout(new BoxLayout(centerStack, BoxLayout.Y_AXIS));
        centerStack.setBackground(BACKGROUND);
        centerStack.add(Box.createVerticalStrut(10));
        centerStack.add(inventoryChart);
        centerStack.add(Box.createVerticalStrut(10));
        centerStack.add(invScroll);

        panel.add(centerStack, BorderLayout.CENTER);
        return panel;
    }

    // ================= Sales Tab =================
    private JPanel buildSalesTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel statsRow = new JPanel(new GridLayout(1, 2, 15, 0));
        statsRow.setBackground(BACKGROUND);
        salesStatTransactions = new JLabel();
        salesStatRevenue = new JLabel();
        statsRow.add(buildStatCard(LanguageManager.get("reports.stat.transactions"), salesStatTransactions));
        statsRow.add(buildStatCard(LanguageManager.get("reports.stat.revenue"), salesStatRevenue));
        panel.add(statsRow, BorderLayout.NORTH);

        salesChart = new HoverBarChartPanel(
                LanguageManager.get("reports.chart.sales.title"), BROWN, LanguageManager.get("reports.chart.nodata"));
        salesChart.setPreferredSize(new Dimension(0, 220));
        salesChart.setBackground(CARD_BG);
        salesChart.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        String[] salesColumns = {"Sale ID", "Product ID", "Customer ID", "Qty", "Total Amount", "Date"};
        salesModel = new DefaultTableModel(salesColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable salesTable = new JTable(salesModel);
        styleTable(salesTable);
        JScrollPane salesScroll = new JScrollPane(salesTable);
        salesScroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        JPanel centerStack = new JPanel();
        centerStack.setLayout(new BoxLayout(centerStack, BoxLayout.Y_AXIS));
        centerStack.setBackground(BACKGROUND);
        centerStack.add(Box.createVerticalStrut(10));
        centerStack.add(salesChart);
        centerStack.add(Box.createVerticalStrut(10));
        centerStack.add(salesScroll);

        panel.add(centerStack, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStatCard(String label, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(SKY_TINT);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel titleLbl = new JLabel(label);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLbl.setForeground(new Color(90, 90, 90));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(PRIMARY_GREEN);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(210, 235, 200));
        table.setGridColor(new Color(240, 240, 240));
        table.setShowGrid(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(PRIMARY_GREEN);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 32));

        table.setDefaultRenderer(Object.class, new ZebraRenderer());
    }

    private static class ZebraRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : SKY_TINT);
            }
            return c;
        }
    }

    private void loadInventoryReport() {
        inventoryModel.setRowCount(0);
        List<Product> products = productDAO.getProductsByFarmer(currentFarmer.getFarmerId());

        double totalStockValue = 0;
        Map<String, Double> stockByProduct = new LinkedHashMap<>();

        for (Product p : products) {
            double stockValue = p.getStockQuantity() * p.getPricePerUnit();
            totalStockValue += stockValue;
            inventoryModel.addRow(new Object[]{
                    p.getProductId(), p.getProductName(), p.getCategory(),
                    p.getStockQuantity(), p.getUnit(), p.getPricePerUnit(),
                    String.format("%.2f", stockValue)
            });
            stockByProduct.put(p.getProductName(), (double) p.getStockQuantity());
        }

        invStatProducts.setText(String.valueOf(products.size()));
        invStatValue.setText("Rs. " + new DecimalFormat("#,##0.00").format(totalStockValue));

        inventoryChart.setData(stockByProduct);
    }

    private void loadSalesReport() {
        salesModel.setRowCount(0);
        List<Sale> sales = salesDAO.getAllSales(currentFarmer.getFarmerId());

        Map<Integer, String> productNames = new LinkedHashMap<>();
        for (Product p : productDAO.getProductsByFarmer(currentFarmer.getFarmerId())) {
            productNames.put(p.getProductId(), p.getProductName());
        }

        Map<String, Double> revenueByProduct = new LinkedHashMap<>();

        for (Sale s : sales) {
            salesModel.addRow(new Object[]{
                    s.getSaleId(), s.getProductId(), s.getCustomerId(),
                    s.getQuantitySold(), s.getTotalAmount(), s.getSaleDate()
            });
            String name = productNames.getOrDefault(s.getProductId(), "Product #" + s.getProductId());
            revenueByProduct.merge(name, s.getTotalAmount(), Double::sum);
        }

        int totalSales = salesDAO.getTotalSalesCount(currentFarmer.getFarmerId());
        double totalRevenue = salesDAO.getTotalRevenue(currentFarmer.getFarmerId());

        salesStatTransactions.setText(String.valueOf(totalSales));
        salesStatRevenue.setText("Rs. " + new DecimalFormat("#,##0.00").format(totalRevenue));

        salesChart.setData(revenueByProduct);
    }

    private void handleExport() {
        int selectedTab = tabbedPane.getSelectedIndex();
        DefaultTableModel modelToExport = (selectedTab == 0) ? inventoryModel : salesModel;
        String reportName = (selectedTab == 0) ? "Inventory_Report" : "Sales_Report";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(reportName + ".txt"));
        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

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

                JOptionPane.showMessageDialog(this, LanguageManager.get("reports.export.success"));

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, LanguageManager.get("reports.export.error") + ex.getMessage());
            }
        }
    }

    // ================= Photo hero banner =================
    private class PhotoBannerPanel extends JPanel {
        private Image bgImage;
        private String title;

        PhotoBannerPanel(String resourcePath, String title) {
            this.title = title;
            try {
                java.net.URL url = getClass().getResource(resourcePath);
                if (url != null) {
                    bgImage = new ImageIcon(url).getImage();
                }
            } catch (Exception ex) {
                bgImage = null;
            }
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (bgImage != null) {
                int imgW = bgImage.getWidth(this);
                int imgH = bgImage.getHeight(this);
                double scale = Math.max((double) w / imgW, (double) h / imgH);
                int drawW = (int) (imgW * scale);
                int drawH = (int) (imgH * scale);
                int x = (w - drawW) / 2;
                int y = (h - drawH) / 2;
                g2.drawImage(bgImage, x, y, drawW, drawH, this);
            } else {
                g2.setColor(PRIMARY_GREEN);
                g2.fillRect(0, 0, w, h);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.setColor(new Color(255, 255, 255, 180));
                g2.drawString("(reports_banner.jpg not found in resources/images)", 70, h - 12);
            }

            GradientPaint overlay = new GradientPaint(0, 0, new Color(0, 0, 0, 160), w, 0, new Color(0, 0, 0, 60));
            g2.setPaint(overlay);
            g2.fillRect(0, 0, w, h);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            int textY = h / 2 + fm.getAscent() / 2 - 4;
            g2.drawString(title, 70, textY);

            g2.dispose();
        }
    }

    // ================= Circular animated back button =================
    private class CircleBackButton extends JButton {
        private float hoverProgress = 0f;
        private Timer timer;

        CircleBackButton() {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { animateTo(1f); }
                @Override
                public void mouseExited(MouseEvent e) { animateTo(0f); }
            });
        }

        private void animateTo(float target) {
            if (timer != null && timer.isRunning()) timer.stop();
            timer = new Timer(15, null);
            timer.addActionListener(e -> {
                float step = 0.15f;
                if (hoverProgress < target) hoverProgress = Math.min(target, hoverProgress + step);
                else if (hoverProgress > target) hoverProgress = Math.max(target, hoverProgress - step);
                repaint();
                if (hoverProgress == target) timer.stop();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Color base = new Color(255, 255, 255, 210);
            Color hover = Color.WHITE;
            int r = (int) (base.getRed() + (hover.getRed() - base.getRed()) * hoverProgress);
            int gr = (int) (base.getGreen() + (hover.getGreen() - base.getGreen()) * hoverProgress);
            int b = (int) (base.getBlue() + (hover.getBlue() - base.getBlue()) * hoverProgress);
            int a = (int) (base.getAlpha() + (hover.getAlpha() - base.getAlpha()) * hoverProgress);

            g2.setColor(new Color(r, gr, b, a));
            g2.fillOval(0, 0, w - 1, h - 1);

            g2.setColor(PRIMARY_GREEN);
            g2.setStroke(new BasicStroke(2.5f));
            int cx = w / 2, cy = h / 2, armLen = 7;
            g2.drawLine(cx + armLen - 2, cy - armLen, cx - armLen + 1, cy);
            g2.drawLine(cx + armLen - 2, cy + armLen, cx - armLen + 1, cy);
            g2.drawLine(cx - armLen + 1, cy, cx + armLen + 2, cy);

            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(42, 42);
        }
    }

    // ================= Animated lift + brighten button =================
    private class AnimatedButton extends JButton {
        private Color baseColor;
        private float liftProgress = 0f;
        private Timer timer;

        AnimatedButton(String text, Color baseColor) {
            super(text);
            this.baseColor = baseColor;
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(12, 26, 16, 26));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { animateTo(1f); }
                @Override
                public void mouseExited(MouseEvent e) { animateTo(0f); }
            });
        }

        private void animateTo(float target) {
            if (timer != null && timer.isRunning()) timer.stop();
            timer = new Timer(15, null);
            timer.addActionListener(e -> {
                float step = 0.12f;
                if (liftProgress < target) liftProgress = Math.min(target, liftProgress + step);
                else if (liftProgress > target) liftProgress = Math.max(target, liftProgress - step);
                repaint();
                if (liftProgress == target) timer.stop();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int lift = (int) (4 * liftProgress);

            g2.setColor(new Color(0, 0, 0, 40 + (int) (20 * liftProgress)));
            g2.fillRoundRect(4, h - 10 + lift, w - 8, 10, 12, 12);

            Color fill = liftProgress > 0 ? baseColor.brighter() : baseColor;
            g2.setColor(fill);
            g2.fillRoundRect(0, -lift, w, h - lift, 16, 16);

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            String text = getText();
            int textX = (w - fm.stringWidth(text)) / 2;
            int textY = (h - lift) / 2 + fm.getAscent() / 2 - 2 - lift / 2;
            g2.setColor(Color.WHITE);
            g2.drawString(text, textX, textY);

            g2.dispose();
        }
    }

    // ================= Bar chart with hover tooltip =================
    private class HoverBarChartPanel extends JPanel {
        private String title;
        private Color barColor;
        private String noDataText;
        private List<String> labels = new ArrayList<>();
        private List<Double> values = new ArrayList<>();
        private int hoveredIndex = -1;

        private final int chartLeft = 60, chartRight = 20, chartTop = 30, chartBottom = 40;

        HoverBarChartPanel(String title, Color barColor, String noDataText) {
            this.title = title;
            this.barColor = barColor;
            this.noDataText = noDataText;

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int idx = indexAt(e.getX());
                    if (idx != hoveredIndex) {
                        hoveredIndex = idx;
                        repaint();
                    }
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hoveredIndex = -1;
                    repaint();
                }
            });
        }

        void setData(Map<String, Double> data) {
            labels.clear();
            values.clear();
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                labels.add(entry.getKey());
                values.add(entry.getValue());
            }
            hoveredIndex = -1;
            repaint();
        }

        private int indexAt(int mouseX) {
            if (labels.isEmpty()) return -1;
            int plotW = getWidth() - chartLeft - chartRight;
            int n = labels.size();
            double slot = (double) plotW / n;
            int i = (int) ((mouseX - chartLeft) / slot);
            if (i < 0 || i >= n) return -1;
            return i;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.setColor(new Color(60, 60, 60));
            g2.drawString(title, 15, 20);

            if (labels.isEmpty()) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.setColor(new Color(150, 150, 150));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(noDataText, (w - fm.stringWidth(noDataText)) / 2, h / 2);
                g2.dispose();
                return;
            }

            int plotW = w - chartLeft - chartRight;
            int plotH = h - chartTop - chartBottom;

            double maxVal = 0;
            for (double v : values) maxVal = Math.max(maxVal, v);
            if (maxVal <= 0) maxVal = 1;

            int gridLines = 4;
            for (int i = 0; i <= gridLines; i++) {
                int y = chartTop + plotH - (int) ((double) i / gridLines * plotH);
                g2.setColor(new Color(230, 230, 230));
                g2.drawLine(chartLeft, y, w - chartRight, y);
                double val = maxVal * i / gridLines;
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.setColor(new Color(120, 120, 120));
                g2.drawString(new DecimalFormat("#,##0").format(val), 5, y + 4);
            }

            g2.setColor(new Color(180, 180, 180));
            g2.drawLine(chartLeft, chartTop, chartLeft, chartTop + plotH);
            g2.drawLine(chartLeft, chartTop + plotH, w - chartRight, chartTop + plotH);

            int n = labels.size();
            double slot = (double) plotW / n;
            int barWidth = (int) Math.min(slot * 0.55, 55);

            for (int i = 0; i < n; i++) {
                double v = values.get(i);
                int barHeight = (int) (v / maxVal * plotH);
                int x = chartLeft + (int) (i * slot + (slot - barWidth) / 2);
                int y = chartTop + plotH - barHeight;

                boolean hovered = (i == hoveredIndex);
                g2.setColor(hovered ? barColor.brighter() : barColor);
                g2.fillRoundRect(x, y, barWidth, barHeight, 6, 6);

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.setColor(new Color(90, 90, 90));
                String lbl = labels.get(i);
                if (lbl.length() > 8) lbl = lbl.substring(0, 7) + "…";
                FontMetrics lfm = g2.getFontMetrics();
                int lblX = x + (barWidth - lfm.stringWidth(lbl)) / 2;
                g2.drawString(lbl, lblX, chartTop + plotH + 15);
            }

            if (hoveredIndex >= 0) {
                double v = values.get(hoveredIndex);
                String label = labels.get(hoveredIndex);
                String tip = label + " : " + new DecimalFormat("#,##0.##").format(v);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                int tipW = fm.stringWidth(tip) + 16;
                int tipH = 24;

                int barX = chartLeft + (int) (hoveredIndex * slot + slot / 2);
                int barHeight = (int) (v / maxVal * plotH);
                int barTopY = chartTop + plotH - barHeight;

                int tipX = Math.min(Math.max(barX - tipW / 2, chartLeft), w - chartRight - tipW);
                int tipY = Math.max(barTopY - tipH - 8, chartTop);

                g2.setColor(new Color(50, 50, 50, 230));
                g2.fillRoundRect(tipX, tipY, tipW, tipH, 8, 8);
                g2.setColor(Color.WHITE);
                g2.drawString(tip, tipX + 8, tipY + 16);
            }

            g2.dispose();
        }
    }
}