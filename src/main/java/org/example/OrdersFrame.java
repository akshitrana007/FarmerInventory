package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class OrdersFrame extends JFrame {

    private final Farmer currentFarmer;
    private DefaultTableModel tableModel;
    private JTable ordersTable;
    private List<Order> currentOrders;
    private boolean showingPending = true;

    private final OrderDAO orderDAO = new OrderDAO();

    private static final Color PRIMARY_GREEN = new Color(76, 145, 65);
    private static final Color PRIMARY_GREEN_DARK = new Color(60, 120, 50);
    private static final Color BACKGROUND = new Color(245, 250, 240);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color ALERT_RED = new Color(200, 60, 60);
    private static final Color SKY_TINT = new Color(224, 240, 252);
    private static final Color SKY_HOVER = new Color(203, 230, 250);
    private static final Color ROW_SELECTED = new Color(198, 228, 193);
    private static final Color PURPLE = new Color(150, 90, 160);
    private static final Color SKY_BLUE = new Color(70, 130, 180);

    private static final int COL_ADDRESS = 7;

    private int hoveredRow = -1;

    public OrdersFrame(Farmer farmer) {
        this.currentFarmer = farmer;

        setTitle(LanguageManager.get("orders.title.prefix") + farmer.getFullName());
        setSize(1300, 730);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BACKGROUND);

        add(buildBanner(), BorderLayout.NORTH);

        // ---- Toggle panel (back to plain buttons, like before) ----
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        togglePanel.setBackground(CARD_BG);
        AnimatedButton viewPendingBtn = new AnimatedButton(LanguageManager.get("orders.button.viewpending"), PRIMARY_GREEN_DARK);
        AnimatedButton viewAcceptedBtn = new AnimatedButton(LanguageManager.get("orders.button.viewaccepted"), SKY_BLUE);
        togglePanel.add(viewPendingBtn);
        togglePanel.add(viewAcceptedBtn);

        // ---- Rounded elevated card behind table ----
        RoundedCardPanel centerCard = new RoundedCardPanel();
        centerCard.setLayout(new BorderLayout());
        centerCard.setBorder(new EmptyBorder(14, 20, 20, 20));
        centerCard.add(togglePanel, BorderLayout.NORTH);

        String[] columns = {
                LanguageManager.get("orders.col.orderid"),
                LanguageManager.get("orders.col.customer"),
                LanguageManager.get("orders.col.contact"),
                LanguageManager.get("orders.col.product"),
                LanguageManager.get("orders.col.qty"),
                LanguageManager.get("orders.col.price"),
                LanguageManager.get("orders.col.deliverymode"),
                LanguageManager.get("orders.col.deliveryaddress"),
                LanguageManager.get("orders.col.distance"),
                LanguageManager.get("orders.col.deliverycharge"),
                LanguageManager.get("orders.col.deliverystatus"),
                LanguageManager.get("orders.col.requestedat")
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        ordersTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (isRowSelected(row)) {
                    c.setBackground(ROW_SELECTED);
                } else if (row == hoveredRow) {
                    c.setBackground(SKY_HOVER);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : SKY_TINT);
                }
                return c;
            }
        };
        ordersTable.setRowHeight(36);
        ordersTable.setFont(new Font("Nirmala UI", Font.PLAIN, 13));
        ordersTable.setShowGrid(false);
        ordersTable.setIntercellSpacing(new Dimension(0, 0));
        ordersTable.setSelectionBackground(ROW_SELECTED);
        ordersTable.setFillsViewportHeight(true);

        ordersTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = ordersTable.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    ordersTable.repaint();
                }
            }
        });
        ordersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                ordersTable.repaint();
            }
        });

        JTableHeader header = ordersTable.getTableHeader();
        header.setFont(new Font("Nirmala UI", Font.BOLD, 13));
        header.setBackground(PRIMARY_GREEN_DARK);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 38));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < ordersTable.getColumnCount(); i++) {
            ordersTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        ordersTable.getColumnModel().getColumn(COL_ADDRESS).setCellRenderer(new TruncatedAddressRenderer());
        ordersTable.getColumnModel().getColumn(COL_ADDRESS).setPreferredWidth(140);
        ordersTable.getColumnModel().getColumn(COL_ADDRESS).setMaxWidth(160);

        ordersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = ordersTable.columnAtPoint(e.getPoint());
                int row = ordersTable.rowAtPoint(e.getPoint());
                if (col == COL_ADDRESS && row >= 0) {
                    Object fullAddress = tableModel.getValueAt(row, COL_ADDRESS);
                    if (fullAddress != null && !fullAddress.toString().equals(LanguageManager.get("orders.na"))) {
                        showFullAddressPopup(fullAddress.toString());
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(225, 235, 245)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        centerCard.add(scrollPane, BorderLayout.CENTER);

        JPanel cardWrapper = new JPanel(new BorderLayout());
        cardWrapper.setBackground(BACKGROUND);
        cardWrapper.setBorder(new EmptyBorder(16, 16, 0, 16));
        cardWrapper.add(centerCard, BorderLayout.CENTER);

        add(cardWrapper, BorderLayout.CENTER);

        // ---- Bottom panel: buttons ----
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        bottomPanel.setBackground(BACKGROUND);

        AnimatedButton acceptButton = new AnimatedButton(LanguageManager.get("orders.button.accept"), PRIMARY_GREEN);
        AnimatedButton rejectButton = new AnimatedButton(LanguageManager.get("orders.button.reject"), ALERT_RED);
        AnimatedButton updateDeliveryButton = new AnimatedButton(LanguageManager.get("orders.button.updatedelivery"), PURPLE);
        AnimatedButton refreshButton = new AnimatedButton(LanguageManager.get("orders.button.refresh"), new Color(120, 120, 120));

        bottomPanel.add(acceptButton);
        bottomPanel.add(rejectButton);
        bottomPanel.add(updateDeliveryButton);
        bottomPanel.add(refreshButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadPendingOrders();

        viewPendingBtn.addActionListener(e -> loadPendingOrders());
        viewAcceptedBtn.addActionListener(e -> loadAcceptedOrders());
        acceptButton.addActionListener(e -> handleAccept());
        rejectButton.addActionListener(e -> handleReject());
        updateDeliveryButton.addActionListener(e -> handleUpdateDeliveryStatus());
        refreshButton.addActionListener(e -> {
            if (showingPending) loadPendingOrders(); else loadAcceptedOrders();
        });

        setVisible(true);
    }

    private void showFullAddressPopup(String address) {
        JTextArea area = new JTextArea(address);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setFont(new Font("Nirmala UI", Font.PLAIN, 14));
        area.setBackground(CARD_BG);
        area.setBorder(new EmptyBorder(10, 10, 10, 10));
        area.setPreferredSize(new Dimension(320, 100));

        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        JOptionPane.showMessageDialog(
                this,
                sp,
                LanguageManager.get("orders.col.deliveryaddress"),
                JOptionPane.PLAIN_MESSAGE
        );
    }

    // ================= Rounded elevated card =================
    private static class RoundedCardPanel extends JPanel {
        RoundedCardPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            for (int i = 6; i > 0; i--) {
                g2.setColor(new Color(0, 0, 0, 4));
                g2.fillRoundRect(i, i + 2, w - i * 2, h - i * 2, 20, 20);
            }

            g2.setColor(CARD_BG);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 18, 18);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ================= Truncated address cell renderer =================
    private class TruncatedAddressRenderer extends DefaultTableCellRenderer {
        TruncatedAddressRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value != null) {
                String full = value.toString();
                boolean clickable = !full.equals(LanguageManager.get("orders.na"));
                String display = full;
                int maxChars = 14;
                if (full.length() > maxChars) {
                    display = full.substring(0, maxChars - 1) + "…";
                }
                setText(display);
                setToolTipText(clickable ? full : null);
                if (clickable) {
                    setForeground(isSelected ? Color.BLACK : new Color(40, 100, 160));
                    setFont(getFont().deriveFont(Font.BOLD));
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    setForeground(isSelected ? Color.BLACK : new Color(120, 120, 120));
                    setFont(getFont().deriveFont(Font.PLAIN));
                    setCursor(Cursor.getDefaultCursor());
                }
            }
            return c;
        }
    }

    // ================= Photo hero banner =================
    private JPanel buildBanner() {
        PhotoBannerPanel banner = new PhotoBannerPanel("/images/orders_banner.jpg", LanguageManager.get("orders.heading"));
        banner.setPreferredSize(new Dimension(1300, 130));
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
                g2.drawString("(orders_banner.jpg not found in resources/images)", 70, h - 12);
            }

            GradientPaint overlay = new GradientPaint(0, 0, new Color(0, 0, 0, 160), w, 0, new Color(0, 0, 0, 60));
            g2.setPaint(overlay);
            g2.fillRect(0, 0, w, h);

            g2.setFont(new Font("Nirmala UI", Font.BOLD, 24));
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
            setFont(new Font("Nirmala UI", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 22, 14, 22));

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

    // ================= Original logic (unchanged) =================
    private void loadPendingOrders() {
        showingPending = true;
        currentOrders = orderDAO.getPendingOrdersForFarmer(currentFarmer.getFarmerId());
        populateTable();

        if (currentOrders.isEmpty()) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("orders.msg.nopending"));
        }
    }

    private void loadAcceptedOrders() {
        showingPending = false;
        currentOrders = orderDAO.getAcceptedOrdersForFarmer(currentFarmer.getFarmerId());
        populateTable();

        if (currentOrders.isEmpty()) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("orders.msg.noaccepted"));
        }
    }

    private void populateTable() {
        tableModel.setRowCount(0);

        for (Order o : currentOrders) {
            boolean isDelivery = "DELIVERY".equalsIgnoreCase(o.getDeliveryMode());

            String modeDisplay = isDelivery
                    ? LanguageManager.get("custdash.delivery.delivery")
                    : LanguageManager.get("custdash.delivery.pickup");

            String addressDisplay = isDelivery && o.getDeliveryAddress() != null
                    ? o.getDeliveryAddress() : LanguageManager.get("orders.na");

            String distanceDisplay = isDelivery
                    ? String.format("%.1f", o.getDistanceKm()) : LanguageManager.get("orders.na");

            String chargeDisplay = isDelivery
                    ? "₹" + String.format("%.2f", o.getDeliveryCharge()) : LanguageManager.get("orders.na");

            String deliveryStatusDisplay = deliveryStatusLabel(o.getDeliveryStatus());

            tableModel.addRow(new Object[]{
                    o.getOrderId(),
                    o.getCustomerName(),
                    o.getCustomerContact(),
                    o.getProductName(),
                    o.getQuantity(),
                    o.getPricePerUnit(),
                    modeDisplay,
                    addressDisplay,
                    distanceDisplay,
                    chargeDisplay,
                    deliveryStatusDisplay,
                    o.getRequestedAt()
            });
        }
    }

    private String deliveryStatusLabel(String rawStatus) {
        if (rawStatus == null) return LanguageManager.get("orders.deliverystatus.pending");
        switch (rawStatus) {
            case "OUT_FOR_DELIVERY": return LanguageManager.get("orders.deliverystatus.outfordelivery");
            case "READY_FOR_PICKUP": return LanguageManager.get("orders.deliverystatus.readyforpickup");
            case "COMPLETED": return LanguageManager.get("orders.deliverystatus.completed");
            default: return LanguageManager.get("orders.deliverystatus.pending");
        }
    }

    private void handleAccept() {
        if (!showingPending) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("orders.msg.pendingonly"));
            return;
        }

        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("orders.msg.selectfirst"));
            return;
        }

        Order selectedOrder = currentOrders.get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this,
                LanguageManager.get("orders.msg.acceptconfirm") + selectedOrder.getQuantity() + " " + selectedOrder.getProductName() +
                        LanguageManager.get("orders.msg.confirmfrom") + selectedOrder.getCustomerName() + "?",
                LanguageManager.get("orders.msg.confirmaccepttitle"), JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean success = orderDAO.acceptOrder(selectedOrder.getOrderId());

        if (success) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("orders.msg.acceptsuccess"));
        } else {
            JOptionPane.showMessageDialog(this, LanguageManager.get("orders.msg.acceptfail"));
        }

        loadPendingOrders();
    }

    private void handleReject() {
        if (!showingPending) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("orders.msg.pendingonly"));
            return;
        }

        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("orders.msg.selectfirst"));
            return;
        }

        Order selectedOrder = currentOrders.get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this,
                LanguageManager.get("orders.msg.rejectconfirm") + selectedOrder.getQuantity() + " " + selectedOrder.getProductName() +
                        LanguageManager.get("orders.msg.confirmfrom") + selectedOrder.getCustomerName() + "?",
                LanguageManager.get("orders.msg.confirmrejecttitle"), JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean success = orderDAO.rejectOrder(selectedOrder.getOrderId());

        if (success) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("orders.msg.rejectsuccess"));
        } else {
            JOptionPane.showMessageDialog(this, LanguageManager.get("orders.msg.rejectfail"));
        }

        loadPendingOrders();
    }

    private void handleUpdateDeliveryStatus() {
        if (showingPending) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("orders.msg.acceptedonly"));
            return;
        }

        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("orders.msg.selectfirst"));
            return;
        }

        Order selectedOrder = currentOrders.get(selectedRow);
        boolean isDelivery = "DELIVERY".equalsIgnoreCase(selectedOrder.getDeliveryMode());

        String[] options;
        String[] optionCodes;

        if (isDelivery) {
            options = new String[]{
                    LanguageManager.get("orders.deliverystatus.outfordelivery"),
                    LanguageManager.get("orders.deliverystatus.completed")
            };
            optionCodes = new String[]{"OUT_FOR_DELIVERY", "COMPLETED"};
        } else {
            options = new String[]{
                    LanguageManager.get("orders.deliverystatus.readyforpickup"),
                    LanguageManager.get("orders.deliverystatus.completed")
            };
            optionCodes = new String[]{"READY_FOR_PICKUP", "COMPLETED"};
        }

        int choice = JOptionPane.showOptionDialog(
                this,
                LanguageManager.get("orders.msg.choosestatus"),
                LanguageManager.get("orders.button.updatedelivery"),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == JOptionPane.CLOSED_OPTION) {
            return;
        }

        boolean success = orderDAO.updateDeliveryStatus(selectedOrder.getOrderId(), optionCodes[choice]);

        if (success) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("orders.msg.deliveryupdated"));
        } else {
            JOptionPane.showMessageDialog(this, LanguageManager.get("orders.msg.deliveryupdatefail"));
        }

        loadAcceptedOrders();
    }
}