package org.example;

import javax.swing.*;
import java.awt.*;

public class HandshakePanel extends JPanel {

    public HandshakePanel() {
        setPreferredSize(new Dimension(400, 180));
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Sky gradient background
        GradientPaint sky = new GradientPaint(0, 0, new Color(135, 206, 235), 0, h, new Color(200, 235, 200));
        g2.setPaint(sky);
        g2.fillRect(0, 0, w, h);

        // Sun
        g2.setColor(new Color(255, 221, 89));
        g2.fillOval(w - 70, 15, 45, 45);

        // Ground
        g2.setColor(new Color(120, 190, 100));
        g2.fillRect(0, h - 40, w, 40);

        int groundY = h - 40;

        // ---- Farmer (left figure) ----
        int farmerX = w / 2 - 90;
        drawPerson(g2, farmerX, groundY, new Color(240, 200, 160), new Color(85, 130, 80), true);

        // ---- Customer (right figure) ----
        int customerX = w / 2 + 40;
        drawPerson(g2, customerX, groundY, new Color(230, 190, 160), new Color(70, 100, 170), false);

        // ---- Handshake connector ----
        g2.setColor(new Color(200, 150, 100));
        g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(farmerX + 55, groundY - 70, customerX + 5, groundY - 70);
        g2.setColor(new Color(230, 190, 160));
        g2.fillOval((farmerX + 55 + customerX + 5) / 2 - 8, groundY - 78, 16, 16);
    }

    private void drawPerson(Graphics2D g2, int baseX, int groundY, Color skinColor, Color shirtColor, boolean isFarmer) {
        // Body
        g2.setColor(shirtColor);
        g2.fillRoundRect(baseX, groundY - 65, 50, 65, 15, 15);

        // Arm reaching toward the center
        g2.setColor(skinColor);
        int armStartX = isFarmer ? baseX + 45 : baseX + 5;
        g2.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(armStartX, groundY - 45, armStartX + (isFarmer ? 15 : -15), groundY - 70);

        // Head
        g2.setColor(skinColor);
        g2.fillOval(baseX + 5, groundY - 100, 40, 40);

        if (isFarmer) {
            // Straw hat
            g2.setColor(new Color(222, 184, 108));
            g2.fillOval(baseX - 8, groundY - 108, 56, 14);
            g2.fillArc(baseX + 5, groundY - 118, 40, 25, 0, 180);
        } else {
            // Simple hair
            g2.setColor(new Color(90, 60, 40));
            g2.fillArc(baseX + 3, groundY - 102, 44, 30, 0, 180);
        }

        // Simple smile
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawArc(baseX + 15, groundY - 85, 15, 10, 200, 140);
    }
}