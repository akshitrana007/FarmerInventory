package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class DashboardFrame extends JFrame {

    private Farmer currentFarmer;

    private JLabel totalProductsValue;
    private JLabel totalStockValue;
    private JLabel totalSalesValue;
    private JLabel totalRevenueValue;
    private JPanel alertContentPanel;
    private AnimatedSidebarButton chatBtn;

    private ProductDAO productDAO = new ProductDAO();
    private SalesDAO salesDAO = new SalesDAO();
    private MessageDAO messageDAO = new MessageDAO();

    private volatile boolean keepMonitoring = true;

    // ---- Light theme, green / sky blue / brown as the major colors ----
    private static final Color PAGE_BG = new Color(224, 242, 250); // light sky-blue
    private static final Color CARD_BG = new Color(233, 247, 252);      // soft sky-blue tint instead of pure white
    private static final Color CARD_BG_HOVER = new Color(216, 240, 249); // slightly deeper on hover
    private static final Color ACCENT_GREEN = new Color(67, 160, 71);
    private static final Color ACCENT_SKYBLUE = new Color(41, 172, 223);
    private static final Color ACCENT_BROWN = new Color(160, 110, 68);
    private static final Color TEXT_DARK = new Color(38, 45, 38);
    private static final Color TEXT_MUTED = new Color(120, 128, 118);
    private static final Color TEXT_LIGHT = Color.WHITE;
    private static final Color ALERT_RED = new Color(200, 60, 60);

    private static final Font ICON_FONT = new Font("Segoe UI Emoji", Font.PLAIN, 18);
    private static final Font ICON_FONT_LARGE = new Font("Segoe UI Emoji", Font.PLAIN, 28);

    public DashboardFrame(Farmer farmer) {
        this.currentFarmer = farmer;

        setTitle(LanguageManager.get("dashboard.window.title") + farmer.getFullName());
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(PAGE_BG);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(buildMainContent(), BorderLayout.CENTER);

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

    // ================= SIDEBAR =================

    /**
     * Sidebar panel that paints a diagonal green gradient instead of a
     * flat solid color. IMPORTANT: super.paintComponent(g) must never be
     * called AFTER we draw the gradient — for an opaque JPanel that call
     * repaints the whole area with the default background color, which
     * was silently wiping out the gradient before. We simply never call
     * it here since we already fill the entire area ourselves.
     */
    private static class GradientSidebarPanel extends JPanel {
        public GradientSidebarPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(46, 92, 40),       // top-left: fresh green
                    w, h, new Color(20, 58, 52)         // bottom-right: deep teal-green
            );
            g2.setPaint(gradient);
            g2.fillRect(0, 0, w, h);

            g2.dispose();
            // NOTE: no super.paintComponent(g) call here — see comment above.
        }
    }

    private JPanel buildSidebar() {
        GradientSidebarPanel sidebar = new GradientSidebarPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(235, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        brandPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel logoLabel = new JLabel("\uD83C\uDF31");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brandLabel = new JLabel(LanguageManager.get("dashboard.brand.name"));
        brandLabel.setFont(new Font("Nirmala UI", Font.BOLD, 16));
        brandLabel.setForeground(TEXT_LIGHT);
        brandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        brandPanel.add(logoLabel);
        brandPanel.add(brandLabel);
        sidebar.add(brandPanel);

        sidebar.add(Box.createVerticalStrut(6));

        sidebar.add(createSidebarButton("\uD83D\uDCE6", LanguageManager.get("dashboard.nav.inventory"), ACCENT_GREEN,
                e -> new InventoryFrame(currentFarmer)));
        sidebar.add(createSidebarButton("\uD83E\uDDFE", LanguageManager.get("dashboard.nav.sales"), ACCENT_SKYBLUE,
                e -> new SalesFrame(currentFarmer)));
        sidebar.add(createSidebarButton("\uD83D\uDCCA", LanguageManager.get("dashboard.nav.reports"), ACCENT_BROWN,
                e -> new ReportsFrame(currentFarmer)));
        sidebar.add(createSidebarButton("\uD83D\uDCCB", LanguageManager.get("dashboard.nav.orders"), ACCENT_GREEN,
                e -> new OrdersFrame(currentFarmer)));
        sidebar.add(createSidebarButton("\uD83D\uDCB9", LanguageManager.get("dashboard.nav.mandi"), ACCENT_SKYBLUE,
                e -> new MandiPriceFrame(currentFarmer)));
        sidebar.add(createSidebarButton("\u2B50", LanguageManager.get("dashboard.nav.feedback"), ACCENT_BROWN,
                e -> new FarmerFeedbackFrame(currentFarmer)));

        chatBtn = createSidebarButton("\uD83D\uDCAC", LanguageManager.get("dashboard.nav.chat"), ACCENT_GREEN,
                e -> new BrowseCustomersFrame(currentFarmer));
        sidebar.add(chatBtn);

        sidebar.add(Box.createVerticalGlue());

        JSeparator sep2 = new JSeparator();
        sep2.setForeground(new Color(60, 100, 52));
        sep2.setMaximumSize(new Dimension(235, 1));
        sidebar.add(sep2);
        sidebar.add(Box.createVerticalStrut(6));

        sidebar.add(createSidebarButton("\uD83D\uDEAA", LanguageManager.get("dashboard.nav.logout"), ALERT_RED,
                e -> {
                    keepMonitoring = false;
                    dispose();
                    new RoleSelectionFrame();
                }));

        return sidebar;
    }

    private static class AnimatedSidebarButton extends JButton {
        private float hoverProgress = 0f;
        private final Color accentColor;
        private Timer animTimer;

        public AnimatedSidebarButton(Color accentColor) {
            this.accentColor = accentColor;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
        }

        private void animateTo(float target) {
            if (animTimer != null && animTimer.isRunning()) {
                animTimer.stop();
            }
            animTimer = new Timer(15, e -> {
                float step = 0.12f;
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

            int barWidth = (int) (4 * hoverProgress);
            int bgAlpha = (int) (255 * hoverProgress);

            g2.setColor(new Color(52, 96, 44, bgAlpha));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(accentColor);
            g2.fillRect(0, 0, barWidth, getHeight());

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private AnimatedSidebarButton createSidebarButton(String icon, String text, Color accentColor,
                                                      java.awt.event.ActionListener action) {
        AnimatedSidebarButton button = new AnimatedSidebarButton(accentColor);
        button.setLayout(new BorderLayout());
        button.setMaximumSize(new Dimension(235, 46));
        button.setPreferredSize(new Dimension(235, 46));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 10));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(ICON_FONT);

        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Nirmala UI", Font.PLAIN, 14));
        textLabel.setForeground(TEXT_LIGHT);
        textLabel.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));
        textLabel.setName("sidebarLabel");

        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.setOpaque(false);
        innerPanel.add(iconLabel, BorderLayout.WEST);
        innerPanel.add(textLabel, BorderLayout.CENTER);

        button.add(innerPanel, BorderLayout.CENTER);
        button.addActionListener(action);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.animateTo(1f);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.animateTo(0f);
            }
        });

        return button;
    }

    // ================= MAIN CONTENT =================

    private JPanel buildMainContent() {
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(PAGE_BG);

        mainContent.add(buildTopBar(), BorderLayout.NORTH);
        mainContent.add(buildBodyPanel(), BorderLayout.CENTER);

        return mainContent;
    }

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(PAGE_BG);
        topBar.setBorder(BorderFactory.createEmptyBorder(28, 32, 10, 32));

        JLabel welcomeLabel = new JLabel(LanguageManager.get("dashboard.welcome") + currentFarmer.getFullName());
        welcomeLabel.setFont(new Font("Nirmala UI", Font.BOLD, 22));
        welcomeLabel.setForeground(TEXT_DARK);

        JLabel locationLabel = new JLabel("\uD83D\uDCCD " + currentFarmer.getFarmLocation());
        locationLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        locationLabel.setForeground(TEXT_MUTED);
        locationLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(welcomeLabel);
        textPanel.add(locationLabel);

        topBar.add(textPanel, BorderLayout.WEST);
        return topBar;
    }

    private JPanel buildBodyPanel() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(PAGE_BG);
        body.setBorder(BorderFactory.createEmptyBorder(20, 32, 30, 32));

        // ---- Hero banner photo, now at the TOP of the dashboard ----
        RealImageBannerPanel farmBanner = new RealImageBannerPanel();
        farmBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
        farmBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        farmBanner.setPreferredSize(new Dimension(10, 220));
        body.add(farmBanner);
        body.add(Box.createVerticalStrut(20));

        // ---- Stat cards ----
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setOpaque(false);
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        totalProductsValue = new JLabel("0");
        totalStockValue = new JLabel("0" + LanguageManager.get("dashboard.unit.units"));
        totalSalesValue = new JLabel("0");
        totalRevenueValue = new JLabel(LanguageManager.get("dashboard.currency.prefix") + "0.00");

        AnimatedCard card1 = createStatCard("\uD83D\uDCE6", LanguageManager.get("dashboard.stat.products"), totalProductsValue, ACCENT_GREEN);
        AnimatedCard card2 = createStatCard("\uD83D\uDCCA", LanguageManager.get("dashboard.stat.stock"), totalStockValue, ACCENT_SKYBLUE);
        AnimatedCard card3 = createStatCard("\uD83E\uDDFE", LanguageManager.get("dashboard.stat.sales"), totalSalesValue, ACCENT_BROWN);
        AnimatedCard card4 = createStatCard("\uD83D\uDCB0", LanguageManager.get("dashboard.stat.revenue"), totalRevenueValue, ACCENT_GREEN);

        statsPanel.add(card1);
        statsPanel.add(card2);
        statsPanel.add(card3);
        statsPanel.add(card4);

        // Staggered entrance: each card fades + slides up a little after the previous one.
        card1.playEntrance(0);
        card2.playEntrance(90);
        card3.playEntrance(180);
        card4.playEntrance(270);

        body.add(statsPanel);
        body.add(Box.createVerticalStrut(20));

        // ---- Alerts card ----
        AnimatedCard alertCard = new AnimatedCard(CARD_BG, CARD_BG_HOVER, ACCENT_BROWN, false);
        alertCard.setLayout(new BorderLayout());
        alertCard.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        alertCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        alertCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel alertTitle = new JLabel("\u26A0 " + LanguageManager.get("dashboard.alerts.title"));
        alertTitle.setFont(new Font("Nirmala UI", Font.BOLD, 15));
        alertTitle.setForeground(TEXT_DARK);
        alertTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        alertCard.add(alertTitle, BorderLayout.NORTH);

        alertContentPanel = new JPanel();
        alertContentPanel.setOpaque(false);
        alertContentPanel.setLayout(new BoxLayout(alertContentPanel, BoxLayout.Y_AXIS));
        alertCard.add(alertContentPanel, BorderLayout.CENTER);
        attachHoverForwarding(alertCard, alertCard);

        body.add(alertCard);
        body.add(Box.createVerticalGlue());

        return body;
    }

    /**
     * Loads a real photo from src/main/resources/images/dashboard_banner.jpg
     * (classpath: /images/dashboard_banner.jpg) and draws it center-cropped
     * inside a rounded rectangle, fading in when the dashboard opens.
     *
     * If the image file hasn't been added yet (or the resources folder
     * isn't marked as a Resources Root / project hasn't been rebuilt), it
     * falls back to a soft gradient with a reminder message instead of
     * crashing, so the app still runs fine either way.
     */
    private static class RealImageBannerPanel extends JPanel {
        private Image bannerImage;
        private float opacity = 0f;
        private Timer fadeTimer;

        public RealImageBannerPanel() {
            setOpaque(false);
            loadImage();
            startFadeIn();
        }

        private void loadImage() {
            java.net.URL imgUrl = getClass().getResource("/images/dashboard_banner.jpg");
            if (imgUrl != null) {
                bannerImage = new ImageIcon(imgUrl).getImage();
            }
        }

        private void startFadeIn() {
            fadeTimer = new Timer(20, e -> {
                opacity = Math.min(1f, opacity + 0.05f);
                repaint();
                if (opacity >= 1f) {
                    ((Timer) e.getSource()).stop();
                }
            });
            fadeTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) {
                g2.dispose();
                return;
            }

            int arc = 18;
            g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, w, h, arc, arc));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, opacity))));

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

                // subtle dark gradient at the bottom, in case text is added over the photo later
                GradientPaint overlay = new GradientPaint(0, h - 80, new Color(0, 0, 0, 0), 0, h, new Color(0, 0, 0, 90));
                g2.setPaint(overlay);
                g2.fillRect(0, h - 80, w, 80);
            } else {
                GradientPaint sky = new GradientPaint(0, 0, new Color(179, 229, 252), 0, h, new Color(224, 247, 250));
                g2.setPaint(sky);
                g2.fillRoundRect(0, 0, w, h, arc, arc);

                g2.setColor(new Color(90, 100, 90));
                g2.setFont(new Font("Nirmala UI", Font.ITALIC, 14));
                String msg = "Add a photo at src/main/resources/images/dashboard_banner.jpg";
                FontMetrics fm = g2.getFontMetrics();
                int msgW = fm.stringWidth(msg);
                g2.drawString(msg, (w - msgW) / 2, h / 2);
            }

            g2.setClip(null);
            g2.dispose();
        }
    }

    /**
     * A rounded card that, on hover, brightens and (optionally) visually
     * floats upward with a growing soft shadow. It also supports a one-time
     * "entrance" animation (fade + slide up) played when the dashboard
     * first opens, for a more modern feel.
     *
     * Because mouse events land on whichever child component is directly
     * under the cursor (icon/text labels), hoverEnter/hoverExit are exposed
     * as public methods and forwarded from every descendant via
     * attachHoverForwarding() below — otherwise the card's own listener
     * would never fire.
     */
    private static class AnimatedCard extends JPanel {
        private float hoverProgress = 0f;
        private float entranceProgress = 1f; // 1f = fully visible/settled by default
        private final Color baseBg;
        private final Color hoverBg;
        private final Color accentColor;
        private final boolean floatEffect;
        private Timer animTimer;
        private Timer entranceTimer;

        public AnimatedCard(Color baseBg, Color hoverBg, Color accentColor, boolean floatEffect) {
            this.baseBg = baseBg;
            this.hoverBg = hoverBg;
            this.accentColor = accentColor;
            this.floatEffect = floatEffect;
            setOpaque(false);
        }

        public void hoverEnter() {
            animateTo(1f);
        }

        public void hoverExit() {
            animateTo(0f);
        }

        /** Plays a fade + slide-up entrance animation, optionally after a delay (for staggering). */
        public void playEntrance(int delayMs) {
            entranceProgress = 0f;
            Timer delayTimer = new Timer(Math.max(delayMs, 1), e -> {
                ((Timer) e.getSource()).stop();
                entranceTimer = new Timer(15, ev -> {
                    entranceProgress = Math.min(1f, entranceProgress + 0.08f);
                    repaint();
                    if (entranceProgress >= 1f) {
                        ((Timer) ev.getSource()).stop();
                    }
                });
                entranceTimer.start();
            });
            delayTimer.setRepeats(false);
            delayTimer.start();
        }

        private void animateTo(float target) {
            if (animTimer != null && animTimer.isRunning()) {
                animTimer.stop();
            }
            animTimer = new Timer(12, e -> {
                float step = 0.1f;
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
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 16;
            int lift = floatEffect ? (int) (7 * hoverProgress) : 0;
            int shadowSize = (int) (8 + 10 * hoverProgress);

            int entranceOffsetY = (int) (18 * (1f - entranceProgress));
            float entranceAlpha = Math.max(0f, Math.min(1f, entranceProgress));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, entranceAlpha));

            for (int i = shadowSize; i > 0; i -= 1) {
                g2.setColor(new Color(30, 40, 30, 4));
                g2.fillRoundRect(i / 2, i + lift + entranceOffsetY, getWidth() - i, getHeight() - i, arc, arc);
            }

            g2.translate(0, -lift + entranceOffsetY);
            super.paint(g2);

            g2.dispose();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 16;
            float t = hoverProgress;
            int r = (int) (baseBg.getRed() + (hoverBg.getRed() - baseBg.getRed()) * t);
            int gC = (int) (baseBg.getGreen() + (hoverBg.getGreen() - baseBg.getGreen()) * t);
            int b = (int) (baseBg.getBlue() + (hoverBg.getBlue() - baseBg.getBlue()) * t);
            g2.setColor(new Color(r, gC, b));
            g2.fillRoundRect(0, 0, getWidth(), getHeight() - 4, arc, arc);

            int accentHeight = (int) (4 + 2 * t);
            g2.setColor(accentColor);
            g2.fillRoundRect(0, 0, getWidth(), accentHeight + arc / 2, arc, arc);
            g2.setColor(new Color(r, gC, b));
            g2.fillRect(0, accentHeight, getWidth(), (getHeight() - 4) - accentHeight);

            g2.dispose();
        }
    }

    // Recursively attaches a mouse listener to a component and every descendant,
    // so hovering ANYWHERE inside the card (including its icon/text labels)
    // correctly triggers the card's own float/brighten animation.
    private void attachHoverForwarding(Component comp, AnimatedCard target) {
        comp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                target.hoverEnter();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                target.hoverExit();
            }
        });

        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                attachHoverForwarding(child, target);
            }
        }
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
        totalStockValue.setText(totalStock + LanguageManager.get("dashboard.unit.units"));
        totalSalesValue.setText(String.valueOf(totalSales));
        totalRevenueValue.setText(LanguageManager.get("dashboard.currency.prefix") + String.format("%.2f", totalRevenue));

        int unreadMessages = messageDAO.getUnreadCount(currentFarmer.getFarmerId(), "FARMER");
        String chatBaseLabel = LanguageManager.get("dashboard.nav.chat");
        JLabel chatTextLabel = findLabelByName(chatBtn, "sidebarLabel");
        if (chatTextLabel != null) {
            if (unreadMessages > 0) {
                chatTextLabel.setText(chatBaseLabel + " (" + unreadMessages + ")");
            } else {
                chatTextLabel.setText(chatBaseLabel);
            }
        }

        alertContentPanel.removeAll();
        List<Product> lowStockList = productDAO.getLowStockProducts(currentFarmer.getFarmerId());

        if (!lowStockList.isEmpty()) {
            for (Product p : lowStockList) {
                JLabel item = new JLabel("\u26A0 " + p.getProductName() + " (" + p.getStockQuantity() + " " + p.getUnit() + LanguageManager.get("dashboard.alerts.left"));
                item.setForeground(ALERT_RED);
                item.setFont(new Font("Nirmala UI", Font.PLAIN, 13));
                item.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
                alertContentPanel.add(item);
            }
        } else {
            JLabel none = new JLabel(LanguageManager.get("dashboard.alerts.none"));
            none.setForeground(TEXT_MUTED);
            none.setFont(new Font("Nirmala UI", Font.PLAIN, 13));
            alertContentPanel.add(none);
        }

        alertContentPanel.revalidate();
        alertContentPanel.repaint();
    }

    private JLabel findLabelByName(Container container, String name) {
        for (Component c : container.getComponents()) {
            if (c instanceof JLabel && name.equals(c.getName())) {
                return (JLabel) c;
            }
            if (c instanceof Container) {
                JLabel found = findLabelByName((Container) c, name);
                if (found != null) return found;
            }
        }
        return null;
    }

    private AnimatedCard createStatCard(String icon, String title, JLabel valueLabel, Color accentColor) {
        AnimatedCard card = new AnimatedCard(CARD_BG, CARD_BG_HOVER, accentColor, true);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel innerContent = new JPanel();
        innerContent.setOpaque(false);
        innerContent.setLayout(new BoxLayout(innerContent, BoxLayout.Y_AXIS));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(ICON_FONT_LARGE);
        iconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLabel.setFont(new Font("Nirmala UI", Font.PLAIN, 13));
        titleLabel.setForeground(TEXT_MUTED);

        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueLabel.setFont(new Font("Nirmala UI", Font.BOLD, 24));
        valueLabel.setForeground(TEXT_DARK);

        innerContent.add(iconLabel);
        innerContent.add(Box.createVerticalStrut(10));
        innerContent.add(titleLabel);
        innerContent.add(Box.createVerticalStrut(4));
        innerContent.add(valueLabel);

        card.add(innerContent, BorderLayout.CENTER);

        attachHoverForwarding(card, card);

        return card;
    }
}