package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class MandiPriceFrame extends JFrame {

    private Farmer currentFarmer;
    private GlowingTextField cropField;
    private JLabel resultLabel;
    private JLabel stateLabel;

    private MandiPriceDAO mandiPriceDAO = new MandiPriceDAO();
    private MandiPriceService mandiPriceService = new MandiPriceService();

    private static final Color PRIMARY_GREEN = new Color(76, 145, 65);
    private static final Color PRIMARY_GREEN_DARK = new Color(60, 120, 50);
    private static final Color BACKGROUND = new Color(245, 250, 240);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color SKY_TINT = new Color(224, 240, 252);
    private static final Color WARNING_RED = new Color(200, 50, 50);

    private Timer pulseTimer;
    private float resultFade = 1f;
    private Timer fadeTimer;

    public MandiPriceFrame(Farmer farmer) {
        this.currentFarmer = farmer;

        setTitle(LanguageManager.get("mandi.title"));
        setSize(700, 560);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout());

        add(buildBanner(), BorderLayout.NORTH);

        // ---- Center content ----
        JPanel outerWrap = new JPanel(new GridBagLayout());
        outerWrap.setBackground(BACKGROUND);

        RoundedCardPanel card = new RoundedCardPanel();
        card.setLayout(new BorderLayout(0, 18));
        card.setBorder(new EmptyBorder(30, 34, 30, 34));
        card.setPreferredSize(new Dimension(500, 320));

        JLabel instructionLabel = new JLabel("<html><body style='width:400px'>" + LanguageManager.get("mandi.instruction") + "</body></html>");
        instructionLabel.setFont(new Font("Nirmala UI", Font.PLAIN, 14));
        instructionLabel.setForeground(new Color(70, 70, 70));

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        searchRow.setBackground(CARD_BG);
        searchRow.setOpaque(false);

        cropField = new GlowingTextField(18);
        AnimatedButton searchBtn = new AnimatedButton(LanguageManager.get("mandi.button.search"), PRIMARY_GREEN);

        searchRow.add(cropField);
        searchRow.add(searchBtn);

        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setOpaque(false);
        resultPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        resultLabel = new JLabel(" ");
        resultLabel.setFont(new Font("Nirmala UI", Font.BOLD, 17));
        resultPanel.add(resultLabel, BorderLayout.CENTER);

        JPanel topBlock = new JPanel();
        topBlock.setOpaque(false);
        topBlock.setLayout(new BoxLayout(topBlock, BoxLayout.Y_AXIS));
        instructionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        topBlock.add(instructionLabel);
        topBlock.add(Box.createVerticalStrut(6));
        topBlock.add(searchRow);

        card.add(topBlock, BorderLayout.NORTH);
        card.add(resultPanel, BorderLayout.CENTER);

        outerWrap.add(card);
        add(outerWrap, BorderLayout.CENTER);

        searchBtn.addActionListener(e -> handleSearch());
        cropField.addActionListener(e -> handleSearch());

        setVisible(true);
    }

    // ================= Banner =================
    private JPanel buildBanner() {
        PhotoBannerPanel banner = new PhotoBannerPanel("/images/mandi_banner.jpg", LanguageManager.get("mandi.title"));
        banner.setPreferredSize(new Dimension(700, 130));
        banner.setLayout(null);

        CircleBackButton backBtn = new CircleBackButton();
        backBtn.setBounds(15, 15, 42, 42);
        backBtn.setToolTipText(LanguageManager.get("common.backtodashboard"));
        backBtn.addActionListener(e -> {
            dispose();
            new DashboardFrame(currentFarmer);
        });
        banner.add(backBtn);

        stateLabel = new JLabel(
                currentFarmer.getState() == null || currentFarmer.getState().isEmpty()
                        ? LanguageManager.get("mandi.state.notset")
                        : LanguageManager.get("mandi.state.prefix") + currentFarmer.getState()
        );
        stateLabel.setFont(new Font("Nirmala UI", Font.PLAIN, 13));
        stateLabel.setForeground(Color.WHITE);
        stateLabel.setBounds(0, 95, 680, 25);
        stateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        banner.add(stateLabel);

        return banner;
    }

    // ================= Search logic (with pulsing + fade-in) =================
    private void handleSearch() {
        stopPulse();

        String cropName = cropField.getText().trim();
        String state = currentFarmer.getState();

        if (cropName.isEmpty()) {
            showResult(LanguageManager.get("mandi.msg.entercrop"), WARNING_RED);
            return;
        }

        if (state == null || state.trim().isEmpty()) {
            showResult(LanguageManager.get("mandi.msg.nostate"), WARNING_RED);
            return;
        }

        startPulse(LanguageManager.get("mandi.msg.checking") + cropName + LanguageManager.get("mandi.msg.in") + state + " ...");

        // Small delay so the pulse animation is visible even on a fast cache hit
        Timer searchDelay = new Timer(350, e -> {
            Double price = mandiPriceDAO.getCachedMandiPrice(cropName, state);

            if (price == null) {
                price = mandiPriceService.fetchAndCacheMandiPrice(cropName, state);
            }

            stopPulse();

            if (price == null) {
                showResult(LanguageManager.get("mandi.msg.notavailable") + cropName + LanguageManager.get("mandi.msg.in") + state + ".", WARNING_RED);
            } else {
                showResult(LanguageManager.get("mandi.msg.resultfor") + cropName + LanguageManager.get("mandi.msg.in") + state + ": \u20B9" + price + LanguageManager.get("mandi.msg.perkg"), PRIMARY_GREEN_DARK);
            }
        });
        searchDelay.setRepeats(false);
        searchDelay.start();
    }

    private void startPulse(String baseText) {
        resultLabel.setForeground(new Color(90, 90, 90));
        int[] dots = {0};
        pulseTimer = new Timer(400, e -> {
            dots[0] = (dots[0] + 1) % 4;
            StringBuilder sb = new StringBuilder(baseText.replaceAll("\\.*$", ""));
            for (int i = 0; i < dots[0]; i++) sb.append(".");
            resultLabel.setText(sb.toString());
        });
        pulseTimer.start();
    }

    private void stopPulse() {
        if (pulseTimer != null && pulseTimer.isRunning()) {
            pulseTimer.stop();
        }
    }

    private void showResult(String text, Color color) {
        resultLabel.setText(text);
        resultLabel.setForeground(color);

        // Fade + scale-in animation for the result
        resultFade = 0f;
        if (fadeTimer != null && fadeTimer.isRunning()) fadeTimer.stop();
        fadeTimer = new Timer(15, null);
        Font baseFont = new Font("Nirmala UI", Font.BOLD, 17);
        fadeTimer.addActionListener(e -> {
            resultFade = Math.min(1f, resultFade + 0.08f);
            int size = (int) (14 + 3 * resultFade); // grows from 14 to 17
            resultLabel.setFont(baseFont.deriveFont((float) size));
            if (resultFade >= 1f) fadeTimer.stop();
        });
        fadeTimer.start();
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
                g2.fillRoundRect(i, i + 2, w - i * 2, h - i * 2, 22, 22);
            }

            g2.setColor(CARD_BG);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 20, 20);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ================= Glowing animated border text field =================
    private class GlowingTextField extends JTextField {
        private float glowProgress = 0f;
        private Timer glowTimer;

        GlowingTextField(int columns) {
            super(columns);
            setFont(new Font("Nirmala UI", Font.PLAIN, 15));
            setOpaque(false);
            setBorder(new EmptyBorder(10, 12, 10, 12));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) { animateTo(1f); }
                @Override
                public void focusLost(FocusEvent e) { animateTo(0f); }
            });
        }

        private void animateTo(float target) {
            if (glowTimer != null && glowTimer.isRunning()) glowTimer.stop();
            glowTimer = new Timer(15, null);
            glowTimer.addActionListener(e -> {
                float step = 0.15f;
                if (glowProgress < target) glowProgress = Math.min(target, glowProgress + step);
                else if (glowProgress > target) glowProgress = Math.max(target, glowProgress - step);
                repaint();
                if (glowProgress == target) glowTimer.stop();
            });
            glowTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            g2.setColor(glowProgress > 0 ? SKY_TINT : Color.WHITE);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);

            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            Color base = new Color(200, 200, 200);
            Color glow = PRIMARY_GREEN;
            int r = (int) (base.getRed() + (glow.getRed() - base.getRed()) * glowProgress);
            int gr = (int) (base.getGreen() + (glow.getGreen() - base.getGreen()) * glowProgress);
            int b = (int) (base.getBlue() + (glow.getBlue() - base.getBlue()) * glowProgress);

            g2.setColor(new Color(r, gr, b));
            g2.setStroke(new BasicStroke(1.5f + 1.5f * glowProgress));
            g2.drawRoundRect(1, 1, w - 3, h - 3, 12, 12);

            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width, 42);
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
                g2.drawString("(mandi_banner.jpg not found in resources/images)", 70, h - 12);
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
}