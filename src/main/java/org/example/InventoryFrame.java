package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class InventoryFrame extends JFrame {

    private Farmer currentFarmer;
    private JTable productTable;
    private DefaultTableModel tableModel;

    private AnimatedTextField nameField, categoryField, stockField, unitField, priceField, thresholdField, searchField;
    private JLabel mandiPriceLabel;

    private ProductDAO productDAO = new ProductDAO();
    private MandiPriceDAO mandiPriceDAO = new MandiPriceDAO();
    private MandiPriceService mandiPriceService = new MandiPriceService();

    private static final Color PAGE_BG = new Color(224, 242, 250);
    private static final Color CARD_BG = new Color(233, 247, 252);
    private static final Color ROW_ALT_BG = new Color(220, 240, 248);
    private static final Color ACCENT_GREEN = new Color(67, 160, 71);
    private static final Color ACCENT_GREEN_DARK = new Color(50, 130, 55);
    private static final Color ACCENT_SKYBLUE = new Color(41, 172, 223);
    private static final Color ACCENT_BROWN = new Color(160, 110, 68);
    private static final Color WARNING_RED = new Color(200, 50, 50);
    private static final Color CAUTION_ORANGE = new Color(210, 140, 20);
    private static final Color TEXT_MUTED = new Color(90, 90, 90);

    public InventoryFrame(Farmer farmer) {
        this.currentFarmer = farmer;

        setTitle(LanguageManager.get("inventory.title"));
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(PAGE_BG);
        setLayout(new BorderLayout(10, 10));

        HeroBannerPanel heroBanner = new HeroBannerPanel("/images/inventory_banner.jpg",
                LanguageManager.get("inventory.title"),
                () -> {
                    dispose();
                    new DashboardFrame(currentFarmer);
                });
        heroBanner.setPreferredSize(new Dimension(10, 130));
        add(heroBanner, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBackground(PAGE_BG);
        mainContent.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        add(mainContent, BorderLayout.CENTER);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        searchPanel.setBackground(CARD_BG);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 225, 235)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JLabel searchLbl = new JLabel(LanguageManager.get("inventory.label.search"));
        searchLbl.setFont(new Font("Nirmala UI", Font.PLAIN, 13));
        searchField = new AnimatedTextField();
        searchField.setColumns(18);

        AnimatedActionButton searchBtn = createSmallButton(LanguageManager.get("inventory.button.search"), ACCENT_SKYBLUE);
        AnimatedActionButton showAllBtn = createSmallButton(LanguageManager.get("inventory.button.showall"), ACCENT_GREEN);

        searchPanel.add(searchLbl);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(showAllBtn);
        mainContent.add(searchPanel, BorderLayout.NORTH);

        String[] columns = {
                LanguageManager.get("inventory.col.id"),
                LanguageManager.get("inventory.col.name"),
                LanguageManager.get("inventory.col.category"),
                LanguageManager.get("inventory.col.stock"),
                LanguageManager.get("inventory.col.unit"),
                LanguageManager.get("inventory.col.price"),
                LanguageManager.get("inventory.col.threshold")
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(tableModel);
        productTable.setRowHeight(30);
        productTable.setFont(new Font("Nirmala UI", Font.PLAIN, 13));
        productTable.setSelectionBackground(new Color(190, 225, 245));
        productTable.setSelectionForeground(Color.BLACK);
        productTable.setGridColor(new Color(225, 238, 245));
        productTable.setShowGrid(false);
        productTable.setIntercellSpacing(new Dimension(0, 0));

        // ---- Zebra-striped rows: alternating white / light sky-blue tint ----
        DefaultTableCellRenderer zebraRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT_BG);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        };
        for (int i = 0; i < productTable.getColumnCount(); i++) {
            productTable.getColumnModel().getColumn(i).setCellRenderer(zebraRenderer);
        }

        JTableHeader header = productTable.getTableHeader();
        header.setFont(new Font("Nirmala UI", Font.BOLD, 13));
        header.setBackground(ACCENT_GREEN);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 34));

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 225, 235)));
        mainContent.add(scrollPane, BorderLayout.CENTER);

        JPanel southContainer = new JPanel(new BorderLayout(0, 10));
        southContainer.setBackground(PAGE_BG);
        southContainer.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel formPanel = new JPanel(new GridLayout(2, 7, 8, 8));
        formPanel.setBackground(CARD_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 225, 235)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        nameField = new AnimatedTextField();
        categoryField = new AnimatedTextField();
        stockField = new AnimatedTextField();
        unitField = new AnimatedTextField();
        priceField = new AnimatedTextField();
        thresholdField = new AnimatedTextField();

        formPanel.add(formLabel(LanguageManager.get("inventory.label.name")));
        formPanel.add(formLabel(LanguageManager.get("inventory.label.category")));
        formPanel.add(formLabel(LanguageManager.get("inventory.label.stock")));
        formPanel.add(formLabel(LanguageManager.get("inventory.label.unit")));
        formPanel.add(formLabel(LanguageManager.get("inventory.label.price")));
        formPanel.add(formLabel(LanguageManager.get("inventory.label.threshold")));
        formPanel.add(new JLabel(""));

        formPanel.add(nameField);
        formPanel.add(categoryField);
        formPanel.add(stockField);
        formPanel.add(unitField);
        formPanel.add(priceField);
        formPanel.add(thresholdField);

        JPanel mandiPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        mandiPanel.setBackground(CARD_BG);
        mandiPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 225, 235)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        AnimatedActionButton checkMandiBtn = createSmallButton(LanguageManager.get("inventory.button.checkmandi"), ACCENT_BROWN);
        mandiPriceLabel = new JLabel(LanguageManager.get("inventory.mandi.prefix") + LanguageManager.get("inventory.mandi.placeholder"));
        mandiPriceLabel.setFont(new Font("Nirmala UI", Font.BOLD, 13));
        mandiPriceLabel.setForeground(TEXT_MUTED);

        mandiPanel.add(checkMandiBtn);
        mandiPanel.add(mandiPriceLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        buttonPanel.setBackground(PAGE_BG);
        AnimatedActionButton addBtn = createActionButton(LanguageManager.get("inventory.button.add"), ACCENT_GREEN);
        AnimatedActionButton updateBtn = createActionButton(LanguageManager.get("inventory.button.update"), ACCENT_SKYBLUE);
        AnimatedActionButton deleteBtn = createActionButton(LanguageManager.get("inventory.button.delete"), WARNING_RED);
        AnimatedActionButton clearBtn = createActionButton(LanguageManager.get("inventory.button.clear"), ACCENT_BROWN);
        buttonPanel.add(addBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(clearBtn);

        JPanel formAndMandi = new JPanel(new BorderLayout(0, 8));
        formAndMandi.setBackground(PAGE_BG);
        formAndMandi.add(formPanel, BorderLayout.NORTH);
        formAndMandi.add(mandiPanel, BorderLayout.CENTER);

        southContainer.add(formAndMandi, BorderLayout.CENTER);
        southContainer.add(buttonPanel, BorderLayout.SOUTH);
        mainContent.add(southContainer, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> handleAddProduct());
        updateBtn.addActionListener(e -> handleUpdateProduct());
        deleteBtn.addActionListener(e -> handleDeleteProduct());
        clearBtn.addActionListener(e -> clearForm());
        searchBtn.addActionListener(e -> handleSearch());
        showAllBtn.addActionListener(e -> loadAllProducts());
        checkMandiBtn.addActionListener(e -> handleCheckMandiPrice());

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

    /**
     * Photo banner with a dark gradient overlay for readable white title
     * text, plus a back arrow (top-left) that returns to the Dashboard.
     * If the image file isn't found yet, falls back to a plain green bar.
     */
    private static class HeroBannerPanel extends JPanel {
        private Image bannerImage;
        private final String titleText;
        private static final int TEXT_OFFSET_X = 70; // room for the back arrow

        public HeroBannerPanel(String resourcePath, String titleText, Runnable onBack) {
            this.titleText = titleText;
            setOpaque(false);
            setLayout(new BorderLayout());

            java.net.URL imgUrl = getClass().getResource(resourcePath);
            if (imgUrl != null) {
                bannerImage = new ImageIcon(imgUrl).getImage();
            }

            AnimatedBackButton backBtn = new AnimatedBackButton();
            backBtn.addActionListener(e -> onBack.run());
            JPanel backWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
            backWrap.setOpaque(false);
            backWrap.add(backBtn);
            add(backWrap, BorderLayout.WEST);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            if (bannerImage != null) {
                int imgW = bannerImage.getWidth(this);
                int imgH = bannerImage.getHeight(this);
                if (imgW > 0 && imgH > 0) {
                    double panelRatio = (double) w / h;
                    double imgRatio = (double) imgW / imgH;
                    int drawW, drawH, drawX, drawY;
                    if (imgRatio > panelRatio) {
                        drawH = h;
                        drawW = (int) (h * imgRatio);
                        drawX = (w - drawW) / 2;
                        drawY = 0;
                    } else {
                        drawW = w;
                        drawH = (int) (w / imgRatio);
                        drawX = 0;
                        drawY = (h - drawH) / 2;
                    }
                    g2.drawImage(bannerImage, drawX, drawY, drawW, drawH, this);
                }
                GradientPaint overlay = new GradientPaint(0, 0, new Color(20, 40, 20, 150), 0, h, new Color(20, 40, 20, 60));
                g2.setPaint(overlay);
                g2.fillRect(0, 0, w, h);
            } else {
                g2.setColor(ACCENT_GREEN_DARK);
                g2.fillRect(0, 0, w, h);
            }

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Nirmala UI", Font.BOLD, 22));
            g2.drawString(titleText, TEXT_OFFSET_X, h / 2 + 8);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Circular "back to Dashboard" arrow button. Transparent by default,
     * fills in with a soft white glow on hover so it's clearly clickable
     * over any photo.
     */
    private static class AnimatedBackButton extends JButton {
        private float hoverProgress = 0f;
        private Timer animTimer;

        public AnimatedBackButton() {
            super("\u2190"); // ←
            setFont(new Font("Segoe UI Emoji", Font.BOLD, 20));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setPreferredSize(new Dimension(42, 42));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText(LanguageManager.get("common.backtodashboard"));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    animateTo(1f);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    animateTo(0f);
                }
            });
        }

        private void animateTo(float target) {
            if (animTimer != null && animTimer.isRunning()) {
                animTimer.stop();
            }
            animTimer = new Timer(12, e -> {
                float step = 0.15f;
                if (hoverProgress < target) {
                    hoverProgress = Math.min(target, hoverProgress + step);
                } else if (hoverProgress > target) {
                    hoverProgress = Math.max(target, hoverProgress - step);
                }
                repaint();
                if (hoverProgress == target) {
                    ((Timer) e.getSource()).stop();
                }
            });
            animTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int alpha = (int) (70 * hoverProgress);
            g2.setColor(new Color(255, 255, 255, alpha));
            g2.fillOval(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Text field whose border animates from a neutral gray to a glowing
     * sky-blue, and thickens slightly, when the field gains focus — gives
     * clear, modern feedback about which field you're typing into.
     */
    private static class AnimatedTextField extends JTextField {
        private float focusProgress = 0f;
        private Timer animTimer;
        private static final Color BASE_BORDER = new Color(200, 220, 230);

        public AnimatedTextField() {
            setFont(new Font("Nirmala UI", Font.PLAIN, 13));
            setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            setBackground(Color.WHITE);

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    animateTo(1f);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    animateTo(0f);
                }
            });
        }

        private void animateTo(float target) {
            if (animTimer != null && animTimer.isRunning()) {
                animTimer.stop();
            }
            animTimer = new Timer(15, e -> {
                float step = 0.15f;
                if (focusProgress < target) {
                    focusProgress = Math.min(target, focusProgress + step);
                } else if (focusProgress > target) {
                    focusProgress = Math.max(target, focusProgress - step);
                }
                repaint();
                if (focusProgress == target) {
                    ((Timer) e.getSource()).stop();
                }
            });
            animTimer.start();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int r = (int) (BASE_BORDER.getRed() + (ACCENT_SKYBLUE.getRed() - BASE_BORDER.getRed()) * focusProgress);
            int gC = (int) (BASE_BORDER.getGreen() + (ACCENT_SKYBLUE.getGreen() - BASE_BORDER.getGreen()) * focusProgress);
            int b = (int) (BASE_BORDER.getBlue() + (ACCENT_SKYBLUE.getBlue() - BASE_BORDER.getBlue()) * focusProgress);

            g2.setColor(new Color(r, gC, b));
            g2.setStroke(new BasicStroke(1f + 1.2f * focusProgress));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);

            g2.dispose();
        }
    }

    /**
     * A button that smoothly brightens and lifts slightly on hover.
     */
    private static class AnimatedActionButton extends JButton {
        private float hoverProgress = 0f;
        private final Color baseColor;
        private Timer animTimer;

        public AnimatedActionButton(String text, Color baseColor) {
            super(text);
            this.baseColor = baseColor;
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    animateTo(1f);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    animateTo(0f);
                }
            });
        }

        private void animateTo(float target) {
            if (animTimer != null && animTimer.isRunning()) {
                animTimer.stop();
            }
            animTimer = new Timer(12, e -> {
                float step = 0.15f;
                if (hoverProgress < target) {
                    hoverProgress = Math.min(target, hoverProgress + step);
                } else if (hoverProgress > target) {
                    hoverProgress = Math.max(target, hoverProgress - step);
                }
                repaint();
                if (hoverProgress == target) {
                    ((Timer) e.getSource()).stop();
                }
            });
            animTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int lift = (int) (3 * hoverProgress);
            int arc = 10;

            g2.setColor(new Color(0, 0, 0, (int) (40 * hoverProgress)));
            g2.fillRoundRect(2, 2 + lift, getWidth() - 4, getHeight() - 4, arc, arc);

            int r = Math.min(255, baseColor.getRed() + (int) (25 * hoverProgress));
            int gC = Math.min(255, baseColor.getGreen() + (int) (25 * hoverProgress));
            int b = Math.min(255, baseColor.getBlue() + (int) (25 * hoverProgress));
            g2.setColor(new Color(r, gC, b));
            g2.fillRoundRect(0, lift, getWidth(), getHeight() - lift - 2, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private JLabel formLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Nirmala UI", Font.BOLD, 12));
        label.setForeground(TEXT_MUTED);
        return label;
    }

    private AnimatedActionButton createSmallButton(String text, Color bgColor) {
        AnimatedActionButton btn = new AnimatedActionButton(text, bgColor);
        btn.setFont(new Font("Nirmala UI", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return btn;
    }

    private AnimatedActionButton createActionButton(String text, Color bgColor) {
        AnimatedActionButton btn = new AnimatedActionButton(text, bgColor);
        btn.setFont(new Font("Nirmala UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
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

    private void handleCheckMandiPrice() {
        String cropName = nameField.getText().trim();
        String state = currentFarmer.getState();

        if (cropName.isEmpty()) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("inventory.msg.entername"));
            return;
        }

        if (state == null || state.trim().isEmpty()) {
            mandiPriceLabel.setForeground(WARNING_RED);
            mandiPriceLabel.setText(LanguageManager.get("inventory.mandi.prefix") + LanguageManager.get("inventory.mandi.nostate"));
            return;
        }

        Double mandiPrice = mandiPriceDAO.getCachedMandiPrice(cropName, state);

        if (mandiPrice == null) {
            mandiPrice = mandiPriceService.fetchAndCacheMandiPrice(cropName, state);
        }

        if (mandiPrice == null) {
            mandiPriceLabel.setForeground(TEXT_MUTED);
            mandiPriceLabel.setText(LanguageManager.get("inventory.mandi.prefix") + LanguageManager.get("inventory.mandi.notavailable") + cropName + LanguageManager.get("inventory.mandi.in") + state);
            return;
        }

        String enteredPriceText = priceField.getText().trim();
        if (enteredPriceText.isEmpty()) {
            mandiPriceLabel.setForeground(ACCENT_GREEN_DARK);
            mandiPriceLabel.setText(LanguageManager.get("inventory.mandi.prefix") + "\u20B9" + mandiPrice + LanguageManager.get("inventory.mandi.perkg"));
            return;
        }

        try {
            double enteredPrice = Double.parseDouble(enteredPriceText);
            double diff = Math.abs(enteredPrice - mandiPrice);

            if (diff <= 2) {
                mandiPriceLabel.setForeground(ACCENT_GREEN_DARK);
                mandiPriceLabel.setText(LanguageManager.get("inventory.mandi.prefix") + "\u20B9" + mandiPrice + LanguageManager.get("inventory.mandi.perkg") + LanguageManager.get("inventory.mandi.fine"));
            } else if (diff < 5) {
                mandiPriceLabel.setForeground(CAUTION_ORANGE);
                mandiPriceLabel.setText(LanguageManager.get("inventory.mandi.prefix") + "\u20B9" + mandiPrice + LanguageManager.get("inventory.mandi.perkg") + LanguageManager.get("inventory.mandi.caution"));
            } else {
                mandiPriceLabel.setForeground(WARNING_RED);
                mandiPriceLabel.setText(LanguageManager.get("inventory.mandi.prefix") + "\u20B9" + mandiPrice + LanguageManager.get("inventory.mandi.perkg") + LanguageManager.get("inventory.mandi.warning"));
            }

        } catch (NumberFormatException ex) {
            mandiPriceLabel.setForeground(ACCENT_GREEN_DARK);
            mandiPriceLabel.setText(LanguageManager.get("inventory.mandi.prefix") + "\u20B9" + mandiPrice + LanguageManager.get("inventory.mandi.perkg"));
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
                JOptionPane.showMessageDialog(this, LanguageManager.get("inventory.msg.namerequired"));
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
                JOptionPane.showMessageDialog(this, LanguageManager.get("inventory.msg.addsuccess"));
                clearForm();
                loadAllProducts();
            } else {
                JOptionPane.showMessageDialog(this, LanguageManager.get("inventory.msg.addfail"));
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("inventory.msg.numbererror"));
        }
    }

    private void handleUpdateProduct() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("inventory.msg.selectfirst"));
            return;
        }

        try {
            int productId = (int) tableModel.getValueAt(row, 0);

            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            int stock = Integer.parseInt(stockField.getText().trim());
            String unit = unitField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());
            int threshold = Integer.parseInt(thresholdField.getText().trim());

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, LanguageManager.get("inventory.msg.nameempty"));
                return;
            }

            boolean success = productDAO.updateProduct(productId, name, category, stock,
                    unit.isEmpty() ? "kg" : unit, price, threshold);

            if (success) {
                JOptionPane.showMessageDialog(this, LanguageManager.get("inventory.msg.updatesuccess"));
                clearForm();
                loadAllProducts();
            } else {
                JOptionPane.showMessageDialog(this, LanguageManager.get("inventory.msg.updatefail"));
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("inventory.msg.numbererror"));
        }
    }

    private void handleDeleteProduct() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("inventory.msg.selectfirst"));
            return;
        }

        int productId = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, LanguageManager.get("inventory.msg.deleteconfirm"), LanguageManager.get("inventory.msg.confirmtitle"), JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = productDAO.deleteProduct(productId);
            if (success) {
                JOptionPane.showMessageDialog(this, LanguageManager.get("inventory.msg.deletesuccess"));
                clearForm();
                loadAllProducts();
            } else {
                JOptionPane.showMessageDialog(this, LanguageManager.get("inventory.msg.deletefail"));
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
        mandiPriceLabel.setForeground(TEXT_MUTED);
        mandiPriceLabel.setText(LanguageManager.get("inventory.mandi.prefix") + LanguageManager.get("inventory.mandi.placeholder"));
    }
}