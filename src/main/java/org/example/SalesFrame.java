package org.example;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SalesFrame extends JFrame {

    private Farmer currentFarmer;

    private JComboBox<String> productDropdown;
    private JComboBox<String> customerDropdown;
    private AnimatedTextField quantityField;
    private JLabel totalLabel;
    private JTable salesTable;
    private DefaultTableModel tableModel;
    private LineChartPanel chartPanel;

    private ProductDAO productDAO = new ProductDAO();
    private CustomerDAO customerDAO = new CustomerDAO();
    private SalesDAO salesDAO = new SalesDAO();

    private List<Product> productList;
    private List<Customer> customerList;

    private static final Color PAGE_BG = new Color(224, 242, 250);
    private static final Color CARD_BG = new Color(233, 247, 252);
    private static final Color ROW_ALT_BG = new Color(220, 240, 248);
    private static final Color ACCENT_GREEN = new Color(67, 160, 71);
    private static final Color ACCENT_GREEN_DARK = new Color(50, 130, 55);
    private static final Color ACCENT_SKYBLUE = new Color(41, 172, 223);
    private static final Color ACCENT_BROWN = new Color(160, 110, 68);
    private static final Color TEXT_MUTED = new Color(90, 90, 90);
    private static final Color TEXT_DARK = new Color(38, 45, 38);

    public SalesFrame(Farmer farmer) {
        this.currentFarmer = farmer;

        setTitle(LanguageManager.get("sales.title"));
        setSize(1000, 850);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(PAGE_BG);
        setLayout(new BorderLayout(10, 10));

        // ================= HERO BANNER =================

        HeroBannerPanel heroBanner = new HeroBannerPanel(
                "/images/sales_banner.jpg",
                LanguageManager.get("sales.title"),
                () -> {
                    dispose();
                    new DashboardFrame(currentFarmer);
                }
        );

        heroBanner.setPreferredSize(new Dimension(10, 130));
        add(heroBanner, BorderLayout.NORTH);

        // ================= MAIN CONTENT =================

        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBackground(PAGE_BG);
        mainContent.setBorder(
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        );

        add(mainContent, BorderLayout.CENTER);

        // ================= NEW SALE FORM =================

        JPanel formPanel = new JPanel(new GridLayout(2, 4, 10, 8));

        formPanel.setBackground(CARD_BG);

        formPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createLineBorder(
                                        new Color(200, 225, 235)
                                ),
                                LanguageManager.get("sales.form.title"),
                                0,
                                0,
                                new Font("Nirmala UI", Font.BOLD, 13),
                                TEXT_MUTED
                        ),
                        BorderFactory.createEmptyBorder(10, 12, 12, 12)
                )
        );

        productDropdown = new JComboBox<>();
        customerDropdown = new JComboBox<>();
        quantityField = new AnimatedTextField();

        styleCombo(productDropdown);
        styleCombo(customerDropdown);

        totalLabel = new JLabel(
                LanguageManager.get("sales.total.prefix") + "0.00"
        );

        totalLabel.setFont(
                new Font("Nirmala UI", Font.BOLD, 15)
        );

        totalLabel.setForeground(ACCENT_GREEN_DARK);

        // ================= LIVE TOTAL CALCULATION =================
        // Whenever quantity is typed/deleted, total will update.

        quantityField.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        updateTotal();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        updateTotal();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        updateTotal();
                    }
                }
        );

        // Whenever product is changed, total will update.

        productDropdown.addActionListener(e -> updateTotal());

        // ================= FORM LABELS =================

        formPanel.add(
                formLabel(LanguageManager.get("sales.label.product"))
        );

        formPanel.add(
                formLabel(LanguageManager.get("sales.label.customer"))
        );

        formPanel.add(
                formLabel(LanguageManager.get("sales.label.quantity"))
        );

        formPanel.add(new JLabel(""));

        formPanel.add(productDropdown);
        formPanel.add(customerDropdown);
        formPanel.add(quantityField);
        formPanel.add(totalLabel);

        mainContent.add(formPanel, BorderLayout.NORTH);

        // ================= CENTER =================

        JPanel centerWrap = new JPanel(
                new BorderLayout(0, 12)
        );

        centerWrap.setBackground(PAGE_BG);

        centerWrap.setBorder(
                BorderFactory.createEmptyBorder(15, 0, 0, 0)
        );

        // ================= CHART CARD =================

        JPanel chartCard = new JPanel(new BorderLayout());

        chartCard.setBackground(CARD_BG);

        chartCard.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(200, 225, 235)
                        ),
                        BorderFactory.createEmptyBorder(
                                12, 16, 10, 16
                        )
                )
        );

        chartCard.setPreferredSize(
                new Dimension(10, 200)
        );

        JLabel chartTitle = new JLabel(
                LanguageManager.get("sales.chart.title")
        );

        chartTitle.setFont(
                new Font("Nirmala UI", Font.BOLD, 14)
        );

        chartTitle.setForeground(TEXT_DARK);

        chartTitle.setBorder(
                BorderFactory.createEmptyBorder(0, 0, 8, 0)
        );

        chartCard.add(chartTitle, BorderLayout.NORTH);

        chartPanel = new LineChartPanel();

        chartCard.add(
                chartPanel,
                BorderLayout.CENTER
        );

        centerWrap.add(
                chartCard,
                BorderLayout.NORTH
        );

        // ================= SALES TABLE =================

        String[] columns = {
                LanguageManager.get("sales.col.saleid"),
                LanguageManager.get("sales.col.productid"),
                LanguageManager.get("sales.col.customerid"),
                LanguageManager.get("sales.col.qty"),
                LanguageManager.get("sales.col.total"),
                LanguageManager.get("sales.col.date")
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column
            ) {
                return false;
            }
        };

        salesTable = new JTable(tableModel);

        salesTable.setRowHeight(30);

        salesTable.setFont(
                new Font("Nirmala UI", Font.PLAIN, 13)
        );

        salesTable.setSelectionBackground(
                new Color(190, 225, 245)
        );

        salesTable.setGridColor(
                new Color(225, 238, 245)
        );

        salesTable.setShowGrid(false);

        salesTable.setIntercellSpacing(
                new Dimension(0, 0)
        );

        // ================= ZEBRA TABLE =================

        DefaultTableCellRenderer zebraRenderer =
                new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row,
                            int column
                    ) {

                        Component c =
                                super.getTableCellRendererComponent(
                                        table,
                                        value,
                                        isSelected,
                                        hasFocus,
                                        row,
                                        column
                                );

                        if (!isSelected) {
                            c.setBackground(
                                    row % 2 == 0
                                            ? Color.WHITE
                                            : ROW_ALT_BG
                            );
                        }

                        setBorder(
                                BorderFactory.createEmptyBorder(
                                        0, 10, 0, 10
                                )
                        );

                        return c;
                    }
                };

        for (int i = 0;
             i < salesTable.getColumnCount();
             i++) {

            salesTable.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(zebraRenderer);
        }

        JTableHeader header =
                salesTable.getTableHeader();

        header.setFont(
                new Font("Nirmala UI", Font.BOLD, 13)
        );

        header.setBackground(ACCENT_GREEN);

        header.setForeground(Color.WHITE);

        header.setPreferredSize(
                new Dimension(0, 34)
        );

        JScrollPane scrollPane =
                new JScrollPane(salesTable);

        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        new Color(200, 225, 235)
                )
        );

        JPanel tableContainer =
                new JPanel(new BorderLayout());

        tableContainer.setBackground(PAGE_BG);

        tableContainer.add(
                scrollPane,
                BorderLayout.CENTER
        );

        centerWrap.add(
                tableContainer,
                BorderLayout.CENTER
        );

        mainContent.add(
                centerWrap,
                BorderLayout.CENTER
        );

        // ================= BOTTOM BUTTONS =================

        JPanel buttonPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER,
                                10,
                                8
                        )
                );

        buttonPanel.setBackground(PAGE_BG);

        AnimatedActionButton recordSaleBtn =
                createActionButton(
                        LanguageManager.get("sales.button.record"),
                        ACCENT_GREEN
                );

        AnimatedActionButton addCustomerBtn =
                createActionButton(
                        LanguageManager.get("sales.button.addcustomer"),
                        ACCENT_SKYBLUE
                );

        AnimatedActionButton refreshBtn =
                createActionButton(
                        LanguageManager.get("sales.button.refresh"),
                        ACCENT_BROWN
                );

        buttonPanel.add(recordSaleBtn);
        buttonPanel.add(addCustomerBtn);
        buttonPanel.add(refreshBtn);

        mainContent.add(
                buttonPanel,
                BorderLayout.SOUTH
        );

        // ================= BUTTON EVENTS =================

        recordSaleBtn.addActionListener(
                e -> handleRecordSale()
        );

        addCustomerBtn.addActionListener(
                e -> handleAddCustomer()
        );

        refreshBtn.addActionListener(
                e -> loadSalesHistory()
        );

        // ================= LOAD DATA =================

        loadProducts();
        loadCustomers();
        loadSalesHistory();

        setVisible(true);
    }

    // =========================================================
    // LIVE TOTAL CALCULATION
    // =========================================================

    private void updateTotal() {

        try {

            // Make sure product list exists
            if (productList == null ||
                    productList.isEmpty()) {

                totalLabel.setText(
                        LanguageManager.get("sales.total.prefix")
                                + "0.00"
                );

                return;
            }

            int productIndex =
                    productDropdown.getSelectedIndex();

            if (productIndex < 0 ||
                    productIndex >= productList.size()) {

                totalLabel.setText(
                        LanguageManager.get("sales.total.prefix")
                                + "0.00"
                );

                return;
            }

            String quantityText =
                    quantityField.getText().trim();

            // No quantity entered
            if (quantityText.isEmpty()) {

                totalLabel.setText(
                        LanguageManager.get("sales.total.prefix")
                                + "0.00"
                );

                return;
            }

            int quantity =
                    Integer.parseInt(quantityText);

            // Don't calculate negative quantity
            if (quantity < 0) {

                totalLabel.setText(
                        LanguageManager.get("sales.total.prefix")
                                + "0.00"
                );

                return;
            }

            Product selectedProduct =
                    productList.get(productIndex);

            double pricePerUnit =
                    selectedProduct.getPricePerUnit();

            double total =
                    quantity * pricePerUnit;

            // UPDATE GUI
            totalLabel.setText(
                    LanguageManager.get("sales.total.prefix")
                            + String.format("%.2f", total)
            );

        } catch (NumberFormatException ex) {

            // While user is typing an invalid value,
            // simply show zero instead of crashing.

            totalLabel.setText(
                    LanguageManager.get("sales.total.prefix")
                            + "0.00"
            );
        }
    }

    // =========================================================
    // DAY-WISE SALES LINE CHART
    // =========================================================

    private static class LineChartPanel extends JPanel {

        private Map<String, Double> dailyTotals =
                new TreeMap<>();

        private List<Point> plottedPoints =
                new ArrayList<>();

        private List<String> dateKeys =
                new ArrayList<>();

        private List<Double> plottedValues =
                new ArrayList<>();

        private int hoveredIndex = -1;

        private float animProgress = 1f;

        private Timer animTimer;

        private static final int LEFT_MARGIN = 55;
        private static final int RIGHT_MARGIN = 20;
        private static final int TOP_MARGIN = 15;
        private static final int BOTTOM_MARGIN = 35;

        public LineChartPanel() {

            setOpaque(false);

            addMouseMotionListener(
                    new MouseMotionAdapter() {

                        @Override
                        public void mouseMoved(MouseEvent e) {
                            updateHoveredIndex(e.getX());
                        }
                    }
            );

            addMouseListener(
                    new MouseAdapter() {

                        @Override
                        public void mouseExited(MouseEvent e) {

                            hoveredIndex = -1;

                            repaint();
                        }
                    }
            );
        }

        public void updateData(List<Sale> sales) {

            Map<String, Double> totals =
                    new TreeMap<>();

            for (Sale s : sales) {

                String key =
                        extractDateKey(
                                s.getSaleDate()
                        );

                totals.merge(
                        key,
                        s.getTotalAmount(),
                        Double::sum
                );
            }

            this.dailyTotals = totals;

            hoveredIndex = -1;

            playEntrance();
        }

        private String extractDateKey(
                Object saleDate
        ) {

            if (saleDate == null)
                return "N/A";

            String s =
                    saleDate.toString();

            return s.length() >= 10
                    ? s.substring(0, 10)
                    : s;
        }

        private void playEntrance() {

            animProgress = 0f;

            if (animTimer != null &&
                    animTimer.isRunning()) {

                animTimer.stop();
            }

            animTimer =
                    new Timer(
                            15,
                            e -> {

                                animProgress =
                                        Math.min(
                                                1f,
                                                animProgress + 0.06f
                                        );

                                repaint();

                                if (animProgress >= 1f) {

                                    ((Timer) e.getSource())
                                            .stop();
                                }
                            }
                    );

            animTimer.start();
        }

        private void updateHoveredIndex(
                int mouseX
        ) {

            if (plottedPoints.isEmpty())
                return;

            int nearest = -1;

            int bestDist =
                    Integer.MAX_VALUE;

            for (int i = 0;
                 i < plottedPoints.size();
                 i++) {

                int dist =
                        Math.abs(
                                plottedPoints.get(i).x
                                        - mouseX
                        );

                if (dist < bestDist) {

                    bestDist = dist;

                    nearest = i;
                }
            }

            int newHover =
                    (nearest != -1 &&
                            bestDist < 40)
                            ? nearest
                            : -1;

            if (newHover != hoveredIndex) {

                hoveredIndex = newHover;

                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int w = getWidth();
            int h = getHeight();

            plottedPoints.clear();
            dateKeys.clear();
            plottedValues.clear();

            if (dailyTotals.isEmpty()) {

                g2.setColor(TEXT_MUTED);

                g2.setFont(
                        new Font(
                                "Nirmala UI",
                                Font.PLAIN,
                                13
                        )
                );

                g2.drawString(
                        LanguageManager.get(
                                "sales.chart.nodata"
                        ),
                        20,
                        h / 2
                );

                g2.dispose();

                return;
            }

            int chartLeft =
                    LEFT_MARGIN;

            int chartRight =
                    w - RIGHT_MARGIN;

            int chartTop =
                    TOP_MARGIN;

            int chartBottom =
                    h - BOTTOM_MARGIN;

            double maxVal = 0;

            for (double v :
                    dailyTotals.values()) {

                if (v > maxVal)
                    maxVal = v;
            }

            if (maxVal <= 0)
                maxVal = 1;

            maxVal *= 1.1;

            // ================= Y AXIS =================

            int divisions = 4;

            g2.setFont(
                    new Font(
                            "Nirmala UI",
                            Font.PLAIN,
                            10
                    )
            );

            for (int i = 0;
                 i <= divisions;
                 i++) {

                int y =
                        chartBottom
                                - (chartBottom - chartTop)
                                * i
                                / divisions;

                double value =
                        maxVal * i / divisions;

                g2.setColor(
                        new Color(220, 232, 238)
                );

                g2.drawLine(
                        chartLeft,
                        y,
                        chartRight,
                        y
                );

                g2.setColor(TEXT_MUTED);

                String label =
                        String.format(
                                "%.0f",
                                value
                        );

                FontMetrics fm =
                        g2.getFontMetrics();

                int labelW =
                        fm.stringWidth(label);

                g2.drawString(
                        label,
                        chartLeft - labelW - 8,
                        y + 4
                );
            }

            // ================= AXES =================

            g2.setColor(
                    new Color(160, 185, 195)
            );

            g2.setStroke(
                    new BasicStroke(1.5f)
            );

            g2.drawLine(
                    chartLeft,
                    chartTop,
                    chartLeft,
                    chartBottom
            );

            g2.drawLine(
                    chartLeft,
                    chartBottom,
                    chartRight,
                    chartBottom
            );

            // ================= POINTS =================

            int n =
                    dailyTotals.size();

            int stepX =
                    n > 1
                            ? (chartRight - chartLeft)
                            / (n - 1)
                            : 0;

            int i = 0;

            for (Map.Entry<String, Double> entry :
                    dailyTotals.entrySet()) {

                int x =
                        n > 1
                                ? chartLeft + i * stepX
                                : (chartLeft + chartRight) / 2;

                double ratio =
                        entry.getValue()
                                / maxVal;

                int fullY =
                        chartBottom
                                - (int)
                                (ratio *
                                        (chartBottom - chartTop));

                int animatedY =
                        chartBottom
                                - (int)
                                (
                                        (chartBottom - fullY)
                                                * animProgress
                                );

                plottedPoints.add(
                        new Point(x, animatedY)
                );

                dateKeys.add(
                        entry.getKey()
                );

                plottedValues.add(
                        entry.getValue()
                );

                i++;
            }

            // ================= AREA =================

            if (plottedPoints.size() >= 1) {

                GeneralPath areaPath =
                        new GeneralPath();

                Point first =
                        plottedPoints.get(0);

                areaPath.moveTo(
                        first.x,
                        chartBottom
                );

                areaPath.lineTo(
                        first.x,
                        first.y
                );

                for (int p = 1;
                     p < plottedPoints.size();
                     p++) {

                    Point pt =
                            plottedPoints.get(p);

                    areaPath.lineTo(
                            pt.x,
                            pt.y
                    );
                }

                Point last =
                        plottedPoints.get(
                                plottedPoints.size() - 1
                        );

                areaPath.lineTo(
                        last.x,
                        chartBottom
                );

                areaPath.closePath();

                GradientPaint areaFill =
                        new GradientPaint(
                                0,
                                chartTop,
                                new Color(
                                        ACCENT_SKYBLUE.getRed(),
                                        ACCENT_SKYBLUE.getGreen(),
                                        ACCENT_SKYBLUE.getBlue(),
                                        90
                                ),
                                0,
                                chartBottom,
                                new Color(
                                        ACCENT_SKYBLUE.getRed(),
                                        ACCENT_SKYBLUE.getGreen(),
                                        ACCENT_SKYBLUE.getBlue(),
                                        10
                                )
                        );

                g2.setPaint(areaFill);

                g2.fill(areaPath);
            }

            // ================= LINE =================

            g2.setStroke(
                    new BasicStroke(2.5f)
            );

            g2.setColor(
                    ACCENT_SKYBLUE
            );

            for (int p = 0;
                 p < plottedPoints.size() - 1;
                 p++) {

                Point p1 =
                        plottedPoints.get(p);

                Point p2 =
                        plottedPoints.get(p + 1);

                g2.drawLine(
                        p1.x,
                        p1.y,
                        p2.x,
                        p2.y
                );
            }

            // ================= X AXIS LABELS =================

            g2.setFont(
                    new Font(
                            "Nirmala UI",
                            Font.PLAIN,
                            10
                    )
            );

            int maxLabels =
                    Math.max(
                            1,
                            (chartRight - chartLeft) / 60
                    );

            int labelStep =
                    Math.max(
                            1,
                            (int)
                                    Math.ceil(
                                            (double) n
                                                    / maxLabels
                                    )
                    );

            for (int p = 0;
                 p < plottedPoints.size();
                 p++) {

                if (p % labelStep != 0 &&
                        p != plottedPoints.size() - 1)
                    continue;

                Point pt =
                        plottedPoints.get(p);

                String dateKey =
                        dateKeys.get(p);

                String shortLabel =
                        dateKey.length() >= 10
                                ? dateKey.substring(5)
                                : dateKey;

                g2.setColor(TEXT_MUTED);

                FontMetrics fm =
                        g2.getFontMetrics();

                int labelW =
                        fm.stringWidth(shortLabel);

                g2.drawString(
                        shortLabel,
                        pt.x - labelW / 2,
                        chartBottom + 16
                );
            }

            // ================= POINTS =================

            for (int p = 0;
                 p < plottedPoints.size();
                 p++) {

                Point pt =
                        plottedPoints.get(p);

                boolean isHover =
                        p == hoveredIndex;

                int r =
                        isHover ? 7 : 4;

                g2.setColor(
                        isHover
                                ? ACCENT_GREEN_DARK
                                : ACCENT_GREEN
                );

                g2.fillOval(
                        pt.x - r,
                        pt.y - r,
                        r * 2,
                        r * 2
                );

                if (isHover) {

                    g2.setColor(
                            new Color(
                                    ACCENT_GREEN_DARK.getRed(),
                                    ACCENT_GREEN_DARK.getGreen(),
                                    ACCENT_GREEN_DARK.getBlue(),
                                    60
                            )
                    );

                    g2.fillOval(
                            pt.x - r - 5,
                            pt.y - r - 5,
                            (r + 5) * 2,
                            (r + 5) * 2
                    );
                }
            }

            // ================= TOOLTIP =================

            if (hoveredIndex >= 0 &&
                    hoveredIndex < plottedPoints.size()) {

                Point pt =
                        plottedPoints.get(
                                hoveredIndex
                        );

                g2.setColor(
                        new Color(
                                150,
                                150,
                                150,
                                150
                        )
                );

                float[] dashPattern =
                        {4f, 4f};

                g2.setStroke(
                        new BasicStroke(
                                1f,
                                BasicStroke.CAP_BUTT,
                                BasicStroke.JOIN_MITER,
                                10f,
                                dashPattern,
                                0f
                        )
                );

                g2.drawLine(
                        pt.x,
                        chartTop,
                        pt.x,
                        chartBottom
                );

                String dateText =
                        dateKeys.get(
                                hoveredIndex
                        );

                String valueText =
                        LanguageManager.get(
                                "sales.currency"
                        )
                                +
                                String.format(
                                        "%.2f",
                                        plottedValues.get(
                                                hoveredIndex
                                        )
                                );

                g2.setFont(
                        new Font(
                                "Nirmala UI",
                                Font.BOLD,
                                12
                        )
                );

                FontMetrics fmVal =
                        g2.getFontMetrics();

                g2.setFont(
                        new Font(
                                "Nirmala UI",
                                Font.PLAIN,
                                11
                        )
                );

                FontMetrics fmDate =
                        g2.getFontMetrics();

                int boxW =
                        Math.max(
                                fmVal.stringWidth(
                                        valueText
                                ),
                                fmDate.stringWidth(
                                        dateText
                                )
                        ) + 20;

                int boxH = 44;

                int boxX =
                        Math.min(
                                Math.max(
                                        pt.x - boxW / 2,
                                        chartLeft
                                ),
                                chartRight - boxW
                        );

                int boxY =
                        Math.max(
                                pt.y - boxH - 14,
                                chartTop
                        );

                g2.setColor(
                        new Color(
                                30,
                                40,
                                30,
                                30
                        )
                );

                g2.fillRoundRect(
                        boxX + 2,
                        boxY + 3,
                        boxW,
                        boxH,
                        10,
                        10
                );

                g2.setColor(Color.WHITE);

                g2.fillRoundRect(
                        boxX,
                        boxY,
                        boxW,
                        boxH,
                        10,
                        10
                );

                g2.setColor(
                        ACCENT_SKYBLUE
                );

                g2.setStroke(
                        new BasicStroke(1.5f)
                );

                g2.drawRoundRect(
                        boxX,
                        boxY,
                        boxW,
                        boxH,
                        10,
                        10
                );

                g2.setColor(TEXT_MUTED);

                g2.setFont(
                        new Font(
                                "Nirmala UI",
                                Font.PLAIN,
                                11
                        )
                );

                g2.drawString(
                        dateText,
                        boxX + 10,
                        boxY + 17
                );

                g2.setColor(
                        ACCENT_GREEN_DARK
                );

                g2.setFont(
                        new Font(
                                "Nirmala UI",
                                Font.BOLD,
                                12
                        )
                );

                g2.drawString(
                        valueText,
                        boxX + 10,
                        boxY + 34
                );
            }

            g2.dispose();
        }
    }

    // =========================================================
    // HERO BANNER
    // =========================================================

    private static class HeroBannerPanel
            extends JPanel {

        private Image bannerImage;

        private final String titleText;

        private static final int TEXT_OFFSET_X = 70;

        public HeroBannerPanel(
                String resourcePath,
                String titleText,
                Runnable onBack
        ) {

            this.titleText = titleText;

            setOpaque(false);

            setLayout(
                    new BorderLayout()
            );

            java.net.URL imgUrl =
                    getClass().getResource(
                            resourcePath
                    );

            if (imgUrl != null) {

                bannerImage =
                        new ImageIcon(
                                imgUrl
                        ).getImage();
            }

            AnimatedBackButton backBtn =
                    new AnimatedBackButton();

            backBtn.addActionListener(
                    e -> onBack.run()
            );

            JPanel backWrap =
                    new JPanel(
                            new FlowLayout(
                                    FlowLayout.LEFT,
                                    16,
                                    0
                            )
                    );

            backWrap.setOpaque(false);

            backWrap.add(backBtn);

            add(
                    backWrap,
                    BorderLayout.WEST
            );
        }

        @Override
        protected void paintComponent(
                Graphics g
        ) {

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int w = getWidth();
            int h = getHeight();

            if (bannerImage != null) {

                int imgW =
                        bannerImage.getWidth(this);

                int imgH =
                        bannerImage.getHeight(this);

                if (imgW > 0 && imgH > 0) {

                    double panelRatio =
                            (double) w / h;

                    double imgRatio =
                            (double) imgW / imgH;

                    int drawW;
                    int drawH;
                    int drawX;
                    int drawY;

                    if (imgRatio > panelRatio) {

                        drawH = h;

                        drawW =
                                (int)
                                        (h * imgRatio);

                        drawX =
                                (w - drawW) / 2;

                        drawY = 0;

                    } else {

                        drawW = w;

                        drawH =
                                (int)
                                        (w / imgRatio);

                        drawX = 0;

                        drawY =
                                (h - drawH) / 2;
                    }

                    g2.drawImage(
                            bannerImage,
                            drawX,
                            drawY,
                            drawW,
                            drawH,
                            this
                    );
                }

                GradientPaint overlay =
                        new GradientPaint(
                                0,
                                0,
                                new Color(
                                        20,
                                        40,
                                        20,
                                        150
                                ),
                                0,
                                h,
                                new Color(
                                        20,
                                        40,
                                        20,
                                        60
                                )
                        );

                g2.setPaint(overlay);

                g2.fillRect(
                        0,
                        0,
                        w,
                        h
                );

            } else {

                g2.setColor(
                        ACCENT_GREEN_DARK
                );

                g2.fillRect(
                        0,
                        0,
                        w,
                        h
                );
            }

            g2.setColor(Color.WHITE);

            g2.setFont(
                    new Font(
                            "Nirmala UI",
                            Font.BOLD,
                            22
                    )
            );

            g2.drawString(
                    titleText,
                    TEXT_OFFSET_X,
                    h / 2 + 8
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // =========================================================
    // BACK BUTTON
    // =========================================================

    private static class AnimatedBackButton
            extends JButton {

        private float hoverProgress = 0f;

        private Timer animTimer;

        public AnimatedBackButton() {

            super("\u2190");

            setFont(
                    new Font(
                            "Segoe UI Emoji",
                            Font.BOLD,
                            20
                    )
            );

            setForeground(Color.WHITE);

            setFocusPainted(false);

            setContentAreaFilled(false);

            setBorderPainted(false);

            setOpaque(false);

            setPreferredSize(
                    new Dimension(42, 42)
            );

            setCursor(
                    new Cursor(
                            Cursor.HAND_CURSOR
                    )
            );

            setToolTipText(
                    LanguageManager.get(
                            "common.backtodashboard"
                    )
            );

            addMouseListener(
                    new MouseAdapter() {

                        @Override
                        public void mouseEntered(
                                MouseEvent e
                        ) {

                            animateTo(1f);
                        }

                        @Override
                        public void mouseExited(
                                MouseEvent e
                        ) {

                            animateTo(0f);
                        }
                    }
            );
        }

        private void animateTo(
                float target
        ) {

            if (animTimer != null &&
                    animTimer.isRunning()) {

                animTimer.stop();
            }

            animTimer =
                    new Timer(
                            12,
                            e -> {

                                float step =
                                        0.15f;

                                if (hoverProgress < target) {

                                    hoverProgress =
                                            Math.min(
                                                    target,
                                                    hoverProgress + step
                                            );

                                } else if (
                                        hoverProgress > target
                                ) {

                                    hoverProgress =
                                            Math.max(
                                                    target,
                                                    hoverProgress - step
                                            );
                                }

                                repaint();

                                if (hoverProgress == target) {

                                    ((Timer) e.getSource())
                                            .stop();
                                }
                            }
                    );

            animTimer.start();
        }

        @Override
        protected void paintComponent(
                Graphics g
        ) {

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int alpha =
                    (int)
                            (70 * hoverProgress);

            g2.setColor(
                    new Color(
                            255,
                            255,
                            255,
                            alpha
                    )
            );

            g2.fillOval(
                    0,
                    0,
                    getWidth(),
                    getHeight()
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // =========================================================
    // TEXT FIELD
    // =========================================================

    private static class AnimatedTextField
            extends JTextField {

        private float focusProgress = 0f;

        private Timer animTimer;

        private static final Color BASE_BORDER =
                new Color(
                        200,
                        220,
                        230
                );

        public AnimatedTextField() {

            setFont(
                    new Font(
                            "Nirmala UI",
                            Font.PLAIN,
                            13
                    )
            );

            setBorder(
                    BorderFactory.createEmptyBorder(
                            8,
                            10,
                            8,
                            10
                    )
            );

            setBackground(Color.WHITE);

            addFocusListener(
                    new FocusAdapter() {

                        @Override
                        public void focusGained(
                                FocusEvent e
                        ) {

                            animateTo(1f);
                        }

                        @Override
                        public void focusLost(
                                FocusEvent e
                        ) {

                            animateTo(0f);
                        }
                    }
            );
        }

        private void animateTo(
                float target
        ) {

            if (animTimer != null &&
                    animTimer.isRunning()) {

                animTimer.stop();
            }

            animTimer =
                    new Timer(
                            15,
                            e -> {

                                float step =
                                        0.15f;

                                if (focusProgress < target) {

                                    focusProgress =
                                            Math.min(
                                                    target,
                                                    focusProgress + step
                                            );

                                } else if (
                                        focusProgress > target
                                ) {

                                    focusProgress =
                                            Math.max(
                                                    target,
                                                    focusProgress - step
                                            );
                                }

                                repaint();

                                if (focusProgress == target) {

                                    ((Timer) e.getSource())
                                            .stop();
                                }
                            }
                    );

            animTimer.start();
        }

        @Override
        protected void paintBorder(
                Graphics g
        ) {

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int r =
                    (int)
                            (
                                    BASE_BORDER.getRed()
                                            +
                                            (
                                                    ACCENT_SKYBLUE.getRed()
                                                            -
                                                            BASE_BORDER.getRed()
                                            )
                                                    * focusProgress
                            );

            int gC =
                    (int)
                            (
                                    BASE_BORDER.getGreen()
                                            +
                                            (
                                                    ACCENT_SKYBLUE.getGreen()
                                                            -
                                                            BASE_BORDER.getGreen()
                                            )
                                                    * focusProgress
                            );

            int b =
                    (int)
                            (
                                    BASE_BORDER.getBlue()
                                            +
                                            (
                                                    ACCENT_SKYBLUE.getBlue()
                                                            -
                                                            BASE_BORDER.getBlue()
                                            )
                                                    * focusProgress
                            );

            g2.setColor(
                    new Color(
                            r,
                            gC,
                            b
                    )
            );

            g2.setStroke(
                    new BasicStroke(
                            1f
                                    +
                                    1.2f
                                            * focusProgress
                    )
            );

            g2.drawRoundRect(
                    1,
                    1,
                    getWidth() - 3,
                    getHeight() - 3,
                    8,
                    8
            );

            g2.dispose();
        }
    }

    // =========================================================
    // ACTION BUTTON
    // =========================================================

    private static class AnimatedActionButton
            extends JButton {

        private float hoverProgress = 0f;

        private final Color baseColor;

        private Timer animTimer;

        public AnimatedActionButton(
                String text,
                Color baseColor
        ) {

            super(text);

            this.baseColor = baseColor;

            setForeground(Color.WHITE);

            setFocusPainted(false);

            setContentAreaFilled(false);

            setBorderPainted(false);

            setOpaque(false);

            setCursor(
                    new Cursor(
                            Cursor.HAND_CURSOR
                    )
            );

            addMouseListener(
                    new MouseAdapter() {

                        @Override
                        public void mouseEntered(
                                MouseEvent e
                        ) {

                            animateTo(1f);
                        }

                        @Override
                        public void mouseExited(
                                MouseEvent e
                        ) {

                            animateTo(0f);
                        }
                    }
            );
        }

        private void animateTo(
                float target
        ) {

            if (animTimer != null &&
                    animTimer.isRunning()) {

                animTimer.stop();
            }

            animTimer =
                    new Timer(
                            12,
                            e -> {

                                float step =
                                        0.15f;

                                if (hoverProgress < target) {

                                    hoverProgress =
                                            Math.min(
                                                    target,
                                                    hoverProgress + step
                                            );

                                } else if (
                                        hoverProgress > target
                                ) {

                                    hoverProgress =
                                            Math.max(
                                                    target,
                                                    hoverProgress - step
                                            );
                                }

                                repaint();

                                if (hoverProgress == target) {

                                    ((Timer) e.getSource())
                                            .stop();
                                }
                            }
                    );

            animTimer.start();
        }

        @Override
        protected void paintComponent(
                Graphics g
        ) {

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int lift =
                    (int)
                            (3 * hoverProgress);

            int arc = 10;

            g2.setColor(
                    new Color(
                            0,
                            0,
                            0,
                            (int)
                                    (40 * hoverProgress)
                    )
            );

            g2.fillRoundRect(
                    2,
                    2 + lift,
                    getWidth() - 4,
                    getHeight() - 4,
                    arc,
                    arc
            );

            int r =
                    Math.min(
                            255,
                            baseColor.getRed()
                                    +
                                    (int)
                                            (
                                                    25
                                                            * hoverProgress
                                            )
                    );

            int gC =
                    Math.min(
                            255,
                            baseColor.getGreen()
                                    +
                                    (int)
                                            (
                                                    25
                                                            * hoverProgress
                                            )
                    );

            int b =
                    Math.min(
                            255,
                            baseColor.getBlue()
                                    +
                                    (int)
                                            (
                                                    25
                                                            * hoverProgress
                                            )
                    );

            g2.setColor(
                    new Color(
                            r,
                            gC,
                            b
                    )
            );

            g2.fillRoundRect(
                    0,
                    lift,
                    getWidth(),
                    getHeight() - lift - 2,
                    arc,
                    arc
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // =========================================================
    // FORM HELPERS
    // =========================================================

    private JLabel formLabel(
            String text
    ) {

        JLabel label =
                new JLabel(text);

        label.setFont(
                new Font(
                        "Nirmala UI",
                        Font.BOLD,
                        12
                )
        );

        label.setForeground(
                TEXT_MUTED
        );

        return label;
    }

    private void styleCombo(
            JComboBox<String> combo
    ) {

        combo.setFont(
                new Font(
                        "Nirmala UI",
                        Font.PLAIN,
                        13
                )
        );

        combo.setBackground(
                Color.WHITE
        );

        combo.setBorder(
                BorderFactory.createLineBorder(
                        new Color(
                                200,
                                220,
                                230
                        )
                )
        );
    }

    private AnimatedActionButton createActionButton(
            String text,
            Color bgColor
    ) {

        AnimatedActionButton btn =
                new AnimatedActionButton(
                        text,
                        bgColor
                );

        btn.setFont(
                new Font(
                        "Nirmala UI",
                        Font.BOLD,
                        13
                )
        );

        btn.setBorder(
                BorderFactory.createEmptyBorder(
                        9,
                        20,
                        9,
                        20
                )
        );

        return btn;
    }

    // =========================================================
    // LOAD PRODUCTS
    // =========================================================

    private void loadProducts() {

        productDropdown.removeAllItems();

        productList =
                productDAO.getProductsByFarmer(
                        currentFarmer.getFarmerId()
                );

        for (Product p : productList) {

            productDropdown.addItem(
                    p.getProductId()
                            + " - "
                            + p.getProductName()
                            + " ("
                            + LanguageManager.get(
                            "sales.currency"
                    )
                            + p.getPricePerUnit()
                            + "/"
                            + p.getUnit()
                            + ")"
            );
        }

        // Update total after loading products
        updateTotal();
    }

    // =========================================================
    // LOAD CUSTOMERS
    // =========================================================

    private void loadCustomers() {

        customerDropdown.removeAllItems();

        customerList =
                customerDAO.getAllCustomers();

        for (Customer c :
                customerList) {

            customerDropdown.addItem(
                    c.getCustomerId()
                            + " - "
                            + c.getCustomerName()
            );
        }
    }

    // =========================================================
    // LOAD SALES HISTORY
    // =========================================================

    private void loadSalesHistory() {

        tableModel.setRowCount(0);

        List<Sale> sales =
                salesDAO.getAllSales(
                        currentFarmer.getFarmerId()
                );

        for (Sale s : sales) {

            tableModel.addRow(
                    new Object[]{
                            s.getSaleId(),
                            s.getProductId(),
                            s.getCustomerId(),
                            s.getQuantitySold(),
                            s.getTotalAmount(),
                            s.getSaleDate()
                    }
            );
        }

        chartPanel.updateData(sales);
    }

    // =========================================================
    // RECORD SALE
    // =========================================================

    private void handleRecordSale() {

        if (productList.isEmpty() ||
                customerList.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    LanguageManager.get(
                            "sales.msg.needproductcustomer"
                    )
            );

            return;
        }

        int productIndex =
                productDropdown.getSelectedIndex();

        int customerIndex =
                customerDropdown.getSelectedIndex();

        if (productIndex < 0 ||
                customerIndex < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    LanguageManager.get(
                            "sales.msg.selectboth"
                    )
            );

            return;
        }

        try {

            int quantity =
                    Integer.parseInt(
                            quantityField
                                    .getText()
                                    .trim()
                    );

            if (quantity <= 0) {

                JOptionPane.showMessageDialog(
                        this,
                        LanguageManager.get(
                                "sales.msg.qtypositive"
                        )
                );

                return;
            }

            Product selectedProduct =
                    productList.get(
                            productIndex
                    );

            Customer selectedCustomer =
                    customerList.get(
                            customerIndex
                    );

            // ================= TOTAL =================

            double total =
                    quantity
                            *
                            selectedProduct
                                    .getPricePerUnit();

            Sale sale =
                    new Sale();

            sale.setProductId(
                    selectedProduct
                            .getProductId()
            );

            sale.setCustomerId(
                    selectedCustomer
                            .getCustomerId()
            );

            sale.setFarmerId(
                    currentFarmer
                            .getFarmerId()
            );

            sale.setQuantitySold(
                    quantity
            );

            sale.setTotalAmount(
                    total
            );

            boolean success =
                    salesDAO.recordSale(
                            sale
                    );

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        LanguageManager.get(
                                "sales.msg.saleconfirm"
                        )
                                + total
                );

                quantityField.setText("");

                totalLabel.setText(
                        LanguageManager.get(
                                "sales.total.prefix"
                        )
                                + "0.00"
                );

                loadProducts();

                loadSalesHistory();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        LanguageManager.get(
                                "sales.msg.salefail"
                        )
                );
            }

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    LanguageManager.get(
                            "sales.msg.qtynumber"
                    )
            );
        }
    }

    // =========================================================
    // ADD CUSTOMER
    // =========================================================

    private void handleAddCustomer() {

        JTextField nameField =
                new JTextField();

        JTextField contactField =
                new JTextField();

        JTextField addressField =
                new JTextField();

        JPanel panel =
                new JPanel(
                        new GridLayout(
                                3,
                                2,
                                5,
                                5
                        )
                );

        panel.add(
                new JLabel(
                        LanguageManager.get(
                                "sales.dialog.name"
                        )
                )
        );

        panel.add(nameField);

        panel.add(
                new JLabel(
                        LanguageManager.get(
                                "sales.dialog.contact"
                        )
                )
        );

        panel.add(contactField);

        panel.add(
                new JLabel(
                        LanguageManager.get(
                                "sales.dialog.address"
                        )
                )
        );

        panel.add(addressField);

        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        panel,
                        LanguageManager.get(
                                "sales.dialog.addcustomertitle"
                        ),
                        JOptionPane.OK_CANCEL_OPTION
                );

        if (result ==
                JOptionPane.OK_OPTION) {

            String name =
                    nameField
                            .getText()
                            .trim();

            if (name.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        LanguageManager.get(
                                "sales.msg.namerequired"
                        )
                );

                return;
            }

            Customer customer =
                    new Customer();

            customer.setCustomerName(
                    name
            );

            customer.setContactNumber(
                    contactField
                            .getText()
                            .trim()
            );

            customer.setAddress(
                    addressField
                            .getText()
                            .trim()
            );

            boolean success =
                    customerDAO.addCustomer(
                            customer
                    );

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        LanguageManager.get(
                                "sales.msg.customeradded"
                        )
                );

                loadCustomers();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        LanguageManager.get(
                                "sales.msg.customerfail"
                        )
                );
            }
        }
    }
}